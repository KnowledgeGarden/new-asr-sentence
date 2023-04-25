/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.wg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IPOS;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ISimpleTriple;
import org.topicquests.newasr.api.ITripleModel;
import org.topicquests.newasr.api.ITupleModel;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.impl.ASRSimpleTriple;
import org.topicquests.newasr.impl.ASRTupleModel;
import org.topicquests.newasr.trip.TripleAnalyzer;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * @author jackpark
 *
 */
public class WordGramBuilder {
	private ASREnvironment environment;
	private IAsrModel model;
	private TripleAnalyzer analyzer;
	private ITupleModel tripleModel;
	private JsonUtil util;
	private final String
		NOUN 	= "NOUN",
		LANG	= "en"; //TODO
	/**
	 * 
	 */
	public WordGramBuilder(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		analyzer = new TripleAnalyzer(environment);
		tripleModel = new ASRTupleModel(environment);
		util = new JsonUtil();
	}

	/*****************8
	 Predicates for "Scientists have been thought to believe that climate change is caused by  carbon dioxide"
	 [{
		"strt": 1,
		"txt": "have been thought to believe"
	 }, {
		"strt": 9,
		"txt": "is caused by"
	 }]
	 DBpedia
	 [
		["climate change", "http://dbpedia.org/resource/Global_warming", "0.9999928412462271"],
		["carbon dioxide", "http://dbpedia.org/resource/Carbon_dioxide", "0.9993317763086826"]
	 ]
	 Wikidata - when it will be available
	 ["Germany", "Q1206012", "LOC", ["country in Central Europe"]]
	 Nouns
	 'nns': [{'strt': 0, 'txt': 'Scientists'}, {'strt': 3, 'txt': 'climate'}, {'strt': 4, 'txt': 'change'}, {'strt': 8, 'txt': 'carbon'}, {'strt': 9, 'txt': 'dioxide'}], 
	 ProperNouns
	 'pnns': [], 
	 Verbs
	 'vrbs': [{'strt': 1, 'txt': 'believe'}, {'strt': 6, 'txt': 'caused'}]
	 
	 
	 
	 
	 A WorkingTuple will come from this triple
	 {
		"subj": {
			"strt": "0",
			"txt": "Scientists"
		},
		"pred": {
			"strt": 5,
			"txt": " believe"
		},
		"obj": {
			"subj": {
				"strt": "8",
				"txt": "climate change",
				"dbp": {
					"strt": "climate change",
					"kid": "http://dbpedia.org/resource/Global_warming",
					"dbp": "0.9999928412462271"
				}
			},
			"pred": {
				"strt": 9,
				"txt": "is caused by"
			},
			"obj": {
				"strt": "13",
				"txt": "carbon dioxide",
				"dbp": {
					"strt": "carbon dioxide",
					"kid": "http://dbpedia.org/resource/Carbon_dioxide",
					"dbp": "0.9993317763086826"
				}
			}
		}
	}
	 ********************/
	///////////////////////////////////
	// Building Triples
	// 1- Craft an ISimpleTriple
	// 2- See if it exists as a Triple
	//		If not, see if it exists as a WorkingTriple
	//			IF so, grab its normId and update that with new sentenceId
	// 3- Otherwise, examine its wordgrams for normalization factors
	// 		If normalization is needed
	//			Craft a second ISimpleTriple and normalize it
	//			Store the normalized Trple and retrieve its Id
	//			Set the normId of the first ISimpleTriple
	//			Store it as a WorkingTriple
	//		Else store it as a Triple
	/////////////////////////////////
	/**
	 * Build the WordGram graph for the given {@code sentence}
	 * @param sentence
	 * @return
	 */
	public IResult processSentence(ISentence sentence) {
		long sentenceId =sentence.getId();
		IResult result = new ResultPojo();
		String text = sentence.getText();
		JsonArray predicates = sentence.getPredicatePhrases();
		JsonArray dbpedia = sentence.getDBpediaData();
		JsonArray wikidata = sentence.getWikiData();
		JsonArray spacyResults = sentence.getSpacyData();
		JsonArray spacyNouns = sentence.getNouns();
		JsonArray spacyProperNouns = sentence.getProperNouns();
		JsonArray spacyVerbs = sentence.getVerbs();
		JsonArray resolvedNouns = sentence.getResolvedNouns();
		Iterator<JsonElement> itr;
		JsonObject jo;
		JsonArray ja;
		String snippet, idx;
		int loc;
		IWordGram wg;
		IResult r;
		
		
		JsonArray ta = analyzer.bigDamnAnalyze(sentence, predicates, resolvedNouns);
		if (ta != null)
			sentence.setSimpleTriples(ta);
		
		int len = ta.size();
		JsonObject trix;
		JsonObject strix;
		for (int i=0;i<len;i++) {
			trix = ta.get(i).getAsJsonObject();
			strix = processSimpleTriple(sentenceId, trix);
			environment.logDebug("BigWGBuild\n"+trix+"\n"+strix);
		}
		
	
		
		
		return result;
	}
	
