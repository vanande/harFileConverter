package com.neotys.draft.harfileconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.MultipartStream;

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
        	String multipartStreamHeaders = multipartStream.readHeaders();
      	
        	//Get Multipart name:
        	String name = ParameterExtractor.extract(multipartStreamHeaders, "name=");
	        if ( !name.equals("") ) { 
	        	currentPartBuilder.name(name.substring(1, name.length()-1)); //substring to remove first and last double quote
	        	//logger.info("name=" + name.substring(1, name.length()-1));
	        } 
	        //Get Multipart Content-Type:
	        String contentType = ParameterExtractor.extract(multipartStreamHeaders, "Content-Type: ");
	        if ( !contentType.equals("") ) { 
	        	currentPartBuilder.contentType(contentType);
	        	//logger.info("contentType=" + contentType);
	        }
	        //Get Multipart fileName:
	        String fileName = ParameterExtractor.extract(multipartStreamHeaders, "filename=");
	        if ( !fileName.equals("") ) { 
	        	currentPartBuilder.filename(fileName);
	        	//logger.info("fileName=" + fileName);
	        }
            
            
	        //Get the value :
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
            multipartStream.readBodyData(stream);

            if (stream.size()!=0 ) { 
            	if (fileName.equals("")) { //TODO : if no filename => exclude the binary files for now...
            		currentPartBuilder.value( new String(stream.toByteArray())); 
            	}
            	else { 
            		currentPartBuilder.value( new String("fake for test"));
            	}
            	
            }
           
            //Add currentPart to Part List:
            multiPartList.add(currentPartBuilder.build());

            nextPart = multipartStream.readBoundary();
        }
        return multiPartList;
    }
}

