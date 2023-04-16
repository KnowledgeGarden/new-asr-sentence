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
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    JsonObject data = tup.getData();
	    long objectId=tup.getId();
	    JsonArray foo =null;
	    try { //TODO Transaction?
		      conn = dbDriver.getConnection();
		      String sql = ITripleQueries.PUT_TRIPLE;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult getTupleByPSI(String psi) {
		// TODO Auto-generated method stub
		return null;
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

}
