package com.neotys.draft.harfileconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 */

public class MultipartAnalyzer {

	public ByteArrayInputStream content;
	byte[] boundary;
	static final Logger logger = LoggerFactory.getLogger(MultipartAnalyzer.class);
	
    public MultipartAnalyzer(String MULTIPART_BODY, String boundaryString) {
    	
        boundary = boundaryString.getBytes();
        content = new ByteArrayInputStream(MULTIPART_BODY.getBytes());
    }
        
    public List<Part> returnParts() throws Exception {

    	List<Part> multiPartList = new ArrayList<>();
    	
        @SuppressWarnings("deprecation")
        MultipartStream multipartStream = new MultipartStream(content, boundary);
        
        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
        	Part.Builder currentPartBuilder = Part.builder();
        	//need to replace CRLF by ';' because org.apache.commons.fileupload ParameterParser does not understand CRLF as 
        	String multipartStreamHeaders = multipartStream.readHeaders().replaceAll("\r\n", ";").replaceAll(": ", "="); 
        	System.out.println(multipartStreamHeaders);
        	ParameterParser multiPartParamParser = new ParameterParser();
        	
        	char[] separators = {';'};
        	Map<String, String> multipartStreamHeadersMap = multiPartParamParser.parse(multipartStreamHeaders, separators);
        	
        	System.out.println(multipartStreamHeadersMap);
        	
//TODO : remove this code block and use the ParameterParser from org.apache.commons.fileupload : 
        	//Get Multipart name:
        	String name = ParameterExtractor.extract(multipartStreamHeaders, "name=");
	        if ( !name.equals("") ) { 
	        	currentPartBuilder.name(name.substring(1, name.length()-1)); //substring to remove first and last double quote
	        	logger.info("name=" + name.substring(1, name.length()-1));
	        } 
	        //Get Multipart Content-Type:
	        String contentType = ParameterExtractor.extract(multipartStreamHeaders, "Content-Type: ");
	        if ( !contentType.equals("") ) { 
	        	currentPartBuilder.contentType(contentType);
	        	logger.info("contentType=" + contentType);
	        }
	        //Get Multipart fileName:
	        String fileName = ParameterExtractor.extract(multipartStreamHeaders, "filename=");
	        if ( !fileName.equals("") ) { 
	        	currentPartBuilder.filename(fileName.substring(1, fileName.length()-1));
	        	logger.info("fileName=" + fileName.substring(1, fileName.length()-1));
	        }
           
	        //Get the value :
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();

            multipartStream.readBodyData(stream);

            
            if (stream.size()!=0 ) { 
            	if (fileName.equals("")) { //TODO : if no filename => exclude the binary files for now...
            		currentPartBuilder.value( new String(stream.toByteArray())
            				.replaceAll("<", "&lt;") //The < must be escaped with a &lt;
            				.replaceAll("&", "&amp;") //The & must be escaped with a &amp;
            				.replaceAll(">", "&gt;") //The > should be escaped with &gt;
            				.replaceAll("'", "&apos;") //The ' should be escaped with a &apos;
            				.replaceAll("\"", "&quot;") //The " should be escaped with a &quot;
            		);
            		logger.info("value=" + new String(stream.toByteArray())
            				.replaceAll("<", "&lt;") //The < must be escaped with a &lt;
            				.replaceAll("&", "&amp;") //The & must be escaped with a &amp;
            				.replaceAll(">", "&gt;") //The > should be escaped with &gt;
            				.replaceAll("'", "&apos;") //The ' should be escaped with a &apos;
            				.replaceAll("\"", "&quot;") //The " should be escaped with a &quot;
            		);
            	}
            	else { 
            		currentPartBuilder.value( new String("fake for test"));
            	}	
            }
            else { // stream size is null
            	currentPartBuilder.value( new String("")); //Otherwise Neoload Writer Optional.get fails
            } 
            
            //TEMP FOR TEST :
            //currentPartBuilder.value( new String("fake for test"));
            
            
            //Add currentPart to Part List:
            multiPartList.add(currentPartBuilder.build());
            
            nextPart = multipartStream.readBoundary();
        }
        return multiPartList;
    }
}

