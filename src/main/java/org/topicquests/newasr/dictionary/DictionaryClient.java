/**
 * 
 */
package org.topicquests.newasr.dictionary;

import java.util.Iterator;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IDictionary;
import org.topicquests.newasr.api.IDictionaryClient;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class DictionaryClient implements IDictionary {
	private ASREnvironment environment;
	private IDictionaryClient dictionaryClient;
	private JsonUtil util;
	/* in memory dictionary for speed */
	private JsonObject dictionary;
	// from DictionaryServer
	private static final String
//		VERB			= "verb",
//		CLIENT_ID		= "clientId",
//		TERM			= "term",
//		GET_TERM		= "getWord",
		IS_NEW_TERM		= "isNewTerm",	// boolean <code>true</code> if is new word
//		TEST			= "test",
//		ERROR			= "error",
		CARGO			= "cargo"; //return object - wordId or word
	// local
	public static final String
		TERMS			= "terms", 	// key= term/lowercase, val=id
		IDS				= "ids";	// key = id, val = term in any case
	/**
	 * @param e
	 */
	public DictionaryClient(ASREnvironment e) {
		environment = e;
		dictionaryClient = environment.getDictionaryClient();
		util = new JsonUtil();
		// initialize the dictionary
		dictionary = new JsonObject();
		IResult r = dictionaryClient.getDictionary();
		environment.logError("AAA "+r.getErrorString(), null);
		//environment.logError("BBB "+r.getResultObject(), null);
		//{"cargo":"{\"36407\":\"minuets|minuets\",\"36408\":\"minuses|minuses\"
		buildDicctionary((String)r.getResultObject());
	}

	void buildDicctionary(String dict) {
		if (dict == null)
			throw new RuntimeException("Missing Dictionary");
		try {
			JsonObject idx = new JsonObject();
			JsonObject tdx = new JsonObject();
			dictionary.add(TERMS, tdx);
			dictionary.add(IDS, idx);
			JsonObject jx = util.parse(dict);
			String jjj = jx.get(CARGO).getAsString();
			System.out.println("BA: "+jjj);

			jx = util.parse(jjj);
			System.out.println("BD: "+jx);
			Iterator<String>itr = jx.keySet().iterator();
			String id;
			String foo;
			String [] them;
			while (itr.hasNext()) {
				id = itr.next();
				foo = jx.get(id).getAsString();
				//foo := word|lowercaseword
				them = foo.split(":");
				if (them.length > 0) {
				//System.out.println("BOOT: "+id+" "+them[0]+""+them[1]);
					idx.addProperty(id, them[0]);
					tdx.addProperty(them[1], id);
				}
			}
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			throw new RuntimeException(e); //game over
		}
		environment.logError("LOADED "+this.getIDs().size(), null);
	}
	@Override
	public String getTerm(String id) {
		synchronized(dictionary) {
			JsonObject words = _getTerms();
			return words.get(id).getAsString();
		}
	}
	
	JsonObject _getTerms() {
		return dictionary.get(TERMS).getAsJsonObject();
	}
	
	JsonObject getIDs() {
		return dictionary.get(IDS).getAsJsonObject();
	}
	
	@Override
	public String getTermId(String term) {
		synchronized(dictionary) {
			JsonObject ids = getIDs();
			//System.out.println("CD.getWordIds "+ids+" "+word);
			String lc = term.toLowerCase();
			JsonElement x = ids.get(lc);
			if (x == null) return null;
			return x.getAsString();
		}	
	}

	public long getTermIdAsLong(String term) {
		synchronized(dictionary) {
			JsonObject ids = getIDs();
			//System.out.println("CD.getWordIds "+ids+" "+word);
			String lc = term.toLowerCase();
			return ids.get(lc).getAsLong();
		}
	}
	
	@Override
	public boolean isEmpty() {
		synchronized(dictionary) {
			JsonObject obj = this._getTerms();
			if (obj == null)
				return false;
			return obj.size() > 0;
		}
	}

	@Override
	public IResult addTerm(String term) {
		String theTerm = term.toLowerCase();
		IResult result = new ResultPojo();
		result.setResultObject("0"); //default
		result.setResultObjectA(new Boolean(true)); // default is new word
		environment.logDebug("Dictionary.addTerm "+term);
		//if (theWord.equals("\""))
		//	return result; // default id for a quote character
		//Will get the word even if lower case
		String id = getTermId(term);
		environment.logDebug("Dictionary.addTerm-1 "+id);
		if (id == null) {
			IResult r = dictionaryClient.addTerm(term);
			environment.logDebug("Dictionary.addTerm-2 "+r.getErrorString()+" | "+r.getResultObject());
			JsonObject jo = null;
			String json = (String)r.getResultObject();
			//TODO null check
			try {
				jo = util.parse(json);
				System.out.println("DictAddTermX "+term+" | "+json);
			} catch (Exception e) {
				environment.logError(e.getMessage(), e);
				e.printStackTrace();
			}
			JsonElement je = jo.get(IS_NEW_TERM);
			boolean isNew = false;
			if (je != null)
				isNew = je.getAsBoolean();
			environment.logDebug("Dictionary.addWord-3 "+isNew);
			//if (isNew)
			//	statisticsClient.addToKey(IASRFields.WORDS_NEW);
			//else
			//	result.setResultObjectA(new Boolean(false));
			result.setResultObjectA(new Boolean(isNew));
			id = jo.get(CARGO).getAsString();
			environment.logDebug("Dictionary.addWord-4 "+id);
			result.setResultObject(id);
			synchronized(dictionary) {
				_getTerms().addProperty(id, term);
				getIDs().addProperty(theTerm, id);
			}
		} else
			result.setResultObject(id);
		environment.logDebug("Dictionary.addWord-5 "+id);
		return result;	}

	@Override
	public JsonObject getDictionary() {
		return dictionary;
	}

}
