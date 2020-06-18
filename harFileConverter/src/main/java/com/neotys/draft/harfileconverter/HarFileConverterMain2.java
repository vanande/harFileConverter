package com.neotys.draft.harfileconverter;


import com.neotys.neoload.model.listener.EventListener;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * <p>Main 2 function to test HarFileConverter Class, this one will execute the har
 *  conversion on all files that are in a specific folder</p>
 * 
 */ 

public class HarFileConverterMain2 implements EventListener { 

	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain2.class);
	
	int nbActionsRequestSuccessDecoding =0;
	int nbActionsRequestFailDecoding =0;
	
	
	//Constructor :
	public HarFileConverterMain2() {
		// Do nothing, we need an instantiated Object harFileConverterMain to use as EventListener 
		// for the HarFileConverter constructor 
	}

	
	public static void main(String[] args) {
	
		File folderHAR = new File("C:\\Users\\jerome\\Downloads\\");
		File[] listHarFiles = folderHAR.listFiles((dir, name) -> name.toLowerCase().endsWith(".har"));
		
		Stream<File> stream1 = Arrays.stream(listHarFiles);
        
		stream1.forEach(currentHarFile -> {
		
			HarFileConverterMain2 harFileConverterMain = new HarFileConverterMain2();
			HarFileConverter harFileConverter = new HarFileConverter ( harFileConverterMain ); //constructor with EventListener
			harFileConverter.writeProject(currentHarFile, new File("C:\\Users\\jerome\\Documents\\NeoLoad Projects\\" + "test.nlp"));
			
		});
	}


	@Override
	public void startReadingScripts(int totalScriptNumber) {
		// do nothing
		
	}


	@Override
	public void endReadingScripts() {
		// do nothing
		
	}


	@Override
	public void startScript(String scriptPath) {
		logger.info("Selected HAR file: {}" , scriptPath);
		
	}


	@Override
	public void endScript() {
		logger.info("End processing HAR File.\nRequest Decoding : success = {} / failures = {}",
				nbActionsRequestSuccessDecoding,
				nbActionsRequestFailDecoding);
		
	}


	@Override
	public void readSupportedAction(String actionName) {
		nbActionsRequestSuccessDecoding++;

		
	}


	@Override
	public void readUnsupportedAction(String actionName) {
		nbActionsRequestFailDecoding++;
		
	}


	@Override
	public void readSupportedFunction(String scriptName, String functionName, Integer lineNumber) {
		// do nothing
		
	}


	@Override
	public void readSupportedFunctionWithWarn(String scriptName, String functionName, Integer lineNumber,
			String warning) {
		// do nothing
		
	}


	@Override
	public void readUnsupportedFunction(String scriptName, String functionName, Integer lineNumber) {
		// do nothing
		
	}


	@Override
	public void readSupportedParameter(String scriptName, String parameterType, String parameterName) {
		// do nothing
		
	}


	@Override
	public void readSupportedParameterWithWarn(String scriptName, String parameterType, String parameterName,
			String warning) {
		// do nothing
		
	}


	@Override
	public void readUnsupportedParameter(String scriptName, String parameterType, String parameterName) {
		// do nothing
		
	}
}
