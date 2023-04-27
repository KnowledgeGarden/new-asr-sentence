/**
 * 
 */
package org.topicquests.newasr.impl;

import java.sql.ResultSet;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.ISimpleTriple;
import org.topicquests.newasr.api.ITripleQueries;
//import org.topicquests.newasr.api.ITuple;
import org.topicquests.newasr.api.ITupleDataProvicer;
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
public class PostgresTupleDatabase implements ITupleDataProvicer {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;

	/**
	 * 
	 */
	public PostgresTupleDatabase(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getTripleDatabaseDriver();
	}

	@Override
	public IResult putTuple(ISimpleTriple tup) {
	    String sql = ITripleQueries.PUT_TRIPLE;
	    return _putTuple(tup, sql, false);
	}
	
	/**
	 * 
	 * @param tup
	 * @param sql
	 * @param isWorking if {@code true} we don't bother with text objects
	 * @return
	 */
	IResult _putTuple(ISimpleTriple tup, String sql, boolean isWorking) {
		environment.logDebug("PutTuple "+isWorking+"\n"+sql+"\n"+tup.getData());
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    JsonObject data = tup.getData();
	    try { //TODO Transaction?
	    	conn = dbDriver.getConnection();
	    	//(subj_id, pred_id, obj_id, subj_typ, obj_typ, subj_txt, pred_txt, obj_txt & norm_id)
	    	int count = 8;
	    	if (isWorking)
	    		count = 6;
	    	// we do not have id - that's returned
	    	Object [] vals = new Object[count];
	    	vals[0] = new Long(tup.getSubjectId());
	    	vals[1] = new Long(tup.getPredicateId());
	    	vals[2] = new Long(tup.getObjectId());
	    	vals[3] = tup.getSubjectType();
	    	vals[4] = tup.getObjectType();
	    	if (isWorking)
	    		vals[5] = new Long(tup.getNormalizedTripleId());
	    	else {
	    		vals[5] = tup.getSubjectText();
	    		vals[6] = tup.getPredicateText();
	    		vals[7] = tup.getObjectText();
	    	}
	    	IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    if (rs != null) {
		    	if (rs.next())
		    		result.setResultObject(new Long(rs.getLong("id")));
		    }
	    	
	    } catch (Exception e) {
		     result.addErrorString("PDD-Put "+e.getMessage());
		     environment.logError("PDD-Put "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
		environment.logDebug("PutTuple+ "+result.getResultObject());
		return result;
	}

	@Override
	public IResult getTupleById(long id) {
	    String sql = ITripleQueries.NEW_GET_TRIPLE;
	    IResult result = new ResultPojo();
	    IPostgresConnection conn = null;

	    JsonArray foo =null;
	    try { 
	    	conn = dbDriver.getConnection();
	    	Object [] vals = new Object[1];
	    	vals[0] = new Long(id);
	    	
	    	//(subj_id, pred_id, obj_id, subj_typ, obj_typ, subj_txt, pred_txt, obj_txt)
	    	IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
			environment.logDebug("GETTUPLE-1 "+rs);
		    if (rs != null) {
		    	ISimpleTriple st = null;
		    	String tripType;
		    	if (rs.next()) {
	    			environment.logDebug("GETTUPLE-2 ");
	    			st = new ASRSimpleTriple();
		    		st.setId(rs.getLong("id"));
		    		tripType = rs.getString("subj_typ");
		    		//TODO what we do next depends on tripType
		    		// if it's a triple, we do somethng else
		    		st.setSubjectId(rs.getLong("subj_id"), tripType);
		    		st.setSubjectText(rs.getString("subj_txt"));
		    		st.setPredicateId(rs.getLong("pred_id"));
		    		st.setPredicateText(rs.getString("pred_txt"));
		    		tripType = rs.getString("obj_typ");
		    		//TODO what we do next depends on tripType
		    		// if it's a triple, we do somethng else
		    		st.setObjectId(rs.getLong("obj_id"), tripType);
		    		st.setObjectText(rs.getString("obj_txt"));
		    		sql = ITripleQueries.GET_TRIPLE_SENTENCES;
		    		rx = conn.executeSelect(sql, vals);
				    if (rx.hasError())
						result.addErrorString(rx.getErrorString());
				    rs = (ResultSet)rx.getResultObject();
				    if (rs != null) {
				    	while (rs.next())
				    		st.addSentenceId(rs.getLong("sentence_id"));
				    }
		    	}
		    
		    	result.setResultObject(st);
		    }
	    } catch (Exception e) {
		     result.addErrorString("PTD-GT "+id+" "+e.getMessage());
		     environment.logError("PTD-GT "+id+" "+result.getErrorString(), null);
		} finally {
		    conn.closeConnection(result);
		}
		return result;
	}

	@Override
	public IResult addSentenceIdToTuple(long sentenceId, long tupleId) {
	    String sql = ITripleQueries.PUT_SENTENCE_ID;
	    String sql2 =ITripleQueries.GET_TRIPLE_SENTENCE;
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;

	    try { //TODO Transaction?
	    	conn = dbDriver.getConnection();
	    	//(id, sentence_id)
	    	Object [] vals = new Object[2];
	    	vals[0] = new Long(tupleId);
	    	vals[1] = new Long(sentenceId);
	    	IResult rx = conn.executeSelect(sql2, vals);
	    	if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    boolean safe = true;
		    // does this sentenceIdalready exist?
		    if (rs != null) {
		    	if (rs.next())
		    		safe = false;
		    }
		    if (safe) {
		    	rx = conn.executeSQL(sql, vals);
		    	if (rx.hasError())
		    		result.addErrorString(rx.getErrorString());
		    }
	    } catch (Exception e) {
		     result.addErrorString("PTD-3 "+e.getMessage());
		     environment.logError("PTD-3 "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
		return result;	
	}


	@Override
	public IResult listTuples(int start, int count) {
	    String sql = ITripleQueries.LIST_TRIPLES;
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;

	    try {
		    conn = dbDriver.getConnection();
		    //LIMIT=? OFFSET=?
		    Object [] vals = new Object[2];
		    vals[0] = new Integer(count);
		    vals[1] = new Integer(start);
		    IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    if (rs != null) {
		    	JsonArray list = new JsonArray();
		    	result.setResultObject(list);
		    	//(subj_id, pred_id, obj_id, subj_typ, obj_typ, subj_txt, pred_txt, obj_txt)
		    	ISimpleTriple st;
		    	while (rs.next()) {
		    		st = new ASRSimpleTriple();
		    		st.setId(rs.getLong("id"));
		    		st.setSubjectId(rs.getLong("subj_id"), rs.getString("subj_typ"));
		    		st.setSubjectText(rs.getString("subj_txt"));
		    		st.setPredicateId(rs.getLong("pred_id"));
		    		st.setPredicateText(rs.getString("pred_txt"));
		    		st.setObjectId(rs.getLong("obj_id"), rs.getString("obj_typ"));
		    		st.setObjectText(rs.getString("obj_txt"));
		    		list.add(st.getData());
		    	}
		    }
	    } catch (Exception e) {
		     result.addErrorString("PTD-5 "+e.getMessage());
		     environment.logError("PTD-5 "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
	    return result;
	}



	@Override
	public IResult getThisTuple(ISimpleTriple template) {
	    String sql = ITripleQueries.GET_THIS_TRIPLE;
	    return _getThisTuple(template, sql, false);
	}

	/**
	 * Builds everything except sentenceIds
	 * @param template
	 * @param sql
	 * @param isWorking
	 * @return
	 */
	IResult _getThisTuple(ISimpleTriple template, String sql, boolean isWorking) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    environment.logDebug("GetThisTuple "+isWorking+"\n"+sql+"\n"+template.getData());
	    try { //TODO Transaction?
		    conn = dbDriver.getConnection();
			//WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?
		    Object [] vals = new Object[5];
		    vals[0] = new Long(template.getSubjectId());
		    vals[1] = new Long(template.getPredicateId());
		    vals[2] = new Long(template.getObjectId());
		    vals[3] = template.getSubjectType();
		    vals[4] = template.getObjectType();
		    IResult rx = conn.executeSelect(sql, vals);
		    if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		    ResultSet rs = (ResultSet)rx.getResultObject();
		    if (rs != null) {
		    	if (rs.next()) {
		    		ISimpleTriple st = new ASRSimpleTriple(template.getData());
		    		st.setId(rs.getLong("id"));
		    		st.setSubjectId(rs.getLong("subj_id"), rs.getString("subj_typ"));
		    		st.setPredicateId(rs.getLong("pred_id"));
	    			st.setObjectId(rs.getLong("obj_id"), rs.getString("obj_typ"));
		    		if (!isWorking) {
			    		st.setSubjectText(rs.getString("subj_txt"));
		    			st.setPredicateText(rs.getString("pred_txt"));
		    			st.setObjectText(rs.getString("obj_txt"));
		    		} else
		    			st.setNormalizedTripleId(rs.getLong("norm_id"));
			    	result.setResultObject(st);
		    	}
		    }
		    
	    } catch (Exception e) {
		     result.addErrorString("PTD-4 "+e.getMessage());
		     environment.logError("PTD-4 "+result.getErrorString(), e);
		} finally {
		    conn.closeConnection(result);
		}
	    return result;
	}
	@Override
	public IResult putWorkingTuple(ISimpleTriple tup) {
	    String sql = ITripleQueries.PUT_WORKING_TRIPLE;
	    return _putTuple(tup, sql, true);
	}

	@Override
	public IResult getThisWorkingTuple(ISimpleTriple template) {
	    String sql = ITripleQueries.GET_THIS_WORKING_TRIPLE;
	    return _getThisTuple(template, sql, true);
	}
}