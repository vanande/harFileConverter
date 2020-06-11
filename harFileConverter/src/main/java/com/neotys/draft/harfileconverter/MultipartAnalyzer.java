package com.neotys.draft.harfileconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	
    public MultipartAnalyzer(String multipartBodyString, String boundaryString) {
    	
        boundary = boundaryString.getBytes();
        content = new ByteArrayInputStream(multipartBodyString.getBytes());
    }
        
    public List<Part> returnParts() throws IOException  {

    	List<Part> multiPartList = new ArrayList<>();
    	
        @SuppressWarnings("deprecation")
        MultipartStream multipartStream = new MultipartStream(content, boundary);
        
        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
        	Part.Builder currentPartBuilder = Part.builder();

        	//need to replace CRLF by ';' because org.apache.commons.fileupload ParameterParser does not understand CRLF as 
        	String multipartStreamHeaders = multipartStream.readHeaders().replaceAll("\r\n", ";").replace(": ", "="); 
        	ParameterParser multiPartParamParser = new ParameterParser();
        	
        	char[] separators = {';'};
        	Map<String, String> multipartStreamHeadersMap = multiPartParamParser.parse(multipartStreamHeaders, separators);
        	
        	String paramName = "";
        	//Get Multipart name value:
        	paramName = "name";
        	if ( multipartStreamHeadersMap.containsKey(paramName)) {
        		currentPartBuilder.name(multipartStreamHeadersMap.get(paramName));
        	}
        	
        	//Get Multipart name Content-Type:
        	paramName = "Content-Type";
        	if ( multipartStreamHeadersMap.containsKey(paramName)) {
        		currentPartBuilder.contentType(multipartStreamHeadersMap.get(paramName));
        	}
        	 //Get Multipart fileName:
        	paramName = "filename";
        	if ( multipartStreamHeadersMap.containsKey(paramName)) {
        		currentPartBuilder.filename(multipartStreamHeadersMap.get(paramName));
        		//sourceFilename is used by PartWriter (v3.writers.neoload.userpath) to detect if data is text or file :
        		currentPartBuilder.sourceFilename(multipartStreamHeadersMap.get(paramName));
        	}
        	
        	//Get value :
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
            multipartStream.readBodyData(stream);
            //Pas de fileName donc on copie les donnees dans PartBuilder.value :
            if (!multipartStreamHeadersMap.containsKey("filename")) {
            	if (stream.size()!=0 ) { 
            		currentPartBuilder.value( new String(stream.toByteArray()));
            	}
            	else { // stream size is null
                	currentPartBuilder.value(""); //Otherwise Neoload Writer Optional.get fails
                } 
            }
            
            //Add currentPart to Part List:
            multiPartList.add(currentPartBuilder.build());
            
            nextPart = multipartStream.readBoundary();
        }
        return multiPartList;
    }
}

