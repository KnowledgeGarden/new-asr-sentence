/**
 * 
 */
package org.topicquests.newasr.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author jackpark
 *
 */
public class JsonUtil {

	/**
	 * 
	 */
	public JsonUtil() {
	}

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws Exception
	 */
	public JsonObject parse(final String jsonString) throws Exception {
		return (JsonObject)JsonParser.parseString(jsonString);
	}

}
