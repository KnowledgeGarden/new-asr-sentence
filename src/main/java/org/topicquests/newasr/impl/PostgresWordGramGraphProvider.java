/**
 * 
 */
package org.topicquests.newasr.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Iterator;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrDataProvider;
import org.topicquests.newasr.api.IConstants;
import org.topicquests.newasr.api.IQueries;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class PostgresWordGramGraphProvider implements IAsrDataProvider {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;

	/**
	 * 
	 */
	public PostgresWordGramGraphProvider(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getDatabaseDriver();
	}
	@Override
	public IResult putNode(IWordGram node) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    JsonObject data = node.getData();
	    long objectId=node.getId();
	    JsonArray foo =null;
	    try { //TODO Transaction?
		      conn = dbDriver.getConnection();
		      String sql = IQueries.PUT_NODE;
		      //(id, words, pos, topicid, dbpedia wikidata, tense, negation, epi, active, cannon)
		      Object [] vals = new Object[11];
		      vals[0] = new Long(objectId);			//id
		      vals[1] = node.getWords();			//wods
		      JsonArray x = node.listPOS();
		      String ts = jsonArrayToCommaString(x);
		      vals[2] = ts; 						// pos might be null;
		      x = node.listTopicLocators();
		      ts = jsonArrayToCommaString(x);
		      vals[3] = ts;							//  locators
		      vals[4] = node.getDBpedia();			// dbpedia
		      vals[5] = node.getWikidata();			// wikidata
		      vals[6] = node.getTense();			// tense
		      vals[7] = node.getNegation();			// negation
		      vals[8] = node.getEpistemicStatus();	// epistemic status
		      long inx  = -1;
		      Long vx = null;
		      if (node.hasInverseTerm())
		    	  vx = new Long(node.getInverseTerm());
		      vals[9] = vx;							//inverse predicate
		      if (node.hasCannonicalTerm())
		    	  vx = new Long(node.getCannonTerm());
		      vals[10] = vx;							//canonical term
		      IResult rx = conn.executeSQL(sql, vals);
		      if (rx.hasError())
				result.addErrorString(rx.getErrorString());
		      // links
		      putLinks(objectId, node, conn, rx);
		      
		      //extension propeties
		      JsonObject props = node.getExtensionPropeties();
		      if (props != null) {
		    	  Iterator<String> itr = props.keySet().iterator();
		    	  String key;
		    	  while (itr.hasNext()) {
		    		  key = itr.next();
		    		  this.putProperty(objectId,
		    		  						 key, props.get(key).getAsString(),
		    		  						 conn, rx);
		    		  if (rx.hasError())
		    			  result.addErrorString(rx.getErrorString());
		    	  }
		      }
	    } catch (Exception e) {
	      result.addErrorString("PDD-4 "+objectId+" "+e.getMessage());
	      environment.logError("PDD-5 "+objectId+" "+result.getErrorString(), null);
	    } finally {
	    	conn.closeConnection(result);
	    }
		return result;

	}
	void putLinks(long nodeId, IWordGram wg, IPostgresConnection conn, IResult r )  {
		JsonArray inL = wg.listInLinks();
		JsonArray outL = wg.listOutLinks();
		if (inL == null && outL == null) return;
		//inLInks
		String sql;
		IResult rx;
		Object [] obj = new Object[3];
		obj [0] = Long.toString(nodeId);
		Iterator<JsonElement> itr;
		String theProp;
		String [] px;
		if (inL != null) {
			sql = IQueries.PUT_INlINK;
			//(id, isentenceId, itargetId)
			itr =inL.iterator();
			while (itr.hasNext()) {
				theProp = itr.next().getAsString();
				px = theProp.split(",");
				obj[1] = px[0].trim();
				obj[2] = px[1].trim();
				rx = conn.executeSQL(sql, obj);
				if (rx.hasError())
					r.addErrorString(rx.getErrorString());
			}
		}
		if (outL != null) {
			sql = IQueries.PUT_OUTlINK;
			itr =outL.iterator();
			while (itr.hasNext()) {
				theProp = itr.next().getAsString();
				px = theProp.split(",");
				obj[1] = px[0].trim();
				obj[2] = px[1].trim();
				rx = conn.executeSQL(sql, obj);
				if (rx.hasError())
					r.addErrorString(rx.getErrorString());
			}
		}
		
	}
	/**
	 * Can return {@code null
	 * @param array
	 * @return
	 */
	String jsonArrayToCommaString(JsonArray array) {
		if (array == null || array.size() == 0) return null;
		StringBuilder buf = new StringBuilder();
		Iterator<JsonElement> itr = array.iterator();
		JsonElement je;
		boolean isFirst = true;
		while (itr.hasNext()) {
			if (isFirst) 
				isFirst = false;
			else 
				buf.append(", ");
			je= itr.next();
			buf.append(je.getAsString());
		}
		return buf.toString().trim();
	}
	/*@Override
	public IResult putNode(IWordGram node) {
		IResult result = new ResultPojo();
	    IPostgresConnection conn = null;
	    JsonObject data = node.getData();
	    long objectId=node.getId();
	    JsonArray foo =null;
	    try {
	      conn = dbDriver.getConnection();
	      String key;
	      Iterator<String> itr = data.keySet().iterator();
	      while (itr.hasNext()) {
	    	  key = itr.next();
	    	  // which key is important -we already have the id key's value
	    	  if (key.equals(IConstants.LOX_KEY)) {
	    		  putStringArray(objectId, IConstants.LOX_KEY, node.listTopicLocators(), conn, result);
	    	  } else if (key.equals(IConstants.IN_KEY)) {
	    		  putStringArray(objectId, IConstants.IN_KEY, node.listInLinks(), conn, result);
	    	  } else if (key.equals(IConstants.OUT_KEY)) {
	    		  putStringArray(objectId, IConstants.OUT_KEY, node.listOutLinks(), conn, result);
	    	  } else if (key.equals(IConstants.DBPED_KEY)) {
	    		  putStringArray(objectId, IConstants.DBPED_KEY, node.listDBpedia(), conn, result);
	    	  } else if (key.equals(IConstants.WIKID_KEY)) {
	    		  putStringArray(objectId, IConstants.WIKID_KEY, node.listWikidata(), conn, result);
	    	  } else if (key.equals(IConstants.POS_KEY)) {
	    		  putStringArray(objectId, IConstants.POS_KEY, node.listPOS(), conn, result);
	    	  } else if (key.equals(IConstants.WORDS_KEY)) {
	    		  putProperty(objectId, IConstants.WORDS_KEY, node.getWords(), conn, result);
	    	  } else if (key.equals(IConstants.INVERSE_KEY)) {
	    		  putLongProperty(objectId, IConstants.INVERSE_KEY, node.getInverseTerm(), conn, result);
	    	  } else if (key.equals(IConstants.CANNON_KEY)) {
	    		  putLongProperty(objectId, IConstants.CANNON_KEY, node.getCannonTerm(), conn, result);
	    	  } else if (key.equals(IConstants.SYNONYM_KEY)) {
	    		  putStringArray(objectId, IConstants.SYNONYM_KEY, node.listSynonyms(), conn, result);
	    	  } else if (key.equals(IConstants.ANTONYM_KEY)) {
	    		  putStringArray(objectId, IConstants.ANTONYM_KEY, node.listAntonyms(), conn, result);
	    	  }	    	  
 	      }
	    } catch (Exception e) {
	      result.addErrorString("PDD-4 "+objectId+" "+e.getMessage());
	      environment.logError("PDD-5 "+objectId+" "+result.getErrorString(), null);
	    } finally {
	    	conn.closeConnection(result);
	    }
		return result;
	}*/
	private void putLongProperty(long id, String key, long value, IPostgresConnection conn, IResult r) {
		if (value == -1) return;
		String sql = IQueries.PUT_PROPERTY;
		Object [] obj = new Object[3];
		//(id, _key, _val)
		obj[0] = new Long(id);
		obj[1] = key;
		obj[2] = new Long(value);
		IResult rx = conn.executeSQL(sql, obj);
		if (rx.hasError())
			r.addErrorString(rx.getErrorString());
	}

	private void putProperty(long id, String key, String value, IPostgresConnection conn, IResult r) {
		if (value == null) return;
		String sql = IQueries.PUT_PROPERTY;
		Object [] obj = new Object[3];
		//(id, _key, _val)
		obj[0] = new Long(id);
		obj[1] = key;
		obj[2] = value;
		IResult rx = conn.executeSQL(sql, obj);
		if (rx.hasError())
			r.addErrorString(rx.getErrorString());
	}
	private void putStringArray(long id, String key, JsonArray vals, IPostgresConnection conn, IResult r) {
		if (vals == null) return;
		String sql = IQueries.PUT_PROPERTY;
		Object [] obj = new Object[3];
		//(id, _key, _val)
		obj[0] = new Long(id);
		obj[1] = key;
		IResult rx;
		Iterator<JsonElement> itr = vals.iterator();
		JsonElement je;
		while  (itr.hasNext()) {
			je = itr.next();
			if (je != null) {
				obj[2] = je.getAsString();
				rx = conn.executeSQL(sql, obj);
				if (rx.hasError())
					r.addErrorString(rx.getErrorString());
			}
		}
	}
	
	@Override
	public IResult getNode(long nodeId) {
		System.out.println("PGgetNode "+nodeId);
		environment.logError("PGgetNode "+nodeId, null);
		IResult result = new ResultPojo();
		String sql = IQueries.GET_NODE;
	    IPostgresConnection conn = null;
		Object obj = new Long(nodeId);
		IWordGram wg = null;
		try {
			conn = dbDriver.getConnection();
			// fetch core
			IResult r = conn.executeSelect(sql, obj);
		    ResultSet rs = (ResultSet)r.getResultObject();
		    if (r.hasError())
		    	result.addErrorString(r.getErrorString());
			System.out.println("PGgetNode-1 "+nodeId+" "+rs);
		    if (rs != null) {
		    	if (rs.next()) {
			    	wg = new WordGram();
			    	result.setResultObject(wg);
			    	wg.setId(nodeId);
			    	wg.setWords(rs.getString("words"));
			    	wg.setNegation(rs.getBoolean("negation"));
			    	if (rs.getString("dbpedia") != null)
			    		wg.setDBpedia(rs.getString("dbpedia"));
			    	if (rs.getString("wikidata") != null)
			    		wg.setWikidata(rs.getString("wikidata"));
			    	if (rs.getString("tense") != null)
			    		wg.setTense(rs.getString("tense"));
			    	if (rs.getString("epi") != null)
			    		wg.setEpistemicStatus(rs.getString("epi"));
			    	if (rs.getString("active") != null)
			    		wg.setInverseTerm(rs.getLong("active"));
			    	if (rs.getString("cannon") != null)
			    		wg.setCannonTerm(rs.getLong("cannon"));
			    	if (rs.getString("pos") != null) {
			    		wg.setPOS(stringToJA(rs.getString("pos")));
			    	}
			    	if (rs.getString("topicid") != null) {
			    		wg.setTopicLocators(stringToJA(rs.getString("topicid")));
			    	}
			    	IResult rx = new ResultPojo();
			    	// fetch links
			    	getLinks(nodeId, wg, conn, rx);
			    	if (rx.hasError())
			    		result.addErrorString(rx.getErrorString());
			    	// fetch extended properties
			    	getProperties(nodeId, wg, conn, rx);
		    	}
		    }
	    } catch (Exception e) {
	    	result.addErrorString("GetNode "+e.getMessage());
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.closeConnection(result);
	    }
		return result;
	}
	
	void getLinks(long nodeId, IWordGram wg, IPostgresConnection conn, IResult r ) throws Exception {
		//inlinks
		Object [] obj = new Object[1];
		obj[0] = new Long(nodeId);
		IResult rx;
		String sql = IQueries.GET_INLINKS;
		rx = conn.executeSelect(sql, obj);
		if (rx.hasError())
			r.addErrorString(rx.getErrorString());
		ResultSet rs = (ResultSet)rx.getResultObject();
		if (rs != null) {
			while (rs.next()) {
				wg.addInLink(Long.valueOf(rs.getString("isentenceId")), Long.valueOf(rs.getString("itargetId")));
			}
		}
		
		//outloinks
		sql = IQueries.GET_OUTLINKS;
		rx = conn.executeSelect(sql, obj);
		if (rx.hasError())
			r.addErrorString(rx.getErrorString());
		rs = (ResultSet)rx.getResultObject();
		if (rs != null) {
			while (rs.next()) {
				wg.addInLink(Long.valueOf(rs.getString("osentenceId")), Long.valueOf(rs.getString("otargetId")));
			}
		}

	}
	void getProperties(long nodeId, IWordGram wg, IPostgresConnection conn, IResult r ) throws Exception {
		String sql = IQueries.GET_PROPERTIES;
		Object [] obj = new Object[1];
		obj[0] = new Long(nodeId);
		IResult rx = conn.executeSelect(sql, obj);
		if (rx.hasError())
			r.addErrorString(rx.getErrorString());
		ResultSet rs = (ResultSet)rx.getResultObject();
		if (rs != null) {
			while (rs.next()) {
				wg.addExtensionProperty(rs.getString("_key"), rs.getString("_val"));
			}
		}
	}
	
	JsonArray stringToJA(String commaDelimitedString) {
		environment.logError("PWGPsplit "+commaDelimitedString, null);
		JsonArray  result = new JsonArray();
		String [] foo = commaDelimitedString.split(",");
		int len = foo.length;
		for (int i=0;i<len;i++)
			result.add(foo[i].trim());
		return result;
	}
