/**
 * 
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IQueries;
import org.topicquests.newasr.api.ITripleQueries;
import org.topicquests.newasr.api.ITuple;
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
	public IResult putTuple(ITuple tup) {
	    String sql = ITripleQueries.PUT_TRIPLE;
	    return _putTuple(tup, sql);
	}
	
	IResult _putTuple(ITuple tup, String sql) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    JsonObject data = tup.getData();
	    long objectId=tup.getId();
	    JsonArray foo =null;
	    try { //TODO Transaction?
		      conn = dbDriver.getConnection();
		// TODO Auto-generated method stub
	    } catch (Exception e) {
		      result.addErrorString("PDD-4 "+objectId+" "+e.getMessage());
		      environment.logError("PDD-5 "+objectId+" "+result.getErrorString(), null);
		} finally {
		    	conn.closeConnection(result);
		}
		return result;
		
	}

	@Override
	public IResult getTupleById(long id) {
	    String sql = ITripleQueries.GET_TRIPLE;
		return _getTupleById(id,sql);
	}

	IResult _getTupleById(long id, String sql) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;

	    JsonArray foo =null;
	    try { //TODO Transaction?
		      conn = dbDriver.getConnection();
		// TODO Auto-generated method stub
	    } catch (Exception e) {
		      //result.addErrorString("PDD-4 "+objectId+" "+e.getMessage());
		     // environment.logError("PDD-5 "+objectId+" "+result.getErrorString(), null);
		} finally {
		    	conn.closeConnection(result);
		}
		return result;
	}

	@Override
	public IResult getTupleByPSI(String psi) {
	    String sql = ITripleQueries.GET_TRIPLE_BY_PSI;
		return _getTupleByPSI(psi,sql);
	}

	IResult _getTupleByPSI(String psi, String sql) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;

	    JsonArray foo =null;
	    try { //TODO Transaction?
		      conn = dbDriver.getConnection();
		// TODO Auto-generated method stub
	    } catch (Exception e) {
		      //result.addErrorString("PDD-4 "+objectId+" "+e.getMessage());
		     // environment.logError("PDD-5 "+objectId+" "+result.getErrorString(), null);
		} finally {
		    	conn.closeConnection(result);
		}
		return result;
	}
	@Override
	public IResult addSentenceIdToTuple(long sentenceId, long tupleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult getTupleBySubjectTypeAndId(String type, long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult listTuples(int start, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult putWorkingTuple(ITuple tup) {
	    String sql = ITripleQueries.PUT_WORKING_TRIPLE;
	    return _putTuple(tup, sql);
	}

	@Override
	public IResult getWorkingTupleById(long id) {
	    String sql = ITripleQueries.GET_WORKING_TRIPLE;
		return _getTupleById(id,sql);
	}

	@Override
	public IResult getWorkingTupleByPSI(String psi) {
	    String sql = ITripleQueries.GET_WORKING_TRIPLE_BY_PSI;
		return _getTupleByPSI(psi, sql);
	}
}