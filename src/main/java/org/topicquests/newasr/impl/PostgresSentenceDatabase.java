/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 */
package org.topicquests.newasr.impl;

import java.sql.ResultSet;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.api.IParagraphQueries;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ISentenceDataProvider;
import org.topicquests.newasr.api.ISentenceQueries;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class PostgresSentenceDatabase implements ISentenceDataProvider {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;
	private JsonUtil util;

	/**
	 * 
	 */
	public PostgresSentenceDatabase(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getDatabaseDriver();
		util = new JsonUtil();
	}

	@Override
	public IResult putSentence(ISentence s) {
		String sql = ISentenceQueries.PUT_SENTENCE;
		environment.logDebug("PutSentence\n"+sql+"\n"+s.getData());
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    try { //TODO Transaction?
	    	conn = dbDriver.getConnection();
	    	//para_id, doc_id, data
	    	int count = 3;
	    	JsonObject jo = null;
	    	// we do not have id - that's returned
	    	Object [] vals = new Object[count];
	    	vals[0] = new Long(s.getParagraphId());
	    	vals[1] = new Long(s.getDocumentId());
	    	vals[2] = s.getData().toString();
	    	IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    if (rs != null) {
		    	if (rs.next())
		    		result.setResultObject(new Long(rs.getLong("id")));
		    }
	    	
	    } catch (Exception e) {
		     result.addErrorString("PPD-Put "+e.getMessage());
		     environment.logError("PPD-Put "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
		environment.logDebug("PutParagraph+ "+result.getResultObject());
		return result;
	}

	@Override
	public IResult getSentence(long id) {
		String sql = ISentenceQueries.GET_SENTENCE; 
	    IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    try { 
	    	conn = dbDriver.getConnection();
	    	Object [] vals = new Object[1];
	    	vals[0] = new Long(id);
	    	
	    	IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    if (rs != null) {
		    	//doc_id, data
		    	if (rs.next()) {
		    		String json = rs.getString("data");
		    		JsonObject jo = util.parse(json);
		    		ISentence p = new ASRSentence(jo);
		    		result.setResultObject(p);
		    	}
		    }
	    
	    } catch (Exception e) {
		     result.addErrorString("PSD-GT "+id+" "+e.getMessage());
		     environment.logError("PDD-GT "+id+" "+result.getErrorString(), null);
		} finally {
		    conn.closeConnection(result);
		}
		return result;
	}

	@Override
	public IResult updateSentence(ISentence s) {
		String sql = ISentenceQueries.UPDATE_SENTENCE;
	    IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    long id = s.getId();
	    try { 
	    	conn = dbDriver.getConnection();
	    	Object [] vals = new Object[2];
	    	vals[0] = new Long(id);
	    	vals[1] = s.getData().getAsString();
	    	
	    	IResult rx = conn.executeSQL(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
	    } catch (Exception e) {
		     result.addErrorString("PSD-US "+id+" "+e.getMessage());
		     environment.logError("PDD-US "+id+" "+result.getErrorString(), null);
		} finally {
		    conn.closeConnection(result);
		}
		return result;
	}

}