	ISimpleTriple _makeTriple(IWordGram subject, IWordGram predicate, IWordGram object) {
		ISimpleTriple result = new ASRSimpleTriple();
		result.setSubjectId(subject.getId(), ISimpleTriple.WORDGRAM_TYPE);
		result.setSubjectText(subject.getWords(LANG));
		result.setPredicateId(predicate.getId());
		result.setPredicateText(predicate.getWords(LANG));
		result.setObjectId(object.getId(), ISimpleTriple.WORDGRAM_TYPE);
		result.setObjectText(object.getWords(LANG));
		return result;
	}
	
	/**
	 * Always tries to return a fully normalized triple
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 * @throws Exception
	 */
	ISimpleTriple makeTriple(IWordGram subject, IWordGram predicate, IWordGram object) throws Exception {
		ISimpleTriple result = _makeTriple(subject, predicate, object);
		// do we have this triple?
		IResult r = tripleModel.getThisTuple(result);
		Object ox = r.getResultObject();
		String json;
		JsonObject jo;
		ISimpleTriple foo;
		if (ox != null) {
			json = (String)ox;
			jo = util.parse(json);
			result = new ASRSimpleTriple(jo);
		} else {
			r  = tripleModel.getThisWorkingTuple(result);
			ox = r.getResultObject();
			if (ox != null) {
				json = (String)ox;
				jo = util.parse(json);
				foo = new ASRSimpleTriple(jo);
				//ACTUALLY we have this as a WorkingTriple
				// let's return the real triple
				long normId = foo.getNormalizedTripleId();
				r = tripleModel.getTupleById(normId);
				json = (String)r.getResultObject();
				jo = util.parse(json);
				result = new ASRSimpleTriple(jo);
			} else {
				// this is virgin territory
				boolean needsNorm =  needsNormalization(subject, predicate, object);
				if (!needsNorm) {
					r = tripleModel.putTuple(result);
					ox = r.getResultObject();
					Long l = (Long)ox;
					result.setId(l.longValue());
				} else {
					
					IWordGram subj = normalizeGram(subject);
					IWordGram obj = normalizeGram(object);
					boolean reverse = predicate.getInverseTerm() > -1;
					IWordGram pred = normalizeGram(predicate);
					if (reverse)
						foo = _makeTriple(obj, pred, subj);
					else
						foo = _makeTriple(subj, pred, obj);
					// save tuple
					r = tripleModel.putTuple(result);
					ox = r.getResultObject();
					Long l = (Long)ox;
					// save as working tuple
					result.setNormalizedTripleId(l.longValue());
					r = tripleModel.putWorkingTuple(result);
					// now fetch the triple
					r = tripleModel.getTupleById(l.longValue());
					json = (String)r.getResultObject();
					jo = util.parse(json);
					result = new ASRSimpleTriple(jo);
				}
			}
		}
		return result;
	}
	IWordGram normalizeGram(IWordGram wg) throws Exception {
		IWordGram result = wg; // default
		long inv = wg.getInverseTerm();
		long can = wg.getCannonTerm();
		IResult r;
		if (inv > -1) {
			r = model.getThisTermById(Long.toString(inv));
			result= (IWordGram)r.getResultObject();
		} else if (can > -1) {
			r = model.getThisTermById(Long.toString(can));
			result= (IWordGram)r.getResultObject();
		}
		return result;
	}
	
