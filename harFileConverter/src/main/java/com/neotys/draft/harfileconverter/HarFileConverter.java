package com.neotys.draft.harfileconverter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.neotys.neoload.model.listener.EventListener;
import com.neotys.neoload.model.v3.project.Project;
import com.neotys.neoload.model.v3.project.server.Server;
import com.neotys.neoload.model.v3.project.userpath.Container;
import com.neotys.neoload.model.v3.project.userpath.Header;
import com.neotys.neoload.model.v3.project.userpath.Request;
import com.neotys.neoload.model.v3.project.userpath.UserPath;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;

/**
 * <p>This class converts a HTTP ARCHIVE file (.har) to a 
 * Neoload project format (.nlp). </p>
 * 
 * Use the {@code returnParts()} function to return a List< Part > at Neoload format
 * 
 *
 */

public class HarFileConverter {

	static final Logger logger = LoggerFactory.getLogger(HarFileConverter.class);

	//Neoload data:
	Container.Builder actionsContainer = null;
	List<Server> servers = new ArrayList<>(); //To avoid server doubles, a list of known servers must be maintained

	//EventListener:
	EventListenerUtilsHAR eventListenerUtilsHAR = new EventListenerUtilsHAR();
	
	//Constructors
	public HarFileConverter(){
		//Do nothing, constructor with no Listener
	}

	public HarFileConverter(final EventListener eventListener){
		eventListenerUtilsHAR.addEventListener(eventListener);
	}

	
	/**
	 * <p>Use this method to run the global process to convert HAR to Neoload Project (.nlp). </p>
	 * 
	 */

	Project returnProject(File harSelectedFile) throws HarReaderException {

		eventListenerUtilsHAR.startScript(harSelectedFile.getName());
		
		logger.info("Selected file: {}" , harSelectedFile.getAbsolutePath());
		
		
		
		actionsContainer = Container.builder().name("Actions");

		HarReader harReader = new HarReader();
		Har har = harReader.readFromFile(harSelectedFile);
		Stream<HarEntry> streamHarEntries = har.getLog().getEntries().stream();

		streamHarEntries.forEach( currentHarEntry -> { 
			try {

				this.buildServer(currentHarEntry);
				this.buildRequest(currentHarEntry);
				eventListenerUtilsHAR.readSupportedAction("Success conversion URL");

			} catch (Exception e) {
				logger.error("Failed conversion for URL : {} " ,  currentHarEntry.getRequest().getUrl());
				logger.error("Cause = {} " , e.getMessage());
				eventListenerUtilsHAR.readUnsupportedAction("Failed conversion URL");
			}

		});

		UserPath userPath = UserPath.builder()
				.init(Container.builder().name("Init").build())
				.actions(actionsContainer.build())
				.end(Container.builder().name("End").build())
				.name("Demo User Path")
				.build();

		
		eventListenerUtilsHAR.endScript();
		
		return Project.builder()
				.name("test_HARFileConverter_Project")
				.addUserPaths(userPath)
				.addServers(servers.toArray(new Server[0]))
				.build();

	}

	/**
	 * <p>This method creates a Request object (Neoload) from a HarEntry object (har-reader) </p>
	 * 
	 */

	private void buildRequest(HarEntry currentHarEntry) throws IOException, ContentTypeException {

		URL url = new URL(currentHarEntry.getRequest().getUrl());

		//Create Stream for HarHeader format:
		Stream<HarHeader> streamHarHeaders = currentHarEntry.getRequest().getHeaders().stream();
		//Convert Stream<HarHeader>(de.sstoehr.harreader) to Stream<Header> (Neoload):
		Stream<Header> streamHeaders = streamHarHeaders.map( currentHarHeader -> 
		Header.builder()
		.name(currentHarHeader.getName())
		.value(currentHarHeader.getValue())
		.build() 
				);

		Request.Builder requestBuilder = Request.builder()
				.name(url.getPath())
				.url(url.toString())
				.method(currentHarEntry.getRequest().getMethod().toString())
				.addAllHeaders(streamHeaders::iterator)
				.server(url.getHost());

		//POST data management : body / bodyBinary / parts 
		//Get the current Content-Type :
		Optional<String> currentContentType = currentHarEntry.getRequest().getHeaders().stream()
				.filter(header -> "content-type".equalsIgnoreCase(header.getName()) && header.getValue() != null)
				.map(HarHeader::getValue)
				.findFirst()
				;
		
		//Content-Type was found and PostData is not empty:
		if (currentContentType.isPresent() && currentHarEntry.getRequest().getPostData().getText() != null) {

			MediaType mediaType = MediaType.parse(currentContentType.get());

			//ANY_TEXT_TYPE :
			if(mediaType.is(MediaType.ANY_TEXT_TYPE)) {
				requestBuilder.body(currentHarEntry.getRequest().getPostData().getText());
			}
			//FORM_CONTENT:
			else if("application".equalsIgnoreCase(mediaType.type())
					&& mediaType.subtype().toLowerCase().contains("form-urlencoded")) { 								
				
				//TODO Wait for information : do we need URLDecoder.decode() or is it up to neoloadWriter to manage ?
				requestBuilder.body(currentHarEntry.getRequest().getPostData().getText());
			}
			//RAW_CONTENT :
			else if("application".equalsIgnoreCase(mediaType.type())) {
				requestBuilder.bodyBinary(currentHarEntry.getRequest().getPostData().getText().getBytes("UTF8"));
			}
			//MULTIPART_CONTENT:
			else if("multipart".equalsIgnoreCase(mediaType.type())) {
				//Get the boundary information in the Content-Type Header:
				String boundary = ParameterExtractor.extract(currentContentType.get(), "boundary=");
				MultipartAnalyzer analyseMultipart = new MultipartAnalyzer(
						currentHarEntry.getRequest().getPostData().getText(),
						boundary);
				requestBuilder.parts(analyseMultipart.returnParts());
			}
			//UNKNOWN Content-Type format :
			else {
				throw new ContentTypeException("Content-Type format UNKNOWN : " + currentContentType.get());
			}
		}
		//Content-Type NOT FOUND but postData is present:
		else if (!currentContentType.isPresent() && currentHarEntry.getRequest().getPostData().getText() != null) {
			throw new ContentTypeException("Content-Type NOT FOUND");
		}

		
		Request request = requestBuilder.build();
		actionsContainer.addSteps(request);
		
		eventListenerUtilsHAR.readSupportedAction("HAR Request success");

	}

	/**
	 * <p>This method updates the Server (Neoload) List from a HarEntry Object (har-reader).
	 * Doubles are not allowed in Server List.</p>
	 * 
	 */

	private void buildServer(HarEntry currentHarEntry) throws MalformedURLException {
		URL url = new URL(currentHarEntry.getRequest().getUrl());

		Server server = Server.builder()
				.name(url.getHost())
				.host(url.getHost())
				.port(String.valueOf(url.getPort() != -1 ? url.getPort() : url.getDefaultPort()))
				.scheme(Server.Scheme.valueOf(url.getProtocol().toUpperCase())) //valueof converts String to equivalent enum value ( HTTP / HTTPS )
				.build();

		if(servers.indexOf(server)==-1) { //Doubles are not allowed in Server List
			servers.add(server);
		}

	}
	
	
	
	
}
