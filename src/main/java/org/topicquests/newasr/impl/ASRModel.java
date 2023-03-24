/**
 * 
 */
package org.topicquests.newasr.impl;


import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrDataProvider;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IConstants;
import org.topicquests.newasr.api.IDictionary;
import org.topicquests.newasr.api.IPOS;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.wg.WordGramCache;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class ASRModel implements IAsrModel {
	private ASREnvironment environment;
	private IDictionary dictionary;
	private IAsrDataProvider database;
	private WordGramCache cache;
	private final int CACHE_SIZE = 8192;

	/**
	 * 
	 */
	public ASRModel(ASREnvironment e) {
		environment = e;
		dictionary = environment.getDictionary();
		database = environment.getDatabase();
		cache = new WordGramCache(environment, this, CACHE_SIZE);
	}

	///////////////////////////////
	// A sentence is broken into a word array, and from there, into WordGram instances
	// A sentence is passed to external agents to identify:
	//		DBpedia entries --> JSON structures for hits of entities
	//		Wikidata identifiers
	//		Eventually - if needed
	//			spaCy models for parse trees
	//	Individual WordGram sequences which are found to be noun or verb phrases are replaced
	//		with their phrase WordGram equivalent
	////////////////////////////////
	@Override
	public IResult processSentence(long sentenceId, String sentence) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	///////////////////////////////
	// A mechanism for bootstrapping the WordGram graph
	// This means we need access to the Dictionary
	///////////////////////////////
	@Override
	public IResult processTerm(String term, String pos) {
		System.out.println("ModelProcessingTerm "+term+" | "+pos);
		IResult result = new ResultPojo();
		if (term.equals("\"")) {
			result.setResultObject("1");
			return result;
		}
		boolean exists =termExists(term); // in dictionary
		IResult r = dictionary.addTerm(term); // add to get id
		String id = (String)r.getResultObject();
		boolean isInDB = termExistsInDB(id);
		result.setResultObject(id);
		long idl = new Long(id).longValue();
		if (!isInDB) {
			r = getTermById(id);
			if (r.getResultObject() == null) {
				IWordGram wg = new WordGram();
				wg.setId(idl);
				wg.setWords(term);
				JsonObject jo = new JsonObject();
				if (pos != null)
					wg.addPOS(pos);

				r = database.putNode(wg);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
		}
		return result;
	}

	@Override
	public IResult getTermById(String id) {
		IResult result = null;
		IWordGram wg = cache.get(id);
		if (wg == null) {
			result = database.getNode(new Long(id).longValue());
			JsonObject jo = (JsonObject)result.getResultObject();
			if (jo != null) {
				wg = new WordGram(jo);
				cache.add(id,wg);
				result.setResultObject(wg);
			}
		} else {
			result = new ResultPojo();
			result.setResultObject(wg);
		}
		return result;		
	}
	
	@Override
	public IResult getThisTermById(String id) {
		System.out.println("ASRGetThis "+id);
		IResult result = database.getNode(new Long(id).longValue());
		IWordGram wg = (IWordGram)result.getResultObject();
		if (wg != null) {
			cache.add(id,wg);
			result.setResultObject(wg);
		}
		return result;
	}
	boolean termExists(String term) {
		return dictionary.getTermId(term) != null;
	}
	boolean termExistsInDB(String term) {
		IResult r = getThisTermById(term);
	
		return r.getResultObject() != null;
	}

	@Override
	public IResult newDictionaryEntry(String term) {
		IResult result = new ResultPojo();
		if (term.equals("\"")) {
			result.setResultObject("1");
			return result;
		}
		result = dictionary.addTerm(term); 	
		System.out.println("ASRNewDict "+term+" | "+result.getResultObject()+" | "+result.getErrorString());
		return result;
	}

	@Override
	public IResult putWordGram(IWordGram newGram) {
		IResult result = database.putNode(newGram);
		return result;
	}

	@Override
	public IResult processPredicate(String term, String tense, String inverseTerm, String cannonicalTerm,
			String epistemicStatus, boolean isNegative) {
		IResult result = new ResultPojo();
		IResult r;
		boolean exists =termExists(term); // in dictionary
		r = dictionary.addTerm(term); // add to get id
		String id = (String)r.getResultObject();
		String invId = null;
		String canonId = null;
		if (inverseTerm != null) {
			r = dictionary.addTerm(inverseTerm);
			invId = (String)r.getResultObject();
		}
		if (cannonicalTerm != null) {
			r = dictionary.addTerm(cannonicalTerm);
			canonId = (String)r.getResultObject();
		}
		IWordGram wg = new WordGram();
		wg.setId(Long.parseLong(id));
		wg.setWords(term);
		wg.setTense(tense);
		wg.setNegation(isNegative);
		wg.addPOS(IPOS.VERB_POS);
		if (epistemicStatus != null)
			wg.setEpistemicStatus(epistemicStatus);
		if (invId !=  null)
			wg.setInverseTerm(Long.parseLong(invId));
		if (canonId !=  null)
			wg.setCannonTerm(Long.parseLong(canonId));
		//Before we store that, check for prior terms
		boolean isInDB;
		IWordGram nwg;
		if (invId !=  null) {
			isInDB = termExistsInDB(invId);
			if (!isInDB) {
				nwg = new WordGram();
				nwg.setId(Long.parseLong(invId));
				nwg.addPOS(IPOS.VERB_POS);
				nwg.setWords(inverseTerm);
				r = database.putNode(nwg);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
		}
		if (canonId !=  null) {
			isInDB = termExistsInDB(canonId);
			if (!isInDB) {
				nwg = new WordGram();
				nwg.setId(Long.parseLong(canonId));
				nwg.addPOS(IPOS.VERB_POS);
				nwg.setWords(cannonicalTerm);
				r = database.putNode(nwg);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
		}
		isInDB = termExistsInDB(id);
		if (isInDB) {
			r = database.putNode(wg); 
			if (r.hasError())
				result.addErrorString(r.getErrorString());
		}
		
		return result;
	}
}