	boolean needsNormalization(IWordGram subject, IWordGram predicate, IWordGram object) {
		boolean result = false;
		if (predicate.hasCannonicalTerm() || 
			predicate.getInverseTerm() > -1)
			return true;
		if (subject.hasCannonicalTerm() ||
			object.hasCannonicalTerm())
			return true;
		return result;
	}
	ISimpleTriple normalizeTriple(ISimpleTriple trip) throws Exception{
		ISimpleTriple result = trip; // default
		ISimpleTriple foo;
		String json;
		JsonObject jo;
		long inv;
		boolean found = false;
		IResult r = tripleModel.getThisTuple(trip);
		if (r.getResultObject() == null) {
			r = tripleModel.getThisWorkingTuple(trip);
			json = (String)r.getResultObject();
			jo = util.parse(json);
			foo = new ASRSimpleTriple(jo);
			inv = foo.getNormalizedTripleId();
			r = tripleModel.getTupleById(inv);
			json = (String)r.getResultObject();
			jo = util.parse(json);
			foo = new ASRSimpleTriple(jo);
			found = true;
		} else {
			json = (String)r.getResultObject();
			jo = util.parse(json);
			foo = new ASRSimpleTriple(jo);
			found = true;
		}
		if (found) {
			return foo;
		}
		// now the harder work because we do not have this triple
		String subjType = trip.getSubjectType();
		String objType = trip.getObjectType();
		long subjId = trip.getSubjectId();
		long objId = trip.getObjectId();
		long predId = trip.getPredicateId();
		IWordGram subj, pred, obj;
		
		r = model.getThisTermById(Long.toString(predId));
		pred = (IWordGram)r.getResultObject();
		inv = pred.getInverseTerm();
		boolean needsReverse = inv > -1;
		ISimpleTriple bar;
		
		if (subjType.equals(ISimpleTriple.WORDGRAM_TYPE)) {
			r = model.getThisTermById(Long.toString(subjId));
			subj = (IWordGram)r.getResultObject();
			subj = normalizeGram(subj);
			if (objType.equals(ISimpleTriple.WORDGRAM_TYPE)) {
				r = model.getThisTermById(Long.toString(objId));
				obj = (IWordGram)r.getResultObject();
				obj = normalizeGram(obj);
				pred = normalizeGram(pred);
				if (!needsReverse)
					result = _makeTriple(subj, pred, obj);
				else
					result = _makeTriple(obj, pred, subj);
			} else {
				// object is a triple
				r = tripleModel.getTupleById(objId);
				json = (String)r.getResultObject();
				jo = util.parse(json);
				foo = new ASRSimpleTriple(jo);
			}
		}
		return result;
	}
	
