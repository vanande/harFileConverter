package com.neotys.draft.harfileconverter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

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
			assertEquals(expectedProject.getServers(),ProjectUnderTest.getServers(), "Servers are different from expected");
			assertEquals(expectedProject.getUserPaths(),ProjectUnderTest.getUserPaths(), "UserPath is different from expected");
			assertEquals(expectedProject,ProjectUnderTest, "Project is different from expected");
			
		
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
			assertEquals(expectedProject.getServers(),ProjectUnderTest.getServers(), "Servers are different from expected");
			assertEquals(expectedProject.getUserPaths(),ProjectUnderTest.getUserPaths(), "UserPath is different from expected");
			assertEquals(expectedProject,ProjectUnderTest, "Project is different from expected");
		
		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}

	}
	
	@Test
	void testPostFormFile() {
		//ARRANGE
		String HARFileNameToTest = "PostFormFile.har";

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
		expectedRequestBuilder.addHeaders(Header.builder().name("Content-Type").value("application/x-www-form-urlencoded").build());
		
		expectedRequestBuilder.body("{\"requests\":[{\"indexName\":\"PROD_learning-content_FR\",\"params\":\"query=&page=0&highlightPreTag=%3Cais-highlight-0000000000%3E&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&facets=%5B%5D&tagFilters=\"}]}");


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
			assertEquals(expectedProject.getServers(),ProjectUnderTest.getServers(), "Servers are different from expected");
			assertEquals(expectedProject.getUserPaths(),ProjectUnderTest.getUserPaths(), "UserPath is different from expected");
			assertEquals(expectedProject,ProjectUnderTest, "Project is different from expected");
		
		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}
	
	
	
	@Test
	void testPostMultiPartFile() {
		//ARRANGE
		String HARFileNameToTest = "PostMultiPartFile.har";

		Server expectedServer = Server.builder()
				.name("jack.intranet.neotys.com")
				.host("jack.intranet.neotys.com")
				.port("9090")
				.scheme(Server.Scheme.valueOf("HTTP"))
				.build();

		Request.Builder expectedRequestBuilder = Request.builder()
				.name("/loadtest/multipart/result.jsp")
				.url("http://jack.intranet.neotys.com:9090/loadtest/multipart/result.jsp")
				.method("POST")
				.server("jack.intranet.neotys.com");

		expectedRequestBuilder.addHeaders(Header.builder().name("Host").value("jack.intranet.neotys.com:9090").build());
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
			assertEquals(expectedProject.getServers(),ProjectUnderTest.getServers(), "Servers are different from expected");
			assertEquals(expectedProject.getUserPaths(),ProjectUnderTest.getUserPaths(), "UserPath is different from expected");
			assertEquals(expectedProject,ProjectUnderTest, "Project is different from expected");
		
		} catch (HarReaderException e) {
			fail("Exception has been thrown : " + e.getMessage());
		}
	}
	
	
	
	
}
