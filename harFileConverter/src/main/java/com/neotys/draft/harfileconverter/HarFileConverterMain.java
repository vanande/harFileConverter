package com.neotys.draft.harfileconverter;


import com.neotys.neoload.model.listener.EventListener;
import com.neotys.neoload.model.v3.project.Project;
import com.neotys.neoload.model.v3.writers.neoload.NeoLoadWriter;

import de.sstoehr.harreader.HarReaderException;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * <p>Main function to test HarFileConverter Class</p>
 * 
 */  

public class HarFileConverterMain implements EventListener { 

	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);
	
	int nbActionsRequestSuccessDecoding =0;
	int nbActionsRequestFailDecoding =0;
	
	
	//Constructor :
	public HarFileConverterMain() {
		// Do nothing, we need an instantiated Object harFileConverterMain to use as EventListener 
		// for the HarFileConverter constructor 
	}

	
	public static void main(String[] args) {

		HarFileConverterMain harFileConverterMain = new HarFileConverterMain();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTTP Archive(.har)", "har");
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File harSelectedFile = fileChooser.getSelectedFile();
			try {
				HarFileConverter harFileConverter = new HarFileConverter ( harFileConverterMain );
				Project project = harFileConverter.returnProject(harSelectedFile);
				NeoLoadWriter writer = new NeoLoadWriter(project,"C:\\Users\\jerome\\Documents\\NeoLoad Projects");
				writer.write(true, "7.0", "7.2.2");
				
			} catch (HarReaderException e) {
				logger.error("File conversion has failed : {} " ,  harSelectedFile.getAbsolutePath());
				logger.error("Cause = {} " , e.getMessage());
			}
		}
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