	ISimpleTriple fetchTriple(long id) throws Exception {
		ISimpleTriple result = null;
		IResult r = tripleModel.getTupleById(id);
		return result;
	}
	String getTripleText(ISimpleTriple trip) throws Exception {
		StringBuilder buf = new StringBuilder();
		//		String oText = "{ "+oSubject.getWords(LANG)+", "+oPredicate.getWords(LANG)+", "+oObject.getWords(LANG)+" }";
		buf.append("{ ");
		String subjType = trip.getSubjectType();
		String objType = trip.getObjectType();
		String subjectText, predText, objectText;
		boolean sIsWG = subjType.equals(ISimpleTriple.WORDGRAM_TYPE);
		boolean oIsWG = objType.equals(ISimpleTriple.WORDGRAM_TYPE);
		predText = trip.getPredicateText();
		ISimpleTriple foo;
		long oid;
		IResult r;
		if (sIsWG) {
			subjectText = trip.getSubjectText();
			if (oIsWG) {
				objectText = trip.getObjectText();
			} else {
				oid = trip.getObjectId();
				//r = 
			}
		}
		buf.append(" }");
		return buf.toString().trim();
	}
	ISimpleTriple makeTriple(IWordGram subject, IWordGram predicate, ISimpleTriple object) {
		ISimpleTriple result = null;
		long inv = predicate.getInverseTerm();
		IResult r;
		boolean needsReverse = inv > -1;
		return result;
	}
	ISimpleTriple makeTriple(ISimpleTriple subject, IWordGram predicate, IWordGram object) {
		ISimpleTriple result = null;
		long inv = predicate.getInverseTerm();
		IResult r;
		boolean needsReverse = inv > -1;
		return result;
	}
	ISimpleTriple makeTriple(IWordGram subject, IWordGram predicate, 
			IWordGram oSubject, IWordGram oPredicate, IWordGram oObject) throws Exception {
		ISimpleTriple result = new ASRSimpleTriple();
		ISimpleTriple foo;
		IWordGram nSubj = normalizeGram(subject);
		IWordGram noSubj = normalizeGram(oSubject);
		IWordGram noObj = normalizeGram(oObject);
		IWordGram nPred = normalizeGram(predicate);
		long inv = predicate.getInverseTerm();
		IResult r;
		boolean needsReverse = inv > -1;
		result.setPredicateId(predicate.getId());
		result.setPredicateText(predicate.getWords(LANG));
		foo = makeTriple(oSubject, oPredicate, oObject); // fully normalized
		String oText = "{ "+oSubject.getWords(LANG)+", "+oPredicate.getWords(LANG)+", "+oObject.getWords(LANG)+" }";
		if (needsReverse) {
			result = new ASRSimpleTriple();
			result.setPredicateId(nPred.getId());
			result.setPredicateText(nPred.getWords(LANG));
			result.setObjectText(nSubj.getWords(LANG));
			result.setObjectId(nSubj.getId(), ISimpleTriple.WORDGRAM_TYPE);
			result.setSubjectId(foo.getId(), ISimpleTriple.TRIPLE_TYPE);
			result.setSubjectText(oText);
		} else {
			result.setSubjectText(nSubj.getWords(LANG));
			result.setSubjectId(nSubj.getId(), ISimpleTriple.WORDGRAM_TYPE);
			result.setPredicateId(nPred.getId());
			result.setPredicateText(nPred.getWords(LANG));
			result.setObjectId(foo.getId(), ISimpleTriple.TRIPLE_TYPE);
			result.setObjectText(oText);
			// this is fully normalized
			r = tripleModel.getThisTuple(result);
			Object ox = r.getResultObject();
			Long l;
			if (ox != null) {
				l = (Long)ox;
				result.setId(l.longValue());
			} else {
				// store it
				r = tripleModel.putTuple(result);
				ox = r.getResultObject();
				l = (Long)ox;
				result.setId(l.longValue());
			}
		}

		return result;
	}
	
	/*ISimpleTriple makeTriple2(IWordGram sSubj, IWordGram sPred, IWordGram sObj,
							 IWordGram predicate, IWordGram object) throws Exception {
		ISimpleTriple result = new ASRSimpleTriple();
		
		return result;
	}*/
	
	ISimpleTriple makeTriple(ISimpleTriple subject, IWordGram predicate, ISimpleTriple object) throws Exception {
		ISimpleTriple result = new ASRSimpleTriple();
		long inv = predicate.getInverseTerm();
		IResult r;
		boolean needsReverse = inv > -1;
		return result;
	}
	