/*	@Override
	public IResult getNode(long nodeId) {
		System.out.println("PGgetNode "+nodeId);
		environment.logError("PGgetNode "+nodeId, null);
		IResult result = new ResultPojo();
		String sql = IQueries.GET_NODE;
	    IPostgresConnection conn = null;
		Object obj = new Long(nodeId);
		try {
			conn = dbDriver.getConnection();
			IResult r = conn.executeSelect(sql, obj);
		    ResultSet rs = (ResultSet)r.getResultObject();
		    if (r.hasError())
		    	result.addErrorString(r.getErrorString());
			System.out.println("PGgetNode-1 "+nodeId+" "+rs);
		    if (rs != null) {
			    String key, value;	  
		    	JsonObject jo = null;
		    	while (rs.next()) {
		    		if (jo == null) jo = new JsonObject();
		    		key = rs.getString("_key");
		    		value = rs.getString("_val");
		    		loadNode(jo, key, value);
		    	}
		    	jo.addProperty("id", new Long(nodeId));
		    	result.setResultObject(jo);
		    }
	    } catch (Exception e) {
	    	result.addErrorString("GetNode "+e.getMessage());
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.closeConnection(result);
	    }
		return result;
	}
*/
	void loadNode(JsonObject node, String key, String value) {
		System.out.println("PGloadNode "+node+" | "+key+" | "+value);
		JsonElement je = node.get(key);
		JsonArray ja;
		if (je == null)
			node.addProperty(key, value);
		else {
			if (je.isJsonArray()) {
				ja = je.getAsJsonArray();
				ja.add(value);
			} else {
				ja = new JsonArray();
				ja.add(je);
				ja.add(value);
				node.add(key, ja);
			}
		}
	}
	
	
	@Override
	public IResult addNodeProperty(long id, String key, String value) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult addNodeProperties(long id, JsonObject keysVals) {
		System.out.println("PGaddNodeProperties "+id+" "+keysVals);
		IResult result = new ResultPojo();
		Iterator<String> itr = keysVals.keySet().iterator();
	    IPostgresConnection conn = null;
	    String key, val;
		try {
			conn = dbDriver.getConnection();
			while (itr.hasNext()) {
				key = itr.next();
				val = keysVals.get(key).getAsString();
				if (key.equals("id")) {
					this.putLongProperty(id, key, Long.valueOf(val).longValue(), conn, result);
				} else
					this.putProperty(id, key, val, conn, result);
			}
	    } catch (Exception e) {
	    	result.addErrorString("GetNode "+e.getMessage());
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.closeConnection(result);
	    }
		
		return result;
	}

	@Override
	public IResult removeNodeProperty(long id, String key, String value) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

}
