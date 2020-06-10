package com.neotys.draft.harfileconverter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.neotys.neoload.model.v3.project.userpath.Part;

class MultipartAnalyzerTest {

	/*
	@Test
	void testReturnParts() {
		fail("Not yet implemented");

	}*/
	@Test
	void testReturnParts_3SimpleParts() {
		// Arrange
		String multipartBodyString = 
				"----99\r\nContent-Disposition: form-data; name=\"caseMinimal\"\r\n\r\nminimal\r\n"
				+ "----99\r\nContent-Disposition: form-data; name=\"caseFileName\"; filename=\"myImage.png\"\r\nContent-Type: image/png\r\n\r\nnoMatter\r\n"
				+ "----99\r\nContent-Disposition: form-data; name=\"caseValueEmpty\"; Content-Type: text/plain\r\n\r\n\r\n"
				+ "----99--\r\n";

		String boundary = "--99";
		
		List<Part> multiPartListExpected = new ArrayList<>();
		Part part1 = Part.builder().name("caseMinimal").value("minimal").build();
		multiPartListExpected.add(part1);
		Part part2 = Part.builder().name("caseFileName").filename("myImage.png").sourceFilename("myImage.png").contentType("image/png").build();
		multiPartListExpected.add(part2);
		Part part3 = Part.builder().name("caseValueEmpty").contentType("text/plain").value("").build();
		multiPartListExpected.add(part3);
		

		List<Part> multiPartListToCheck = null;
		MultipartAnalyzer multipartAnalyzer = new MultipartAnalyzer(multipartBodyString,boundary);

		//Act	
		try {
			multiPartListToCheck = multipartAnalyzer.returnParts();
		} catch (IOException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
		// Assert
		assertEquals(multiPartListExpected,multiPartListToCheck);
	}

}