	/**
	 * Main entry point
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 * @throws Exception
	 */
	ISimpleTriple exploreTriple(Object subject, IWordGram predicate, Object object) throws Exception {
		boolean subjIsWG = (subject instanceof IWordGram);
		boolean objIsWG = (object instanceof IWordGram);
		if (subjIsWG) {
			if (objIsWG)
				return makeTriple((IWordGram)subject, predicate,(IWordGram)object);
			else {
				return (makeTriple((IWordGram)subject, predicate, (ISimpleTriple)object));
			}
		} if (objIsWG)
			return makeTriple((ISimpleTriple)subject, predicate,(IWordGram)object);
		else {
			return (makeTriple((ISimpleTriple)subject, predicate, (ISimpleTriple)object));
		}
	}
	/**
	 * Recursive
	 * NOTE: this will not work until we are storing triples to give them Identity
	 * @param triple
	 */
	JsonObject processSimpleTriple(long sentenceId,JsonObject triple) {
		JsonObject pred = triple.get("pred").getAsJsonObject();
		JsonObject sx = triple.get("subj").getAsJsonObject();
		JsonObject ox = triple.get("obj").getAsJsonObject();
		ISimpleTriple st = new ASRSimpleTriple();
		// Test subject
		JsonObject subject = null;
		String subjText = null;
		IWordGram wg;
		IResult r;
		String idx;
		IWordGram predicateGram;
		String predText = pred.get("txt").getAsString();
		/////////////////////
		// TODO
		// This is where we test for predicate existence
		// and Stop if it doesn't
		/////////////////////
		r = model.processTerm(predText, IPOS.VERB_POS);
		idx = (String)r.getResultObject();
		r = model.getThisTermById(idx);
		predicateGram = (IWordGram)r.getResultObject();
		String tense = predicateGram.getTense();
		String epi = predicateGram.getEpistemicStatus();
		st.setPredicateId(predicateGram.getId());
		if (sx.get("subj") == null) {
			subjText = sx.get("txt").getAsString();
			r = model.processTerm(subjText, IPOS.NOUN_POS);
			idx = (String)r.getResultObject();
			r = model.getThisTermById(idx);
			wg = (IWordGram)r.getResultObject();	
			wg.setSentenceId(sentenceId, tense, epi);
			st.setSubjectId(wg.getId(), ISimpleTriple.WORDGRAM_TYPE);
		} else {
			environment.logError("WGB-1 missing", null);
		}
		if (ox.get("subj") == null) {
			subjText = sx.get("txt").getAsString();
			r = model.processTerm(subjText, IPOS.NOUN_POS);
			idx = (String)r.getResultObject();
			r = model.getThisTermById(idx);
			wg = (IWordGram)r.getResultObject();	
			wg.setSentenceId(sentenceId, tense, epi);
			st.setObjectId(wg.getId(), ISimpleTriple.WORDGRAM_TYPE);
		} else {
			environment.logError("WGB-2 missing", null);
		}
		st.addSentenceId(sentenceId);
		//st.computePSI();
		return st.getData();
	}
	///////////////////////////////
	// We need to know where terms are relative to each other
	// We also must pay attention to inverse predicates
	/**
	 * <p>Create triples if they can be created, and return in list</p>
	 * <p>In theory, there will be one triple for each predicate</p>
	 * @param sentence
	 * @param predWordgrams
	 * @param dbPediaWordgrams
	 * @param wikidataWordgrams
	 * @param nounWordgrams
	 * @return can return an empty array
	 * /
	JsonArray lookForTriples(ISentence sentence, 
			List<IWordGram> predWordgrams,
			List<IWordGram> dbPediaWordgrams,
			List<IWordGram> wikidataWordgrams,
			List<IWordGram> nounWordgrams) {
		JsonArray result = new JsonArray();
		String theSentence = sentence.getText();
		IWordGram subj=null, pred=null, obj=null;
		ISimpleTriple subjT, objT;
		int lenP = predWordgrams.size();
		int lenS1 = dbPediaWordgrams.size();
		int lenS3 = wikidataWordgrams.size();
		int lenS4 = nounWordgrams.size();
		String txtA, txtB;
		boolean hasInverseTerm;
		long canonicalTermId = -1;
		int wherePredicate = -1;
		int whereOther = -1;
		boolean subjectFound, objectFound;
		// For each predicaate
		for (int i=0;i<lenP;i++) {
			subjectFound = false;
			objectFound = false;
			hasInverseTerm = false;
			pred = predWordgrams.get(i);
			txtA = pred.getWords(null);
			hasInverseTerm = pred.hasInverseTerm();
			wherePredicate = locateTermInSentence(theSentence, txtA);
			//Look in dbpedia
			for (int j=0;j<lenS1;j++) {
				subj = dbPediaWordgrams.get(j);
				txtB = subj.getWords(null);
				whereOther = locateTermInSentence(theSentence, txtB);
				if (whereOther > wherePredicate) {
					subj = null;
					break;
				} else {
					environment.logDebug("PPG-1 "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
					// heuristic = mayte that's it
					subjectFound = true;
					break;
				}
			}
			if (!subjectFound && lenS3 > 0) {
				for (int j=0;j<lenS3;j++) {
					subj = wikidataWordgrams.get(j);
					txtB = subj.getWords(null);
					whereOther = locateTermInSentence(theSentence, txtB);
					if (whereOther > wherePredicate) {
						subj = null;
						break;
					} else {
						environment.logDebug("PPG-2 "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
						// heuristic = mayte that's it
						subjectFound = true;
						break;
					}
				}
			}
			if (!subjectFound && lenS4 > 0) {
				for (int j=0;j<lenS4;j++) {
					subj = nounWordgrams.get(j);
					txtB = subj.getWords(null);
					whereOther = locateTermInSentence(theSentence, txtB);
					if (whereOther > wherePredicate) {
						subj = null;
						break;
					} else {
						environment.logDebug("PPG-3 "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
						// heuristic = mayte that's it
						subjectFound = true;
						break;
					}
				}
			}
			if (subjectFound)  {
				//look for object
				//Look in dbpedia
				for (int j=0;j<lenS1;j++) {
					obj = dbPediaWordgrams.get(j);
					txtB = obj.getWords(null);
					whereOther = locateTermInSentence(theSentence, txtB);
					environment.logDebug("PPG-4a "+lenS1+" "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
					if (whereOther < wherePredicate) {
						obj = null;
						;
					} else {
						environment.logDebug("PPG-4 "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
						// heuristic = mayte that's it
						objectFound = true;
						break;
					}
				}
				if (!objectFound && lenS3 > 0) {
					for (int j=0;j<lenS3;j++) {
						obj = wikidataWordgrams.get(j);
						txtB = obj.getWords(null);
						whereOther = locateTermInSentence(theSentence, txtB);
						if (whereOther < wherePredicate) {
							obj = null;
							
						} else {
							environment.logDebug("PPG-5 "+txtA+" "+txtA+" "+wherePredicate+" "+whereOther);
							// heuristic = mayte that's it
							objectFound = true;
							break;
						}
					}
				}
				if (!objectFound && lenS4 > 0) {
					for (int j=0;j<lenS4;j++) {
						obj = nounWordgrams.get(j);
						txtB = obj.getWords(null);
						whereOther = locateTermInSentence(theSentence, txtB);
						if (whereOther < wherePredicate) {
							obj = null;
							;
						} else {
							environment.logDebug("PPG-6 "+txtA+" "+txtB+" "+wherePredicate+" "+whereOther);
							// heuristic = mayte that's it
							objectFound = true;
							break;
						}
					}
				}				
			}
			if (subjectFound && objectFound) {
				System.out.println("XX "+subj+"|"+pred+"|"+obj);
				formTriple(subj, pred, obj);
			}

		}
		return result;
	}
	
	ISimpleTriple formTriple(IWordGram subject, IWordGram predicate, IWordGram object) {
		ISimpleTriple result = null;
		long cannon= -1;
		boolean hasInverse = predicate.hasInverseTerm();
		IWordGram pred;
		IResult r;
		if (hasInverse) {
			r = model.getThisTermById(Long.toString(predicate.getInverseTerm()));
			pred = (IWordGram)r.getResultObject();
		} else {
			pred = predicate;
		} 
		if (cannon > -1) {
			r = model.getThisTermById(Long.toString(cannon));
			pred = (IWordGram)r.getResultObject();
		}
		//TODO check subject and object for predicates
		String foo = subject.getWords(null)+" "+pred.getWords(null)+" "+object.getWords(null);
		environment.logDebug("TheTriple: "+foo);
		environment.logDebug("Pred: "+predicate.getData());

		return result;
	}
	
	int locateTermInSentence(String sentence, String term) {
		String SS = sentence.toLowerCase();
		String TT = term.toLowerCase();
		return SS.indexOf(TT);
	}
*/
}

