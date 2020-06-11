package com.neotys.draft.harfileconverter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neotys.neoload.model.v3.project.Project;
import com.neotys.neoload.model.v3.project.server.Server;
import com.neotys.neoload.model.v3.project.userpath.Container;
import com.neotys.neoload.model.v3.project.userpath.Header;
import com.neotys.neoload.model.v3.project.userpath.Request;
import com.neotys.neoload.model.v3.project.userpath.UserPath;

import de.sstoehr.harreader.HarReaderException;

class HarFileConverterTest {
	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);
	ClassLoader classLoader = getClass().getClassLoader();

	@Test
	void testInvalidHARFile() {
		//ARRANGE
		String HARFileNameToTest = "invalidFile.har";
		//ACT
		File harSelectedFile = new File(classLoader.getResource(HARFileNameToTest).getFile());
		//ASSERT
		HarFileConverter harFileConverter = new HarFileConverter();
		assertThrows(HarReaderException.class , () -> harFileConverter.returnProject(harSelectedFile));
	}

	
	@Test
	void testSimpleGETFile() {
		//ARRANGE
		String HARFileNameToTest = "simpleGETFile.har";

		Server expectedServer = Server.builder()
				.name("jack.intranet.neotys.com")
				.host("jack.intranet.neotys.com")
				.port("9090")
				.scheme(Server.Scheme.valueOf("HTTP"))
				.build();

		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/favicon.ico")
				.url("http://jack.intranet.neotys.com:9090/favicon.ico")
				.method("GET")
				.server("jack.intranet.neotys.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("header1").value("1").build());
		expectedRequestBuilder.addHeaders(Header.builder().name("header2").value("2").build());

		Container.Builder expectedActionsContainer = Container.builder().name("Actions").addSteps(expectedRequestBuilder.build());

		UserPath expectedUserPath = UserPath.builder()
				.init(Container.builder().name("Init").build())
				.actions(expectedActionsContainer.build())
				.end(Container.builder().name("End").build())
				.name("Demo User Path")
				.build();

		Project expectedProject = Project.builder()
				.name("test_HARFileConverter_Project")
				.addUserPaths(expectedUserPath)
				.addServers(expectedServer)
				.build();

		//ACT
		File harSelectedFile = new File(classLoader.getResource(HARFileNameToTest).getFile());

		//ASSERT
		try {
			HarFileConverter harFileConverter = new HarFileConverter();
			Project ProjectUnderTest = harFileConverter.returnProject(harSelectedFile);
			assertTrue(expectedProject.getServers().equals(ProjectUnderTest.getServers()), "Servers are different from expected");
			assertTrue(expectedProject.getUserPaths().equals(ProjectUnderTest.getUserPaths()), "UserPath is different from expected");
			assertTrue(expectedProject.equals(ProjectUnderTest), "Project is different from expected");
			
		
		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

	}

	
	@Test
	void testPostTextFile() {
		//ARRANGE
		String HARFileNameToTest = "PostTextFile.har";

		Server expectedServer = Server.builder()
				.name("api.segment.io")
				.host("api.segment.io")
				.port("443") //default for https
				.scheme(Server.Scheme.valueOf("HTTPS"))
				.build();

		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/v1/p")
				.url("https://api.segment.io/v1/p")
				.method("POST")
				.server("api.segment.io");

		expectedRequestBuilder.addHeaders(Header.builder().name("header1").value("1").build());
		expectedRequestBuilder.addHeaders(Header.builder().name("header2").value("2").build());
		expectedRequestBuilder.addHeaders(Header.builder().name("Content-Type").value("text/plain").build());
		
		expectedRequestBuilder.body("fake text");


		Container.Builder expectedActionsContainer = Container.builder().name("Actions").addSteps(expectedRequestBuilder.build());

		UserPath expectedUserPath = UserPath.builder()
				.init(Container.builder().name("Init").build())
				.actions(expectedActionsContainer.build())
				.end(Container.builder().name("End").build())
				.name("Demo User Path")
				.build();

		Project expectedProject = Project.builder()
				.name("test_HARFileConverter_Project")
				.addUserPaths(expectedUserPath)
				.addServers(expectedServer)
				.build();

		//ACT
		File harSelectedFile = new File(classLoader.getResource(HARFileNameToTest).getFile());

		//ASSERT
		try {
			HarFileConverter harFileConverter = new HarFileConverter();
			Project ProjectUnderTest = harFileConverter.returnProject(harSelectedFile);
			assertTrue(expectedProject.getServers().equals(ProjectUnderTest.getServers()), "Servers are different from expected");
			assertTrue(expectedProject.getUserPaths().equals(ProjectUnderTest.getUserPaths()), "UserPath is different from expected");
			assertTrue(expectedProject.equals(ProjectUnderTest), "Project is different from expected");
		
		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

	}
	
	
}
