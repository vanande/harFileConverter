package com.neotys.draft.harfileconverter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
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
import com.neotys.neoload.model.v3.writers.neoload.NeoLoadWriter;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;

/**
 * <p>This class converts a HTTP ARCHIVE file (.har) to a 
 * Neoload project format (.nlp). </p>
 * 
 * Use the {@code writeProject()} function to write the Neoload project
 * 
 *
 */ 

public class HarFileConverter { 

	static final Logger logger = LoggerFactory.getLogger(HarFileConverter.class);

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
	 * <p>Use this method to write the Neoload Project (.nlp) in . </p>
	 * 
	 */

	public void writeProject(File harSelectedFile, File neoloadProjectFile) {

		try {
			String neoloadProjectFolder = neoloadProjectFile.getParent();
			String neoloadProjectName = FilenameUtils.removeExtension(neoloadProjectFile.getName());

			NeoLoadWriter writer = new NeoLoadWriter(returnProject(harSelectedFile,neoloadProjectName),neoloadProjectFolder);
			writer.write(true, "7.0", "7.2.2");

		} catch (Exception e) {
			logger.error("File conversion has failed : {} " ,  harSelectedFile.getAbsolutePath());
			logger.error("Cause = {} " , e.getMessage());
		}
	}


	/**
	 * <p>Use this method to convert HAR file to Neoload Project (.nlp). </p>
	 * 
	 * @return Neoload Project
	 * 
	 */ 

	protected Project returnProject(File harSelectedFile, String neoloadProjectName) throws HarReaderException {

		eventListenerUtilsHAR.startScript(harSelectedFile.getName());

		//Neoload objects:
		List<Server> servers = new ArrayList<>(); //To avoid server doubles, a list of known servers must be maintained	
		Container.Builder actionsContainer = Container.builder().name("Actions"); //This is the "root" container for all Actions
		LinkedHashMap<String,Container.Builder> hashMapContainerBuilderForPages = new LinkedHashMap<>(); // 1 container created for each "pageref" HAR objects, they will be subcontainer of actionsContainer

		HarReader harReader = new HarReader();
		Har har = harReader.readFromFile(harSelectedFile);


		Stream<HarEntry> streamHarEntries = har.getLog().getEntries().stream();
		//need to sort the Stream because HAR entries are not written in the correct chronological order :
		streamHarEntries.sorted(Comparator.comparing(HarEntry::getStartedDateTime))
		.forEach( currentHarEntry -> {

			try {
				this.buildContainer(currentHarEntry, hashMapContainerBuilderForPages); //used for har "pageref" management
				this.buildServer(currentHarEntry, servers);
				this.buildRequest(currentHarEntry,actionsContainer,hashMapContainerBuilderForPages);
				eventListenerUtilsHAR.readSupportedAction("Success conversion URL");

			} catch (Exception e) {
				logger.error("Failed conversion for URL : {} " ,  currentHarEntry.getRequest().getUrl());
				logger.error("Error Message = {}" , e.getMessage());
				eventListenerUtilsHAR.readUnsupportedAction("Failed conversion URL");
			}

		});

		//Add all Containers in hashMapContainerBuilderForPages to the "root" container : actionsContainer
		hashMapContainerBuilderForPages.entrySet().stream()
		.map(Map.Entry::getValue)
		.forEach(currentContainerBuilder ->
		actionsContainer.addSteps(currentContainerBuilder.build()));


		UserPath userPath = UserPath.builder()
				.init(Container.builder().name("Init").build())
				.actions(actionsContainer.build())
				.end(Container.builder().name("End").build())
				.name("Demo User Path")
				.build();


		eventListenerUtilsHAR.endScript();

		return Project.builder()
				.name(neoloadProjectName)
				.addUserPaths(userPath)
				.addServers(servers.toArray(new Server[0]))
				.build();

	}

	/**
	 * <p>This method creates a Request object (Neoload) from a HarEntry object (har-reader) </p>
	 * 
	 */

	protected void buildRequest(HarEntry currentHarEntry, Container.Builder actionsContainer,LinkedHashMap<String,Container.Builder> hashMapContainerBuilderForPages) throws IOException {

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

				//Do we need URLDecoder.decode() or is it up to neoloadWriter to manage ?
				requestBuilder.body(currentHarEntry.getRequest().getPostData().getText());
			}
			//RAW_CONTENT :
			else if("application".equalsIgnoreCase(mediaType.type())) {
				requestBuilder.bodyBinary(currentHarEntry.getRequest().getPostData().getText().getBytes(StandardCharsets.UTF_8));
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
				logger.warn("URL : {} " ,  currentHarEntry.getRequest().getUrl());
				logger.warn("Content-Type format UNKNOWN : {} , Post Data will be considered as binary by default" , currentContentType.get());
				requestBuilder.bodyBinary(currentHarEntry.getRequest().getPostData().getText().getBytes(StandardCharsets.UTF_8));
			}
		}
		//Content-Type NOT FOUND but postData is present:
		else if (!currentContentType.isPresent() && currentHarEntry.getRequest().getPostData().getText() != null) {

			logger.warn("URL : {} " ,  currentHarEntry.getRequest().getUrl());
			logger.warn("Content-Type NOT FOUND, Post Data will be considered as binary by default");
			requestBuilder.bodyBinary(currentHarEntry.getRequest().getPostData().getText().getBytes(StandardCharsets.UTF_8));

		}

		Request request = requestBuilder.build();

		//Get the pageRef and feed the correspondant Container, if pageRef is empty String or null, we will use the "root" actionsContainer
		if (currentHarEntry.getPageref() != null && !currentHarEntry.getPageref().isEmpty()) {
			String currentPageRef =  currentHarEntry.getPageref();
			hashMapContainerBuilderForPages.get(currentPageRef).addSteps(request);
		}
		else { //pageRef is empty String or null => use the "root" actionsContainer:
			actionsContainer.addSteps(request);
		}




	}


	/**
	 * <p>This method updates the Server (Neoload) List from a HarEntry Object (har-reader).
	 * Doubles are not allowed in Server List.</p>
	 * 
	 */ 

	protected void buildServer(HarEntry currentHarEntry, List<Server> servers) throws MalformedURLException {
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

	/**
	 * <p>This method updates the HashMap of Container (Neoload) from a HarEntry Object (har-reader).
	 * The objective is to create a Container for each object "pageref" contained in HAR file.
	 * All entries where pageref is missing will be stored in the "root" actionsContainer. </p>
	 * 
	 */

	protected void buildContainer(HarEntry currentHarEntry, LinkedHashMap<String,Container.Builder> hashMapContainerBuilderForPages) {
		//If pageRef is empty String or null, we will use the "root" actionsContainer
		if (currentHarEntry.getPageref() != null && !currentHarEntry.getPageref().isEmpty()) {
			String currentPageRef =  currentHarEntry.getPageref();

			if (!hashMapContainerBuilderForPages.containsKey(currentPageRef)) {
				hashMapContainerBuilderForPages.put(currentPageRef, Container.builder().name(currentPageRef));
			}
		}

	}
}