/**
A spacy sentence
{
	"nodes": [{
		"concepts": {
			"concepts": ["http:\/\/ontology.apa.org\/apaonto\/termsonlyOUT%20(5).owl#Scientists", "http:\/\/localhost\/plosthes.2017-1#8580"],
			"text": "Scientists"
		},
		"pos": "NOUN",
		"start": 0,
		"i": 0,
		"lemma": "scientist",
		"tag": "NNS",
		"text": "Scientists",
		"dep": "nsubjpass"
	}, {
		"pos": "AUX",
		"start": 11,
		"i": 1,
		"lemma": "have",
		"tag": "VBP",
		"text": "have",
		"dep": "aux"
	}, {
		"pos": "AUX",
		"start": 16,
		"i": 2,
		"lemma": "be",
		"tag": "VBN",
		"text": "been",
		"dep": "auxpass"
	}, {
		"pos": "VERB",
		"start": 21,
		"i": 3,
		"lemma": "think",
		"tag": "VBN",
		"text": "thought",
		"dep": "ROOT"
	}, {
		"pos": "PART",
		"start": 29,
		"i": 4,
		"lemma": "to",
		"tag": "TO",
		"text": "to",
		"dep": "aux"
	}, {
		"pos": "VERB",
		"start": 32,
		"i": 5,
		"lemma": "believe",
		"tag": "VB",
		"text": "believe",
		"dep": "xcomp"
	}, {
		"pos": "SCONJ",
		"start": 40,
		"i": 6,
		"lemma": "that",
		"tag": "IN",
		"text": "that",
		"dep": "mark"
	}, {
		"pos": "NOUN",
		"start": 45,
		"i": 7,
		"lemma": "climate",
		"tag": "NN",
		"text": "climate",
		"dep": "compound"
	}, {
		"concepts": {
			"concepts": ["http:\/\/purl.obolibrary.org\/obo\/ExO_0000014", "http:\/\/purl.obolibrary.org\/obo\/ENVO_01000629"],
			"text": "climate change"
		},
		"pos": "NOUN",
		"start": 53,
		"i": 8,
		"lemma": "change",
		"tag": "NN",
		"text": "change",
		"dep": "nsubjpass"
	}, {
		"pos": "AUX",
		"start": 60,
		"i": 9,
		"lemma": "be",
		"tag": "VBZ",
		"text": "is",
		"dep": "auxpass"
	}, {
		"pos": "VERB",
		"start": 63,
		"i": 10,
		"lemma": "cause",
		"tag": "VBN",
		"text": "caused",
		"dep": "ccomp"
	}, {
		"pos": "ADP",
		"start": 70,
		"i": 11,
		"lemma": "by",
		"tag": "IN",
		"text": "by",
		"dep": "agent"
	}, {
		"pos": "SPACE",
		"start": 73,
		"i": 12,
		"lemma": " ",
		"tag": "_SP",
		"text": " ",
		"dep": "dep"
	}, {
		"pos": "NOUN",
		"start": 74,
		"i": 13,
		"lemma": "carbon",
		"tag": "NN",
		"text": "carbon",
		"dep": "compound"
	}, {
		"concepts": {
			"concepts": ["http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/sbmi.uth.tmc.edu\/ontology\/ochv#2393", "http:\/\/purl.allotrope.org\/ontologies\/material#AFM_0001116", "http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.bioontology.org\/ontology\/UATC\/V03AN02", "http:\/\/purl.bioontology.org\/ontology\/CSP\/2604-9089", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/11\/msv\/carbonDioxideConcentration"],
			"text": "carbon dioxide"
		},
		"pos": "NOUN",
		"start": 81,
		"i": 14,
		"lemma": "dioxide",
		"tag": "NN",
		"text": "dioxide",
		"dep": "pobj"
	}],
	"tree": {
		"left": [{
			"concepts": {
				"concepts": ["http:\/\/ontology.apa.org\/apaonto\/termsonlyOUT%20(5).owl#Scientists", "http:\/\/localhost\/plosthes.2017-1#8580"],
				"text": "Scientists"
			},
			"pos": "NOUN",
			"start": 0,
			"i": 0,
			"lemma": "scientist",
			"tag": "NNS",
			"text": "Scientists",
			"dep": "nsubjpass"
		}, {
			"pos": "AUX",
			"start": 11,
			"i": 1,
			"lemma": "have",
			"tag": "VBP",
			"text": "have",
			"dep": "aux"
		}, {
			"pos": "AUX",
			"start": 16,
			"i": 2,
			"lemma": "be",
			"tag": "VBN",
			"text": "been",
			"dep": "auxpass"
		}],
		"pos": "VERB",
		"start": 21,
		"i": 3,
		"lemma": "think",
		"right": [{
			"left": [{
				"pos": "PART",
				"start": 29,
				"i": 4,
				"lemma": "to",
				"tag": "TO",
				"text": "to",
				"dep": "aux"
			}],
			"pos": "VERB",
			"start": 32,
			"i": 5,
			"lemma": "believe",
			"right": [{
				"left": [{
					"pos": "SCONJ",
					"start": 40,
					"i": 6,
					"lemma": "that",
					"tag": "IN",
					"text": "that",
					"dep": "mark"
				}, {
					"concepts": {
						"concepts": ["http:\/\/purl.obolibrary.org\/obo\/ExO_0000014", "http:\/\/purl.obolibrary.org\/obo\/ENVO_01000629"],
						"text": "climate change"
					},
					"left": [{
						"pos": "NOUN",
						"start": 45,
						"i": 7,
						"lemma": "climate",
						"tag": "NN",
						"text": "climate",
						"dep": "compound"
					}],
					"pos": "NOUN",
					"start": 53,
					"i": 8,
					"lemma": "change",
					"tag": "NN",
					"text": "change",
					"dep": "nsubjpass"
				}, {
					"pos": "AUX",
					"start": 60,
					"i": 9,
					"lemma": "be",
					"tag": "VBZ",
					"text": "is",
					"dep": "auxpass"
				}],
				"pos": "VERB",
				"start": 63,
				"i": 10,
				"lemma": "cause",
				"right": [{
					"pos": "ADP",
					"start": 70,
					"i": 11,
					"lemma": "by",
					"tag": "IN",
					"text": "by",
					"dep": "agent"
				}],
				"tag": "VBN",
				"text": "caused",
				"dep": "ccomp"
			}],
			"tag": "VB",
			"text": "believe",
			"dep": "xcomp"
		}, {
			"pos": "SPACE",
			"start": 73,
			"i": 12,
			"lemma": " ",
			"right": [{
				"concepts": {
					"concepts": ["http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/sbmi.uth.tmc.edu\/ontology\/ochv#2393", "http:\/\/purl.allotrope.org\/ontologies\/material#AFM_0001116", "http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.bioontology.org\/ontology\/UATC\/V03AN02", "http:\/\/purl.bioontology.org\/ontology\/CSP\/2604-9089", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/11\/msv\/carbonDioxideConcentration"],
					"text": "carbon dioxide"
				},
				"left": [{
					"pos": "NOUN",
					"start": 74,
					"i": 13,
					"lemma": "carbon",
					"tag": "NN",
					"text": "carbon",
					"dep": "compound"
				}],
				"pos": "NOUN",
				"start": 81,
				"i": 14,
				"lemma": "dioxide",
				"tag": "NN",
				"text": "dioxide",
				"dep": "pobj"
			}],
			"tag": "_SP",
			"text": " ",
			"dep": "dep"
		}],
		"tag": "VBN",
		"text": "thought",
		"dep": "ROOT"
	},
	"id": "s_0",
	"text": "Scientists have been thought to believe that climate change is caused by  carbon dioxide"
} 
 */
