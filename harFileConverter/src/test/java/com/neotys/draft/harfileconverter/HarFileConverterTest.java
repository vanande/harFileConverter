package com.neotys.draft.harfileconverter; 


import static org.junit.jupiter.api.Assertions.*; 

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neotys.neoload.model.v3.project.Project;
import com.neotys.neoload.model.v3.project.server.Server;
import com.neotys.neoload.model.v3.project.userpath.Container;
import com.neotys.neoload.model.v3.project.userpath.Header;
import com.neotys.neoload.model.v3.project.userpath.ImmutableRequest;
import com.neotys.neoload.model.v3.project.userpath.Request;
import com.neotys.neoload.model.v3.project.userpath.UserPath;

import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;
import de.sstoehr.harreader.model.HarPostData;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;

class HarFileConverterTest { 
	static final Logger logger = LoggerFactory.getLogger(HarFileConverterMain.class);
	ClassLoader classLoader = getClass().getClassLoader();

	@Test 
	void testMethod_returnProject_invalidHARFile() {
		//ARRANGE
		String HARFileNameToTest = "invalidFile.har";
		//ACT
		File harSelectedFile = new File(classLoader.getResource(HARFileNameToTest).getFile());
		//ASSERT
		HarFileConverter harFileConverter = new HarFileConverter();
		assertThrows(HarReaderException.class , () -> harFileConverter.returnProject(harSelectedFile, "test"));
	}
	
	@Test
	void testMethod_returnProject_1Valid_1Invalid_Entries() {
		//ARRANGE
		String HARFileNameToTest = "1valid_1Invalid_Entries_test.har";

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

		expectedRequestBuilder.addHeaders(Header.builder().name("Host").value("jack.intranet.neotys.com:9090").build());

		//Actions container that contains all pages containers
		Container.Builder expectedActionsContainer = Container.builder().name("Actions").addSteps(expectedRequestBuilder.build());

		UserPath expectedUserPath = UserPath.builder()
				.init(Container.builder().name("Init").build())
				.actions(expectedActionsContainer.build())
				.end(Container.builder().name("End").build())
				.name("Demo User Path")
				.build();

		Project expectedProject = Project.builder()
				.name("test")
				.addUserPaths(expectedUserPath)
				.addServers(expectedServer)
				.build();

		//ACT
		File harSelectedFile = new File(classLoader.getResource(HARFileNameToTest).getFile());
		try {
			HarFileConverter harFileConverter = new HarFileConverter();
			Project ProjectUnderTest = harFileConverter.returnProject(harSelectedFile, "test");
		//ASSERT
			assertEquals(expectedProject.getServers(),ProjectUnderTest.getServers(), "Servers are different from expected");
			assertEquals(expectedProject.getUserPaths(),ProjectUnderTest.getUserPaths(), "UserPath is different from expected");
			assertEquals(expectedProject,ProjectUnderTest, "Project is different from expected");

		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}


	@Test
	void testMethod_buildRequest_GET_PageRef1() {
		String pageRef = "page_1";

		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("header1");
		header1.setValue("1");
		headers.add(header1);

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("GET"));
		harRequest.setUrl("http://jack.intranet.neotys.com:9090/favicon.ico");
		harRequest.setHeaders(headers);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);
		harEntry.setPageref(pageRef);

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/favicon.ico")
				.url("http://jack.intranet.neotys.com:9090/favicon.ico")
				.method("GET")
				.server("jack.intranet.neotys.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("header1").value("1").build());

		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedHashMapContainerBuilderForPages.put(pageRef, Container.builder().name(pageRef));
		ExpectedHashMapContainerBuilderForPages.get(pageRef).addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			ResultHashMapContainerBuilderForPages.put(pageRef, Container.builder().name(pageRef));
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertEquals(ResultActionsContainer.build(),ExpectedActionsContainer.build()); //both need to be empty
			assertEquals(ResultHashMapContainerBuilderForPages.get(pageRef).build(),ExpectedHashMapContainerBuilderForPages.get(pageRef).build());

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

	}



