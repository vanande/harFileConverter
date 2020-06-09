package com.neotys.draft.harfileconverter;

import com.google.common.net.MediaType;
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * <p>This class converts a HTTP ARCHIVE file (.har) to a 
 * Neoload project format (.nlp). </p>
 * 
 * Use the {@code returnParts()} function to return a List< Part > at Neoload format
 * 
 *
 */

public class HarFileConverterMain {

	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);


	public static void main(String[] args) throws HarReaderException {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTTP Archive(.har)", "har");
		fileChooser.setFileFilter(filter);
		
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File harSelectedFile = fileChooser.getSelectedFile();
			logger.info("Selected file: {}" , harSelectedFile.getAbsolutePath());


			Container.Builder actionsContainer = Container.builder().name("Actions");
			//To avoid server doubles, a list of known servers must be maintained :
			List<Server> servers = new ArrayList<>();

			HarReader harReader = new HarReader();
			Har har = harReader.readFromFile(harSelectedFile);

			List<HarEntry> harFileListOfEntries = har.getLog().getEntries();
			Stream<HarEntry> streamHarEntries = harFileListOfEntries.stream();

			streamHarEntries.forEach( currentHarEntry -> {

				try {
					URL url = new URL(currentHarEntry.getRequest().getUrl());

					Server server = Server.builder()
							.name(url.getHost())
							.host(url.getHost())
							.port(String.valueOf(url.getPort() != -1 ? url.getPort() : url.getDefaultPort()))
							.scheme(Server.Scheme.valueOf(url.getProtocol().toUpperCase())) //valueof converts String to equivalent enum value ( HTTP / HTTPS )
							.build();
					
					if(servers.indexOf(server)!=-1) {
						server = servers.get(servers.indexOf(server));
					} else {
						servers.add(server);
					}

					
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
					
					if (currentContentType.isPresent()  && currentHarEntry.getRequest().getPostData().getText() != null) {

	 					MediaType mediaType = MediaType.parse(currentContentType.get());
	 					
	 					//ANY_TEXT_TYPE :
	 					if(mediaType.is(MediaType.ANY_TEXT_TYPE)) {
							requestBuilder.body(currentHarEntry.getRequest().getPostData().getText());
						}
	 					//FORM_CONTENT:
						else if("application".equalsIgnoreCase(mediaType.type())
								&& mediaType.subtype().toLowerCase().contains("form-urlencoded")) { 								
							requestBuilder.body(currentHarEntry.getRequest().getPostData().getText());
						}
						//RAW_CONTENT :
						else if("application".equalsIgnoreCase(mediaType.type())) {
							requestBuilder.bodyBinary(currentHarEntry.getRequest().getPostData().getText().getBytes());
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
						else
							throw new ContentTypeUnknownException("UNKNOWN Content-Type format : " + currentContentType.get());
					}
					Request request = requestBuilder.build();
					actionsContainer.addSteps(request);

				} catch (Exception e) {
					logger.error("Failed conversion URL : {} " ,  currentHarEntry.getRequest().getUrl());
					logger.error("Cause = {} " , e.getMessage());
				}
				
			});

			UserPath userPath = UserPath.builder()
					.init(Container.builder().name("Init").build())
					.actions(actionsContainer.build())
					.end(Container.builder().name("End").build())
					.name("Demo User Path")
					.build();

			Project project = Project.builder()
					.name("test_HARFileConverter_Project")
					.addUserPaths(userPath)
					.addServers(servers.toArray(new Server[0]))
					.build();

			NeoLoadWriter writer = new NeoLoadWriter(project,"C:\\Users\\jerome\\Documents\\NeoLoad Projects");
			writer.write(true, "7.0", "7.2.2");
			
			
			
		}
	}
}
