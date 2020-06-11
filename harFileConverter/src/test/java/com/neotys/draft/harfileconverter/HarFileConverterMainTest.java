package com.neotys.draft.harfileconverter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sstoehr.harreader.HarReaderException;

class HarFileConverterMainTest {
	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);
	
	
	@Test
	void testInvalidHARFile() {

		ClassLoader classLoader = getClass().getClassLoader();
		File harSelectedFile = new File(classLoader.getResource("invalidFile.har").getFile());
		assertThrows(HarReaderException.class , () -> HarFileConverterMain.run(harSelectedFile,"noMatter"));

	}
	
	
	@Test
	void testMissingMultipartBoundary() {

		ClassLoader classLoader = getClass().getClassLoader();
		File harSelectedFile = new File(classLoader.getResource("invalidFile.har").getFile());
		assertThrows(HarReaderException.class , () -> HarFileConverterMain.run(harSelectedFile,"noMatter"));

	}
	

}
