/**
 * 
 */
package com.neotys.draft.harfileconverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This class uses a static method to find a specific parameter value in a String (stringToAnalyze) using the parameter name (key) </p>
 * In the following String example:</p>
 *
 * <pre>
 * Content-Disposition: form-data; name="smallPicture"; filename="cross-blue.png"
 * Content-Type: image/png
 * </pre>
 *
 * <p>The return value of {@code extract(stringToAnalyze, "filename=")} is "cross-blue.png"</p>
 * <p>The return value of {@code extract(stringToAnalyze, "Content-Type: ")} is image/png </p>
 * 
 * @author jerome
 *
 */
public final class ParameterExtractor {
	 private ParameterExtractor () { // private constructor
	    }
	 public static String extract(String stringToAnalyze, String key) {
		 
		 Pattern r = Pattern.compile(".*\\b" + key +"(.*?)(;|$).*", Pattern.MULTILINE);
	     Matcher m = r.matcher(stringToAnalyze);
	     if (m.find()) {
	         return m.group(1);
	     }
	     else return "";
	 }

}
