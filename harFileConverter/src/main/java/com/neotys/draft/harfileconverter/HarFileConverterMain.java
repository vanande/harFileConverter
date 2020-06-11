package com.neotys.draft.harfileconverter;


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

public class HarFileConverterMain { 

	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);
	
	public static void main(String[] args) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTTP Archive(.har)", "har");
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File harSelectedFile = fileChooser.getSelectedFile();
			try {
				HarFileConverter harFileConverter = new HarFileConverter();
				Project project = harFileConverter.returnProject(harSelectedFile);
				NeoLoadWriter writer = new NeoLoadWriter(project,"C:\\Users\\jerome\\Documents\\NeoLoad Projects");
				writer.write(true, "7.0", "7.2.2");
				
			} catch (HarReaderException e) {
				logger.error("File conversion has failed : {} " ,  harSelectedFile.getAbsolutePath());
				logger.error("Cause = {} " , e.getMessage());
			}
		}
	}
}
