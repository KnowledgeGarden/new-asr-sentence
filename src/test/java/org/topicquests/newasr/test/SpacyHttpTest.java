/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.test;


import org.topicquests.newasr.util.HttpClient;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
public class SpacyHttpTest extends TestingRoot {
	private HttpClient client;
	private final String
		URL		= "http://127.0.0.1:8000",
		S1		= "Many scientists believe that Carbon dioxide causes climate change";
	/**
	 * 
	 */
	public SpacyHttpTest() {
		super();
		client = new HttpClient(environment);
		JSONObject jo = new JSONObject();
		jo.put("text", S1);
		try {
			IResult r = client.post(URL, jo.toJSONString());//URLEncoder.encode(jo.toJSONString(), "UTF-8"));
			System.out.println("A "+r.getErrorString());
			System.out.println("B "+r.getResultObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
		environment.shutDown();
		System.exit(0);
	}

}
