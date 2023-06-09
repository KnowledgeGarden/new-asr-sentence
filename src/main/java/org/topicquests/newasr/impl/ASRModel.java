/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;


import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.SentenceEngine;
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
	private SentenceEngine sentenceEngine;

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
				IWordGram wg = new WordGram(environment);
				wg.setId(idl);
				wg.setWords(term, null);
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
			System.out.println("ASRGet-1 "+result.getResultObject());
			wg = (IWordGram)result.getResultObject();
			if (wg != null) {
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
		System.out.println("ASRGetThis-1 "+result.getResultObject());
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
		environment.logDebug("PROCESSPRED "+term+" |"+tense+" |"+inverseTerm+" |"+cannonicalTerm+" |"+epistemicStatus+" |"+isNegative);
		IResult result = new ResultPojo();
		IResult r;
		boolean exists =termExists(term); // in dictionary
		r = dictionary.addTerm(term); // add to get id
		String id = (String)r.getResultObject();
		String invId = null;
		String canonId = null;
		if (inverseTerm != null && !inverseTerm.equals("")) {
			r = dictionary.addTerm(inverseTerm);
			invId = (String)r.getResultObject();
		}
		if (cannonicalTerm != null && !cannonicalTerm.equals("")) {
			r = dictionary.addTerm(cannonicalTerm);
			canonId = (String)r.getResultObject();
		}
		IWordGram wg = new WordGram(environment);
		wg.setId(Long.parseLong(id));
		wg.setWords(term, null);
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
				nwg = new WordGram(environment);
				nwg.setId(Long.parseLong(invId));
				nwg.addPOS(IPOS.VERB_POS);
				nwg.setWords(inverseTerm, null);
				r = database.putNode(nwg);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
		}
		if (canonId !=  null) {
			isInDB = termExistsInDB(canonId);
			if (!isInDB) {
				nwg = new WordGram(environment);
				nwg.setId(Long.parseLong(canonId));
				nwg.addPOS(IPOS.VERB_POS);
				nwg.setWords(cannonicalTerm, null);
				r = database.putNode(nwg);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
		}
		isInDB = termExistsInDB(id);
		environment.logDebug("PROCESSPRED-21 "+isInDB+" "+wg.getData()); 
		if (!isInDB) {
			environment.logDebug("PROCESSPRED-25 "); 
			r = database.putNode(wg); 
			environment.logDebug("PROCESSPRED-30 "+r.getErrorString()); 
			if (r.hasError())
				result.addErrorString(r.getErrorString());
		}
		
		return result;
	}

	@Override
	public boolean acceptSpacyResponse(JsonObject sentence) {
		return sentenceEngine.acceptSpacyResponse(sentence);
	}

	@Override
	public boolean acceptNewSentence(JsonObject sentence) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSentenceEngine(SentenceEngine se) {
		sentenceEngine = se;
	}

	@Override
	public IResult addPOS(long gramId, String value) {
		return database.addPOS(gramId, value);
	}

	@Override
	public IResult addWikidata(long gramId, String value) {
		return database.addWikidata(gramId, value);
	}

	@Override
	public IResult addDBpedia(long gramId, String value) {
		return database.addDBpedia(gramId, value);
	}

	@Override
	public IResult addSentenceEdge(long gramId, long sentenceId, long inLinkTargetId, long outlinkTargetId,
			String tense, String epistemicStatus) {
		return database.addSentenceEdge(gramId, sentenceId, inLinkTargetId, outlinkTargetId, tense, epistemicStatus);
	}

	@Override
	public IResult addTopicLocator(long gramId, long topicLocator) {
		// TODO Auto-generated method stub
		return database.addTopicLocator(gramId, topicLocator);
	}

	@Override
	public IResult addSynonymTerm(long gramId, long synonymTermId) {
		return database.addSynonymTerm(gramId, synonymTermId);
	}

	@Override
	public IResult addAntonymTerm(long gramId, long antonymTermId) {
		return database.addAntonymTerm(gramId,antonymTermId);
	}

	@Override
	public IResult addHyponymTerm(long gramId, long hypoTermId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult addHypernymTerm(long gramId, long hyperTermId) {
		return database.addHyponymTerm(gramId, hyperTermId);
	}

	@Override
	public IResult processTerm(String term, String pos, long sentenceId, long inTargetId, long outTargetId,
			String tense, String epi) {
		IResult result = new ResultPojo();
		IResult r = this.processTerm(term, pos);
		if (r.hasError())
			result.addErrorString(r.getErrorString());
		String ix = (String)r.getResultObject();
		//long gramId = Long.parseLong(ix);
		r = this.getThisTermById(ix);
		if (r.hasError())
			result.addErrorString(r.getErrorString());
		IWordGram wg = (IWordGram)r.getResultObject();
		if (sentenceId > -1)
			wg.addSentenceEdge(sentenceId, inTargetId, outTargetId, tense, epi);
		result.setResultObject(ix);
		return result;
	}

}