	@Test
	void testMethod_buildRequest_POST_TEXT_() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("Content-type");
		header1.setValue("text/plain");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("fake text");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("https://www.example.com");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest); 

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("")
				.url("https://www.example.com")
				.method("POST")
				.server("www.example.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("Content-type").value("text/plain").build());
		expectedRequestBuilder.body("fake text");

		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertEquals(ResultActionsContainer.build(),ExpectedActionsContainer.build()); //contains data
			assertEquals(0, ResultHashMapContainerBuilderForPages.size());
			assertEquals(0, 0);

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}


	//TODO: Writer format ? Needs url decoding before ? Waiting for Neoload response.
	@Test
	void testMethod_buildRequest_POST_FORM_URLENCODED() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("Content-Type");
		header1.setValue("application/x-www-form-urlencoded");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("{\"requests\":[{\"indexName\":\"PROD_learning-content_FR\",\"params\":\"query=&page=0&highlightPreTag=%3Cais-highlight-0000000000%3E&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&facets=%5B%5D&tagFilters=\"}]}");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("https://api.segment.io/v1/p");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/v1/p")
				.url("https://api.segment.io/v1/p")
				.method("POST")
				.server("api.segment.io");

		expectedRequestBuilder.addHeaders(Header.builder().name("Content-Type").value("application/x-www-form-urlencoded").build());
		expectedRequestBuilder.body("{\"requests\":[{\"indexName\":\"PROD_learning-content_FR\",\"params\":\"query=&page=0&highlightPreTag=%3Cais-highlight-0000000000%3E&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&facets=%5B%5D&tagFilters=\"}]}");

		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertEquals(ResultActionsContainer.build(),ExpectedActionsContainer.build()); //contains data
			assertEquals(0, ResultHashMapContainerBuilderForPages.size());
			assertEquals(0, ExpectedHashMapContainerBuilderForPages.size());

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}


	@Test
	void testMethod_buildRequest_POST_BINARY() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("content-type");
		header1.setValue("application/json");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("{\"2\":{\"1\":[{\"1\":\"19\",\"2\":{\"1\":\"thread-a:r-1233115413788030\",\"2\":{\"3\":{\"1\":{\"1\":\"\",\"3\":\"1591087529\",\"4\":\"thread-a:r-1233115413788030\",\"5\":[{\"1\":\"msg-a:r8149510851038\",\"2\":{\"1\":1,\"2\":\"unknown@gmail.com\",\"3\":\"John Smith\",\"10\":\"unknown@gmail.com\"},\"3\":[{\"1\":1,\"2\":\"\",\"3\":\"john\"}],\"7\":\"1591087529\",\"8\":\"\",\"9\":{\"2\":[{\"1\":0,\"2\":\"<div dir=\\\"ltr\\\"><br></div>\"}],\"7\":1},\"11\":[\"^all\",\"^r\",\"^r_bt\"],\"18\":\"1591087529\",\"36\":{\"6\":0},\"37\":{\"4\":0},\"42\":0,\"43\":{\"1\":0,\"2\":0,\"3\":0,\"4\":0},\"52\":\"s:3665a9284077db64|#msg-a:r8149510851038585|0\"}]}}}}}]},\"3\":{\"1\":1,\"2\":\"2254\",\"5\":{\"2\":0},\"7\":1},\"4\":{\"2\":1,\"3\":\"1591087529\",\"4\":0,\"5\":112},\"5\":2}");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("https://mail.google.com/sync/u/0/i/s?hl=fr&c=9");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/sync/u/0/i/s")
				.url("https://mail.google.com/sync/u/0/i/s?hl=fr&c=9")
				.method("POST")
				.server("mail.google.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("content-type").value("application/json").build());

		String expectedbodyBinary_textFormat = "{\"2\":{\"1\":[{\"1\":\"19\",\"2\":{\"1\":\"thread-a:r-1233115413788030\",\"2\":{\"3\":{\"1\":{\"1\":\"\",\"3\":\"1591087529\",\"4\":\"thread-a:r-1233115413788030\",\"5\":[{\"1\":\"msg-a:r8149510851038\",\"2\":{\"1\":1,\"2\":\"unknown@gmail.com\",\"3\":\"John Smith\",\"10\":\"unknown@gmail.com\"},\"3\":[{\"1\":1,\"2\":\"\",\"3\":\"john\"}],\"7\":\"1591087529\",\"8\":\"\",\"9\":{\"2\":[{\"1\":0,\"2\":\"<div dir=\\\"ltr\\\"><br></div>\"}],\"7\":1},\"11\":[\"^all\",\"^r\",\"^r_bt\"],\"18\":\"1591087529\",\"36\":{\"6\":0},\"37\":{\"4\":0},\"42\":0,\"43\":{\"1\":0,\"2\":0,\"3\":0,\"4\":0},\"52\":\"s:3665a9284077db64|#msg-a:r8149510851038585|0\"}]}}}}}]},\"3\":{\"1\":1,\"2\":\"2254\",\"5\":{\"2\":0},\"7\":1},\"4\":{\"2\":1,\"3\":\"1591087529\",\"4\":0,\"5\":112},\"5\":2}";
		expectedRequestBuilder.bodyBinary(expectedbodyBinary_textFormat.getBytes(StandardCharsets.UTF_8));

		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertTrue(equalsDebugged((ImmutableRequest)ResultActionsContainer.build().getSteps().get(0),(ImmutableRequest)ExpectedActionsContainer.build().getSteps().get(0))); //contains data
			assertTrue(ResultHashMapContainerBuilderForPages.size()==0);
			assertTrue(ExpectedHashMapContainerBuilderForPages.size()==0);

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}	



	@Test
	void testMethod_buildRequest_POST_ContentTypeUnknown() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("content-type");
		header1.setValue("thisIsAnUnknownContentType/superUnknown");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("fake text");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("https://api.segment.io/v1/p");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);

		
		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/v1/p")
				.url("https://api.segment.io/v1/p")
				.method("POST")
				.server("api.segment.io");

		expectedRequestBuilder.addHeaders(Header.builder().name("content-type").value("thisIsAnUnknownContentType/superUnknown").build());
		expectedRequestBuilder.bodyBinary(new String("fake text").getBytes());


		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertTrue(equalsDebugged((ImmutableRequest)ResultActionsContainer.build().getSteps().get(0),(ImmutableRequest)ExpectedActionsContainer.build().getSteps().get(0))); //contains data
			assertTrue(ResultHashMapContainerBuilderForPages.size()==0);
			assertTrue(ExpectedHashMapContainerBuilderForPages.size()==0);

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}


	@Test
	void testMethod_buildRequest_POST_ContentTypeNotFound() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("header1");
		header1.setValue("1");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("fake text");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("https://api.segment.io/v1/p");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/v1/p")
				.url("https://api.segment.io/v1/p")
				.method("POST")
				.server("api.segment.io");

		expectedRequestBuilder.addHeaders(Header.builder().name("header1").value("1").build());
		expectedRequestBuilder.bodyBinary(new String("fake text").getBytes());


		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertTrue(equalsDebugged((ImmutableRequest)ResultActionsContainer.build().getSteps().get(0),(ImmutableRequest)ExpectedActionsContainer.build().getSteps().get(0))); //contains data
			assertTrue(ResultHashMapContainerBuilderForPages.size()==0);
			assertTrue(ExpectedHashMapContainerBuilderForPages.size()==0);

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

	}


	//POST MULTI PART: 
	@Test 
	void testMethod_buildRequest_POST_MultiPart() {
		//Creating harEntry (har-reader object) test object :
		List<HarHeader> headers = new ArrayList<HarHeader>();
		HarHeader header1 = new HarHeader(); 
		header1.setName("Content-Type");
		header1.setValue("multipart/form-data; boundary=---------------------------3619210861134004098289507961");
		headers.add(header1);

		HarPostData harPostData = new HarPostData();
		harPostData.setText("-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"secret\"\r\n\r\nAZERTYUIOPQSDFGHJKLMWXCVBN\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"dynamic\"\r\n\r\n30783779\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"product\"\r\n\r\nMy Product\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"tva\"\r\n\r\n5.5\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"smallPicture\"; filename=\"cross-blue.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\u001a\n\u0000\u0000\u0000\rIHDR\u0000\u0000\u0000\u0012\u0000\u0000\u0000\u0012\b\u0006\u0000\u0000\u0000VÎW\u0000\u0000\u0000\u0006bKGD\u0000\u0000\u0000\u0000\u0000\u0000ùC»\u0000\u0000\u0000\tpHYs\u0000\u0000\u000b\u0013\u0000\u0000\u000b\u0013\u0001\u0000\u0018\u0000\u0000\u0000\u0007tIME\u0007Ö\u0003\u0017\u0011\u00066\u0006Ù»<\u0000\u0000\u0000\u001dtEXtComment\u0000Created with The GIMPïd%n\u0000\u0000\u0003§IDATxÚmËoUe\u0014Åç;·-mo{\u001föI\u001f<,(!¼\u0014DÑ4D£F&jL\u0018&Dÿ\u0000\u001885\u0011£IuÂLDÑ: )>\u0002H\u001bIPÛ\u000e\u0014Ch©\u0001´Ü{{_çï|/\u0007\u0017\u001bP×|¯üöÚÙËsÎ9îÓüBÉé9&f®scq\u0019\u0019\u001b<\u0001í©fvlêeïã\u001bÙ´®úºÄýcxÿ\u0018I¥\u0019¿|\u0013g.HÔ3ÐßA¶¥\u0019ß\u0017T#I¡\u0014pýÖ\u0012b÷<ÆÁvji|ÐH*Í'_M06yÝ[\u0006èiOãpÄÊ¢!\u0014Ê\u0018¤2ä+ü1wõ«Ó\u001c\u001b~5ÝY\u0000\u0004Àøå«M^aß®Az;3\u0018k±A*M\u0018)\"¥¤\"b\u0010¬ímç÷ù%>üüÂ\n_ÈqâÌ%vm~tK\u0013Rjdl\u0010Â#\f\u0015a¬\b¢/\b\"E(\u00152V´eZ¸ðó5Æ&~«\u0019MNÏá\u0004ÙVd¬QÚòÜöõ¼¾o3`©\u0004\u0012ßóxçÀnÞØ¿(Ra¬Ã¯«gôÜ\u0014ÅJ¸8=ÇÚÕm(cJ\u0013kE[ºlª·^Ü6áWvÒMÒÛÞ¶0\t¥B\bÁìBÙw\u0011óåI65\u0010JE5)V%\u001fþD¾\u0014°¦+Ã»ÃC¬ïÉ²T¨ptä,¹å*A¨±Æ\u0018K¾\u0014r{©\b\"\rÎ#1Riª$_\nyÿ³hmH'\u001bQÚptä,wò\u0015d¬ÑÖ¢µÁ9Öj\u0014#\u0007¥ $\bcÊ$r Ù´®DÂ\u0007 .á³õZÆ Æ9±\u0016!<\u0012¾hK5s·P% \t#ÅöG{xsÿV¬u|y~\u0006k\u001do\u001fx½;\u0006°ÆbïXëXUïimDì\u0018ìãæ\u001cÕPR1}­\u001c~íI\u0000FF'ùàÔE>:=\u0001À±CClÞÐ]£1\u0016¥\u0015]Ù\u0016\u0006úÚ\u0011Ï?±(È\u0015+QÌ\\»ËUNæä·S\u0018kùô_8õÝ¯,\u0016*ä\u0001ÆX5h­Ù³m¾Î\f^\u0014+wüä8_|?E&Ý6dc=rx/Lu\u000eç\u001c­Í«(\u0002­åÔßbô½C¤º\u0004Ã¯îfp]\u0007·\u0017óÄJ/\u0005hmÐÚ`¬ÅZ\u001eR6\u001a¥4éd\u0003G\u000e\u000eJ6>øý×n,qüä8?NÍâ\u0004¾/\u0000°¶FS»RmþÎ4G\u000e\u000eñÌ¶:ñþÝG_ÿ0Ãèù)foå(W%Ú:<\u001c«\u001aêè~¨g·oàð§WHþÓG÷k¹\u001cpõÏEn.\u0016¤¢.áM5±¡¿þ®\fÿ§¿\u0001Mñ1ÝH©v¾\u0000\u0000\u0000\u0000IEND®B`\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"bigPicture\"; filename=\"arrow-right-green1.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\u001a\n\u0000\u0000\u0000\rIHDR\u0000\u0000\u0000\u0012\u0000\u0000\u0000\u0012\b\u0006\u0000\u0000\u0000VÎW\u0000\u0000\u0000\u0006bKGD\u0000\u0000\u0000\u0000\u0000\u0000ùC»\u0000\u0000\u0000\tpHYs\u0000\u0000\u000b\u0013\u0000\u0000\u000b\u0013\u0001\u0000\u0018\u0000\u0000\u0000\u0007tIME\u0007Ö\u0003\u0017\u0011\u0007%|Ë£\u0000\u0000\u0000\u001dtEXtComment\u0000Created with The GIMPïd%n\u0000\u0000\u0003IDATxÚKlTe\u0014Çsï´ÎLÓi§oÊ´¥¢-Á\u001a© ÖTPÑ¸ð±'FM@\u0017`F\u0013Æ\u0007qCâB\u000bpÃRM &jR\u0012[¡\u0010B\u001f´RjK;ítæÞï»÷{¸hÀbñÕùå,ÎùG¬µuLe'9{ms3Üð¦ÚÇÁ¥2¦»v\u0007}­O²9ÝI±[¼~È-TÁ\u0001?Ji¡­¶d¢¢¨C>ÈõV\u0018faYòlûìyð5*bw¤\u001cûýKNMôÓ{o7õåilÄ\u0010Ð\u0004\b%\bl¯|²\u0015F¦¯±!q?ú\u000eÓÜ\b\u0003081À©~vwí ¡²\u0016\"Ð\u0002¡}|åáßêJ@\u001425åþà«sÝÞÈÊNrüüQ\u001eíØJy¢P\u000b¤\u0016øÚÇSkgØ{ß§\u0017¥ðÃ\u0002Ò\bª«\u0013\fMÿÌ¿_\u0013½6D<\u0011.K\u0012j0>¾.PP\u001e^X \u0010zX\u00035ñF^Ýü>éX\u000bòP\u0010·,äÄÈ7äÄ\nnÃsÉ\u000f\u001bjÊ(ÅF\"@*P\u0012_û\b-¸)ç©(®¦­¢ÎÔ6~ûk9o\u0016\u0015\tX]\u0015lk|èù«´6uá+\u000fi$\u001bË¶àPDhBU(£\btÈ3lIm'\u001doà¿æÝáW\u0018Y¼@V/pcu¨\u0017®BÄâ+\u001fi\u0002zë_¢&ÞÀQ\u0015«á/xë×\u0019]\u001eÃ\u000b\nD\u001d\\VD\u000eë\u001a¤\u000e°Öð¿° µ%b]¢NhU¬,ñX1ÒHNN\u001c#êÄPZ\u0011\u0010eBB£0ÖràHDK¸éÏóÞð\u001b/\u0011£d¼èÖºí;IS:Ô\u0001C³?!DhÔ\u0002e\f\t·Ý¹qfó3¼9¸±åQt`i¹§¶Ô&­O_°XX¥ |¼Ð§\u0010z\b%\bµFiCWê!Úð<së¼sf/cÙQ´2(Ïá±L\u001fM\u0015\u001bpºê¶òÌ¦\u0017[\"ççñ\u0002\u000f©\u0003Ñ(cÑÆ\u000fòLå®rà=\\?Ö\u0006åC¦¤}=ûÿùµ%o?¼ÎÅ¥aJ.¸`A\u001b¶k2cÁXÑ\u0016% \u0015©çã]GØÕ¾ûÎï\u001f_¼ÂçÃG8=3\u0000\t[\u001cÁ`1vMh\fhe0¾K¦¤·{\u000fÑyâvDþGý¾ãÄ¥oÌSK\u0004D¬KÜ)¡>ÖÌãìëÙOy¬âîy´e?ËÅË\\_A(\"§ªDöT\u0007Í-w=«¿\u0001û\u0007´-c\u0000\u0000\u0000\u0000IEND®B`\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"sameName\"\r\n\r\nsameName1\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"sameName\"\r\n\r\nsameName2\r\n-----------------------------3619210861134004098289507961--\r\n");

		HarRequest harRequest = new HarRequest();
		harRequest.setMethod(HttpMethod.valueOf("POST"));
		harRequest.setUrl("http://jack.intranet.neotys.com:9090/loadtest/multipart/result.jsp");
		harRequest.setHeaders(headers);
		harRequest.setPostData(harPostData);

		HarEntry harEntry = new HarEntry();
		harEntry.setRequest(harRequest);

		//Creating Expected Request (neoload object) Result:
		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/loadtest/multipart/result.jsp")
				.url("http://jack.intranet.neotys.com:9090/loadtest/multipart/result.jsp")
				.method("POST")
				.server("jack.intranet.neotys.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("Content-Type").value("multipart/form-data; boundary=---------------------------3619210861134004098289507961").build());

		//Get the boundary information in the Content-Type Header:
		String expectedBoundary = "---------------------------3619210861134004098289507961";
		MultipartAnalyzer analyseMultipart = new MultipartAnalyzer(
				"-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"secret\"\r\n\r\nAZERTYUIOPQSDFGHJKLMWXCVBN\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"dynamic\"\r\n\r\n30783779\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"product\"\r\n\r\nMy Product\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"tva\"\r\n\r\n5.5\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"smallPicture\"; filename=\"cross-blue.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\u001a\n\u0000\u0000\u0000\rIHDR\u0000\u0000\u0000\u0012\u0000\u0000\u0000\u0012\b\u0006\u0000\u0000\u0000VÎW\u0000\u0000\u0000\u0006bKGD\u0000\u0000\u0000\u0000\u0000\u0000ùC»\u0000\u0000\u0000\tpHYs\u0000\u0000\u000b\u0013\u0000\u0000\u000b\u0013\u0001\u0000\u0018\u0000\u0000\u0000\u0007tIME\u0007Ö\u0003\u0017\u0011\u00066\u0006Ù»<\u0000\u0000\u0000\u001dtEXtComment\u0000Created with The GIMPïd%n\u0000\u0000\u0003§IDATxÚmËoUe\u0014Åç;·-mo{\u001föI\u001f<,(!¼\u0014DÑ4D£F&jL\u0018&Dÿ\u0000\u001885\u0011£IuÂLDÑ: )>\u0002H\u001bIPÛ\u000e\u0014Ch©\u0001´Ü{{_çï|/\u0007\u0017\u001bP×|¯üöÚÙËsÎ9îÓüBÉé9&f®scq\u0019\u0019\u001b<\u0001í©fvlêeïã\u001bÙ´®úºÄýcxÿ\u0018I¥\u0019¿|\u0013g.HÔ3ÐßA¶¥\u0019ß\u0017T#I¡\u0014pýÖ\u0012b÷<ÆÁvji|ÐH*Í'_M06yÝ[\u0006èiOãpÄÊ¢!\u0014Ê\u0018¤2ä+ü1wõ«Ó\u001c\u001b~5ÝY\u0000\u0004Àøå«M^aß®Az;3\u0018k±A*M\u0018)\"¥¤\"b\u0010¬ímç÷ù%>üüÂ\n_ÈqâÌ%vm~tK\u0013Rjdl\u0010Â#\f\u0015a¬\b¢/\b\"E(\u00152V´eZ¸ðó5Æ&~«\u0019MNÏá\u0004ÙVd¬QÚòÜöõ¼¾o3`©\u0004\u0012ßóxçÀnÞØ¿(Ra¬Ã¯«gôÜ\u0014ÅJ¸8=ÇÚÕm(cJ\u0013kE[ºlª·^Ü6áWvÒMÒÛÞ¶0\t¥B\bÁìBÙw\u0011óåI65\u0010JE5)V%\u001fþD¾\u0014°¦+Ã»ÃC¬ïÉ²T¨ptä,¹å*A¨±Æ\u0018K¾\u0014r{©\b\"\rÎ#1Riª$_\nyÿ³hmH'\u001bQÚptä,wò\u0015d¬ÑÖ¢µÁ9Öj\u0014#\u0007¥ $\bcÊ$r Ù´®DÂ\u0007 .á³õZÆ Æ9±\u0016!<\u0012¾hK5s·P% \t#ÅöG{xsÿV¬u|y~\u0006k\u001do\u001fx½;\u0006°ÆbïXëXUïimDì\u0018ìãæ\u001cÕPR1}­\u001c~íI\u0000FF'ùàÔE>:=\u0001À±CClÞÐ]£1\u0016¥\u0015]Ù\u0016\u0006úÚ\u0011Ï?±(È\u0015+QÌ\\»ËUNæä·S\u0018kùô_8õÝ¯,\u0016*ä\u0001ÆX5h­Ù³m¾Î\f^\u0014+wüä8_|?E&Ý6dc=rx/Lu\u000eç\u001c­Í«(\u0002­åÔßbô½C¤º\u0004Ã¯îfp]\u0007·\u0017óÄJ/\u0005hmÐÚ`¬ÅZ\u001eR6\u001a¥4éd\u0003G\u000e\u000eJ6>øý×n,qüä8?NÍâ\u0004¾/\u0000°¶FS»RmþÎ4G\u000e\u000eñÌ¶:ñþÝG_ÿ0Ãèù)foå(W%Ú:<\u001c«\u001aêè~¨g·oàð§WHþÓG÷k¹\u001cpõÏEn.\u0016¤¢.áM5±¡¿þ®\fÿ§¿\u0001Mñ1ÝH©v¾\u0000\u0000\u0000\u0000IEND®B`\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"bigPicture\"; filename=\"arrow-right-green1.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\u001a\n\u0000\u0000\u0000\rIHDR\u0000\u0000\u0000\u0012\u0000\u0000\u0000\u0012\b\u0006\u0000\u0000\u0000VÎW\u0000\u0000\u0000\u0006bKGD\u0000\u0000\u0000\u0000\u0000\u0000ùC»\u0000\u0000\u0000\tpHYs\u0000\u0000\u000b\u0013\u0000\u0000\u000b\u0013\u0001\u0000\u0018\u0000\u0000\u0000\u0007tIME\u0007Ö\u0003\u0017\u0011\u0007%|Ë£\u0000\u0000\u0000\u001dtEXtComment\u0000Created with The GIMPïd%n\u0000\u0000\u0003IDATxÚKlTe\u0014Çsï´ÎLÓi§oÊ´¥¢-Á\u001a© ÖTPÑ¸ð±'FM@\u0017`F\u0013Æ\u0007qCâB\u000bpÃRM &jR\u0012[¡\u0010B\u001f´RjK;ítæÞï»÷{¸hÀbñÕùå,ÎùG¬µuLe'9{ms3Üð¦ÚÇÁ¥2¦»v\u0007}­O²9ÝI±[¼~È-TÁ\u0001?Ji¡­¶d¢¢¨C>ÈõV\u0018faYòlûìyð5*bw¤\u001cûýKNMôÓ{o7õåilÄ\u0010Ð\u0004\b%\bl¯|²\u0015F¦¯±!q?ú\u000eÓÜ\b\u0003081À©~vwí ¡²\u0016\"Ð\u0002¡}|åáßêJ@\u001425åþà«sÝÞÈÊNrüüQ\u001eíØJy¢P\u000b¤\u0016øÚÇSkgØ{ß§\u0017¥ðÃ\u0002Ò\bª«\u0013\fMÿÌ¿_\u0013½6D<\u0011.K\u0012j0>¾.PP\u001e^X \u0010zX\u00035ñF^Ýü>éX\u000bòP\u0010·,äÄÈ7äÄ\nnÃsÉ\u000f\u001bjÊ(ÅF\"@*P\u0012_û\b-¸)ç©(®¦­¢ÎÔ6~ûk9o\u0016\u0015\tX]\u0015lk|èù«´6uá+\u000fi$\u001bË¶àPDhBU(£\btÈ3lIm'\u001doà¿æÝáW\u0018Y¼@V/pcu¨\u0017®BÄâ+\u001fi\u0002zë_¢&ÞÀQ\u0015«á/xë×\u0019]\u001eÃ\u000b\nD\u001d\\VD\u000eë\u001a¤\u000e°Öð¿° µ%b]¢NhU¬,ñX1ÒHNN\u001c#êÄPZ\u0011\u0010eBB£0ÖràHDK¸éÏóÞð\u001b/\u0011£d¼èÖºí;IS:Ô\u0001C³?!DhÔ\u0002e\f\t·Ý¹qfó3¼9¸±åQt`i¹§¶Ô&­O_°XX¥ |¼Ð§\u0010z\b%\bµFiCWê!Úð<së¼sf/cÙQ´2(Ïá±L\u001fM\u0015\u001bpºê¶òÌ¦\u0017[\"ççñ\u0002\u000f©\u0003Ñ(cÑÆ\u000fòLå®rà=\\?Ö\u0006åC¦¤}=ûÿùµ%o?¼ÎÅ¥aJ.¸`A\u001b¶k2cÁXÑ\u0016% \u0015©çã]GØÕ¾ûÎï\u001f_¼ÂçÃG8=3\u0000\t[\u001cÁ`1vMh\fhe0¾K¦¤·{\u000fÑyâvDþGý¾ãÄ¥oÌSK\u0004D¬KÜ)¡>ÖÌãìëÙOy¬âîy´e?ËÅË\\_A(\"§ªDöT\u0007Í-w=«¿\u0001û\u0007´-c\u0000\u0000\u0000\u0000IEND®B`\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"sameName\"\r\n\r\nsameName1\r\n-----------------------------3619210861134004098289507961\r\nContent-Disposition: form-data; name=\"sameName\"\r\n\r\nsameName2\r\n-----------------------------3619210861134004098289507961--\r\n",
				expectedBoundary);
		try {
			expectedRequestBuilder.parts(analyseMultipart.returnParts());
		} catch (IOException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

		//Store Request in the correct Container:
		Container.Builder ExpectedActionsContainer = Container.builder().name("Actions");
		LinkedHashMap<String,Container.Builder> ExpectedHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
		ExpectedActionsContainer.addSteps(expectedRequestBuilder.build());

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			Container.Builder ResultActionsContainer = Container.builder().name("Actions");
			LinkedHashMap<String,Container.Builder> ResultHashMapContainerBuilderForPages = new LinkedHashMap<String,Container.Builder>();
			harFileConverter.buildRequest(harEntry,ResultActionsContainer,ResultHashMapContainerBuilderForPages);

			//ASSERT:
			assertEquals(ResultActionsContainer.build(),ExpectedActionsContainer.build()); //contains data
			assertTrue(ResultHashMapContainerBuilderForPages.size()==0);
			assertTrue(ExpectedHashMapContainerBuilderForPages.size()==0);

		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}


	@Test
	void testMethod_buildServer() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		HarRequest harRequest1 = new HarRequest();
		harRequest1.setUrl("http://jack.intranet.neotys.com:9090/favicon.ico");
		HarEntry harEntry1 = new HarEntry();
		harEntry1.setRequest(harRequest1);
		
		
		HarRequest harRequest2 = new HarRequest();
		harRequest2.setUrl("https://www.qwant.com/?q=lion&t=web");
		HarEntry harEntry2 = new HarEntry();
		harEntry2.setRequest(harRequest2);
		
		//redundant server:
		HarRequest harRequest3 = new HarRequest();
		harRequest3.setUrl("https://www.qwant.com:443/?q=z%C3%A8bre&t=web");
		HarEntry harEntry3 = new HarEntry();
		harEntry3.setRequest(harRequest3);


		//Creating Expected Request (neoload object) Result:
		Server server1 = Server.builder()
				.name("jack.intranet.neotys.com")
				.host("jack.intranet.neotys.com")
				.port("9090")
				.scheme(Server.Scheme.valueOf("HTTP")) //valueof converts String to equivalent enum value ( HTTP / HTTPS )
				.build();
		
		Server server2 = Server.builder()
				.name("www.qwant.com")
				.host("www.qwant.com")
				.port("443")
				.scheme(Server.Scheme.valueOf("HTTPS")) //valueof converts String to equivalent enum value ( HTTP / HTTPS )
				.build();
		
		List<Server> expectedServers = new ArrayList<>();
		expectedServers.add(server1);
		expectedServers.add(server2);

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			List<Server> resultServers = new ArrayList<>();
			harFileConverter.buildServer(harEntry1, resultServers);
			harFileConverter.buildServer(harEntry2, resultServers);
			harFileConverter.buildServer(harEntry3, resultServers);
			
			//ASSERT:
			assertEquals(resultServers,expectedServers);
			
		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
 
	}
	
	
	
	@Test
	void testMethod_buildContainer() {
		//ARRANGE
		//Creating harEntry (har-reader object) test object :
		HarEntry harEntry1 = new HarEntry();
		harEntry1.setPageref("page_1");
		
		HarEntry harEntry2 = new HarEntry();
		harEntry2.setPageref("page_1"); //duplicate page number
		
		HarEntry harEntry3 = new HarEntry();
		harEntry3.setPageref("page_3");
		
		HarEntry harEntry4 = new HarEntry();
		harEntry4.setPageref(""); //empty page number
		
		HarEntry harEntry5 = new HarEntry(); //No pageRef object

		//Creating Expected Request (neoload object) Result:
		LinkedHashMap<String,Container.Builder> expectedHashMapContainerBuilderForPages = new LinkedHashMap<>(); 
		expectedHashMapContainerBuilderForPages.put("page_1", Container.builder().name("page_1"));
		expectedHashMapContainerBuilderForPages.put("page_3", Container.builder().name("page_3"));

		//ACT:
		HarFileConverter harFileConverter = new HarFileConverter();
		try {
			LinkedHashMap<String,Container.Builder> resultHashMapContainerBuilderForPages = new LinkedHashMap<>(); 
			harFileConverter.buildContainer(harEntry1, resultHashMapContainerBuilderForPages);
			harFileConverter.buildContainer(harEntry2, resultHashMapContainerBuilderForPages);
			harFileConverter.buildContainer(harEntry3, resultHashMapContainerBuilderForPages);
			harFileConverter.buildContainer(harEntry4, resultHashMapContainerBuilderForPages);
			harFileConverter.buildContainer(harEntry5, resultHashMapContainerBuilderForPages);
			
			//ASSERT:
			//no equals defined for Container.Builder, needs to create the Container with build() to access the functional .equals
			assertEquals(expectedHashMapContainerBuilderForPages.get("page_1").build(),resultHashMapContainerBuilderForPages.get("page_1").build()); //page_1
			assertEquals(expectedHashMapContainerBuilderForPages.get("page_3").build(),resultHashMapContainerBuilderForPages.get("page_3").build()); //page_3
			assertEquals(expectedHashMapContainerBuilderForPages.size() , resultHashMapContainerBuilderForPages.size());
			
		} catch (Exception e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
 
	}
 	
	

	/**
	 * <p>This method replaces the equals method from ImmutableRequest (package com.neotys.neoload.model.v3.project.userpath) 
	 * when bodyBinary data is present.	The ImmutableRequest method is currently wrong since {@code  Objects.equals} does
	 *  not work as expected for binary arrays. It should be replaced by :
	 *  {@code Arrays.equal} </p>
	 * 
	 */


	private boolean equalsDebugged(ImmutableRequest req1, ImmutableRequest req2) {
		return req1.getName().equals(req2.getName())
				&& req1.getUrl().equals(req2.getUrl())
				&& req1.getServer().equals(req2.getServer())
				&& req1.getMethod().equals(req2.getMethod())
				&& req1.getHeaders().equals(req2.getHeaders())
				&& req1.getBody().equals(req2.getBody())
				&& equalsForOptional( req1.getBodyBinary(), req2.getBodyBinary())
				&& req1.getParts().equals(req2.getParts())
				&& req1.getExtractors().equals(req2.getExtractors())
				&& req1.getFollowRedirects().equals(req2.getFollowRedirects())
				&& req1.getDescription().equals(req2.getDescription())
				&& req1.getSlaProfile().equals(req2.getSlaProfile());
	}

	private boolean equalsForOptional (Optional<byte[]> obj1, Optional<byte[]> obj2 ){
		if (obj1.isPresent() && obj2.isPresent()) {
			return Arrays.equals( obj1.get(), obj2.get());
		} else if (obj1.isPresent()) {
			return false;
		} else if (obj2.isPresent()) {
			return false;
		} else {
			return true;
		}
	}
}




