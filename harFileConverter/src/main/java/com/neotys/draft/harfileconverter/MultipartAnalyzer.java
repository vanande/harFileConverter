package com.neotys.draft.harfileconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;

import com.neotys.neoload.model.v3.project.userpath.Part;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class converts a MIME Multipart String 
 * to a List < Part > (neoLoad format) .</p>
 * 
 * Use the {@code returnParts()} function to return a List< Part > at Neoload format
 * 
 *
 */ 

public class MultipartAnalyzer {

	private ByteArrayInputStream content;
	private byte[] boundary;
	static final Logger logger = LoggerFactory.getLogger(MultipartAnalyzer.class);

	Map<String, String> multipartStreamHeadersMap = null;
	Part.Builder currentPartBuilder = null;
	
	//SonarCloud asks to define a constant instead of duplicating this literal "filename" 6 times...
	String fileNameString = "filename"; 
	
	public MultipartAnalyzer(String multipartBodyString, String boundaryString) {
		//TODO : Warning, still questions about getBytes Charset...
		boundary = boundaryString.getBytes(StandardCharsets.UTF_8);
		content = new ByteArrayInputStream(multipartBodyString.getBytes(StandardCharsets.UTF_8));
	} 

	public List<Part> returnParts() throws IOException  {

		List<Part> multiPartList = new ArrayList<>();

		@SuppressWarnings("deprecation")
		MultipartStream multipartStream = new MultipartStream(content, boundary);

		boolean nextPart = multipartStream.skipPreamble();
		while (nextPart) {
			currentPartBuilder = Part.builder();

			//need to replace CRLF by ';' as CRLF can be a separator between parameters
			//need to replace ": " with classic name/value separator "="
			String multipartStreamHeaders = multipartStream.readHeaders().replaceAll("\r\n", ";").replace(": ", "="); 
			ParameterParser multiPartParamParser = new ParameterParser();
			char[] separators = {';'};
			multipartStreamHeadersMap = multiPartParamParser.parse(multipartStreamHeaders, separators);

			//Get Multipart header value for : name
			partBuilderSetHeader("name");
			//Get Multipart header value for : Content-Type:
			partBuilderSetHeader("Content-Type");
			//Get Multipart fileName AND sourceFilename, note that if file data is present a complete sourcefilename will be set later
			partBuilderSetHeader(fileNameString);

			//Get value :
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			multipartStream.readBodyData(stream);
			partBuilderSetValue(stream);

			//Add currentPart to Part List:
			multiPartList.add(currentPartBuilder.build());

			nextPart = multipartStream.readBoundary();
		}
		return multiPartList;
	}

	/**
	 * <p>This method adds the parameter (paramName) from Apache fileupload Map object to the the Part.Builder.</p>
	 * 
	 *
	 */ 
	
	public void partBuilderSetHeader(String paramName) {

		if ( multipartStreamHeadersMap.containsKey(paramName)) {
			if ( paramName.equalsIgnoreCase("name") ) {
				currentPartBuilder.name(multipartStreamHeadersMap.get(paramName));
			}
			else if ( paramName.equalsIgnoreCase("Content-Type")) {
				currentPartBuilder.contentType(multipartStreamHeadersMap.get(paramName));
			}
			else if ( paramName.equalsIgnoreCase(fileNameString)) {
				currentPartBuilder.filename(multipartStreamHeadersMap.get(paramName));
				//note that if file data is present, a file will be re-built and a complete sourcefilename will replace this value:
				currentPartBuilder.sourceFilename(multipartStreamHeadersMap.get(paramName));
			}
			else {
				logger.error("Unsupported parameter name : {} in function PartBuilderSet", paramName);
			}
		}
	}
	
	
	/**
	 * <p>This method adds the data value to the the Part.Builder.value. If the filename is present in the headers and 
	 * data value is present, this data is used to re-build the original file in the Neoload project, Part.Builder.sourcefilename will be set
	 * accordingly </p>
	 * @throws IOException 
	 *
	 */ 
	
	public void partBuilderSetValue(ByteArrayOutputStream stream) throws IOException {

		//fileName is present so we recreate the file in the folder of neoload project if data value is present:
		if (multipartStreamHeadersMap.containsKey(fileNameString)) { 
			if (stream.size()!=0 ) { 
				//recreate file
				String recreatedFileName = "C:\\Users\\v.khatchatrian\\Documents\\NeoLoad Projects\\" + multipartStreamHeadersMap.get(fileNameString);
				String recreatedFileNameRelativeToProject = System.getProperty("file.separator") + multipartStreamHeadersMap.get(fileNameString);
				logger.info("Recreated file {}", recreatedFileName);
				try(OutputStream outputStream = new FileOutputStream(recreatedFileName)) {
				    stream.writeTo(outputStream);
				    currentPartBuilder.sourceFilename(recreatedFileNameRelativeToProject);
				}
			
			}
			else { // stream size is null, hence file can't be re-created
				logger.info("Missing data for {}, file can't be created", multipartStreamHeadersMap.get(fileNameString));
			} 
		}
		else { //Pas de fileName donc on copie les donnees dans PartBuilder.value
			if (stream.size()!=0 ) { 
				currentPartBuilder.value( new String(stream.toByteArray()));
			}
			else { // stream size is null
				currentPartBuilder.value(""); //Otherwise Neoload Writer Optional.get fails
			} 
		}
		
		
	}
	
	
}