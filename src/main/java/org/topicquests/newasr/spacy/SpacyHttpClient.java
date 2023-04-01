/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.spacy;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.util.HttpClient;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class SpacyHttpClient extends HttpClient {
	private final String SERVER_URL;
	/**
	 * @param env
	 */
	public SpacyHttpClient(ASREnvironment env) {
		super(env);
		SERVER_URL = environment.getStringProperty("SpacyURL");
	}
	
	public IResult processSentence(String sentence) {
		JSONObject jo = new JSONObject();
		jo.put("text", sentence);

		IResult result = post(SERVER_URL, jo.toJSONString());
		return result;
	}

}
