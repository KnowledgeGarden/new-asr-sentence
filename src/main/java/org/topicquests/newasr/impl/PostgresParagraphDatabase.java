/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import java.sql.ResultSet;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.api.IParagraphDataProvider;
import org.topicquests.newasr.api.IParagraphQueries;
import org.topicquests.newasr.api.ISentenceQueries;
import org.topicquests.newasr.api.ITripleQueries;
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
public class PostgresParagraphDatabase implements IParagraphDataProvider {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;
	private JsonUtil util;

	/**
	 * 
	 */
	public PostgresParagraphDatabase(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getDatabaseDriver();
		util = new JsonUtil();
	}

	@Override
	public IResult putParagraph(IParagraph p) {
		String sql = IParagraphQueries.PUT_PARAGRAPH;
		environment.logDebug("PutParagraph\n"+sql+"\n"+p.getData());
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    try { //TODO Transaction?
	    	conn = dbDriver.getConnection();
	    	//doc_id, data)
	    	int count = 2;
	    	//JsonObject jo = null;
	    	// we do not have id - that's returned
	    	Object [] vals = new Object[count];
	    	vals[0] = new Long(p.getDocumentId());
	    	vals[1] = p.getData().toString();
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
	public IResult getParagraph(long id) {
		String sql = IParagraphQueries.GET_PARAGRAPH; 
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
		    		IParagraph p = new ASRParagraph(jo);
		    		result.setResultObject(p);
		    	}
		    }
	    
	    } catch (Exception e) {
		     result.addErrorString("PPD-GT "+id+" "+e.getMessage());
		     environment.logError("PPD-GT "+id+" "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
		return result;
	}

	@Override
	public IResult updateParagraph(IParagraph p) {
		String sql = IParagraphQueries.UPDATE_PARAGRAPH;
		environment.logDebug("UpdateParagraph\n"+sql+"\n"+p.getData());
	    IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    long id = p.getId();
	    try { 
	    	conn = dbDriver.getConnection();
	    	Object [] vals = new Object[2];
	    	vals[0] = p.getData().toString();
	    	vals[1] = new Long(id);
	    	
	    	IResult rx = conn.executeSQL(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
	    } catch (Exception e) {
		     result.addErrorString("PPD-US "+id+" "+e.getMessage());
		     environment.logError("PPD-US "+id+" "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
		return result;	
	}

}
