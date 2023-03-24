/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.dictionary;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IDictionaryClient;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IEnvironment;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class DictionaryHttpClient implements IDictionaryClient {
	private ASREnvironment environment;
	public final String
		SERVER_URL,
		CLIENT_ID,
		// from IDictionaryServerModel
		VERB			= "verb",
		WORD			= "word",
		GET_TERM_ID		= "getTermId",
		ADD_TERM		= "addTerm",
		//GET_WORD		= "getWord",
		GET_DICTIONARY	= "getDictionary",
		IS_NEW_WORD		= "isNewWord",	// boolean <code>true</code> if is new word
		TEST			= "test",
		ERROR			= "error",
		CARGO			= "cargo"; //return object - wordId or word
	/**
	 * 
	 */
	public DictionaryHttpClient(ASREnvironment env) {
		environment = env;
		String urx = (String)environment.getProperties().get("DictServerURl");
		String port = (String)environment.getProperties().get("DictServerPort");
		CLIENT_ID = (String)environment.getProperties().get("DictServerClientId");
		SERVER_URL = "http://"+urx+":"+port+"/";
	}
	@Override
	public IResult getDictionary() {
		IResult result = new ResultPojo();
		//build query
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"verb\":\"getDictionary\","); // the verb
		buf.append("\"clientId\":\""+CLIENT_ID+"\"}");
		String query = buf.toString();
		environment.logError("SENDING "+query, null);
		try {
			query = URLEncoder.encode(query, "UTF-8");
			getQuery(SERVER_URL+query, result);
		} catch (Exception e) {
			String x = e.getMessage()+" : "+buf.toString();
			environment.logError(x, e);
			result.addErrorString(x);
		}
		return result;	
	}
	
	@Override
	public IResult addTerm(String term) {
		IResult result = new ResultPojo();
		//build query
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"verb\":\"addTerm\","); // the verb
		buf.append("\"term\":\""+term+"\","); // the field
		buf.append("\"clientId\":\""+CLIENT_ID+"\"}");
		String query = buf.toString();
		environment.logDebug("DictionaryHttpClient.addWord "+term+"\n"+query);
		try {
			getQuery(query, result);
		} catch (Exception e) {
			String x = e.getMessage()+" : "+buf.toString();
			environment.logError(x, e);
			result.addErrorString(x);
		}
		return result;	
	}

	/**
	 * Simple HTTP client with a long timeout
	 * @param query
	 * @param result
	 */
	void getQuery(String query, IResult result) {
		
		BufferedReader rd = null;
		HttpURLConnection con = null;
		PrintWriter out = null;
		try {
			URL urx = new URL(SERVER_URL);
			con = (HttpURLConnection) urx.openConnection();
			con.setReadTimeout(500000); //29 seconds for 1m words - leave lots of time
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			OutputStreamWriter bos = new OutputStreamWriter(os, "UTF-8");
			out = new PrintWriter(bos, true);
			out.print(query);
			out.close();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder buf = new StringBuilder();

			String line;
			while ((line = rd.readLine()) != null) {
				buf.append(line + '\n');
			}

			result.setResultObject(buf.toString());
		} catch (Exception var18) {
			var18.printStackTrace();
			result.addErrorString(var18.getMessage());
			environment.logError(var18.getMessage()+"|"+query, var18);
		} finally {
			try {
				if (rd != null) {
					rd.close();
				}

				if (con != null) {
					con.disconnect();
				}
				
			} catch (Exception var17) {
				var17.printStackTrace();
				result.addErrorString(var17.getMessage());
				environment.logError(var17.getMessage(), var17);
			}

		}
	}
}
