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
		tripleModel = environment.getTripleModel();
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
		JsonArray predicates = sentence.getPredicatePhrases();
		JsonArray resolvedNouns = sentence.getResolvedNouns();
		Iterator<JsonElement> itr;
		//JsonObject jo;
		//JsonArray ja;
		//String snippet, idx;
		//int loc;
		//IWordGram wg;
		IResult r;
		
		
		JsonArray ta = analyzer.bigDamnAnalyze(sentence, predicates, resolvedNouns);
		if (ta != null)
			sentence.setSimpleTriples(ta);
		environment.logDebug("WGBuild\n"+ta);
		
		int len = ta.size();
		JsonObject trix;
		ISimpleTriple strix;
		for (int i=0;i<len;i++) {
			trix = ta.get(i).getAsJsonObject();
			environment.logDebug("WGBuild-1\n"+trix);
			strix = processSimpleTriple(sentenceId, trix);
			environment.logDebug("BigWGBuild\n"+trix+"\n"+strix.getData());
		}
		
		
		
		return result;
	}
	
	String getTripleText(ISimpleTriple trip) {
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
		long oid, sid;
		IResult r;
		IWordGram wg;
		if (sIsWG) {
			subjectText = trip.getSubjectText();
			if (oIsWG) {
				objectText = trip.getObjectText();
			} else {
				oid = trip.getObjectId();
				r = model.getThisTermById(Long.toString(oid));
				wg = (IWordGram)r.getResultObject();
				objectText = wg.getWords(LANG);
			}
		} else {
			sid = trip.getSubjectId();
			r = model.getThisTermById(Long.toString(sid));
			wg = (IWordGram)r.getResultObject();
			subjectText = wg.getWords(LANG);
			if (oIsWG) {
				objectText = trip.getObjectText();
			} else {
				oid = trip.getObjectId();
				r = model.getThisTermById(Long.toString(oid));
				wg = (IWordGram)r.getResultObject();
				objectText = wg.getWords(LANG);
			}
		}
		buf.append("subj: "+subjectText+", pred: "+predText+", obj: "+objectText);
		buf.append(" }");
		return buf.toString().trim();
	}
	
	/**
	 * Recursive
	 * <p>The game here is to take an object which is composed of features like<br/>
	 * {@code {startPosition, text}} and convert the text to an {@link IWordGram}</p>
	 * <p>The process is complex because a subject or an object - or both - can be a
	 * a triple, which makes this a recursive process</p>
	 * @param triple
	 */
	ISimpleTriple processSimpleTriple(long sentenceId, JsonObject triple) {
		environment.logDebug("ProcessSimpleTriple\n"+triple);
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
		// process the predicate first
		IWordGram predicateGram;
		String predText = pred.get("txt").getAsString();
		/////////////////////
		// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// This is where we test for predicate existence
		// and Stop if it doesn't
		/////////////////////
		r = model.processTerm(predText, IPOS.VERB_POS);
		idx = (String)r.getResultObject();
		r = model.getThisTermById(idx);
		predicateGram = (IWordGram)r.getResultObject();
		// tense and epi are necessary for wordgram edges indexed by sentenceId
		String tense = predicateGram.getTense();
		String epi = predicateGram.getEpistemicStatus();
		long invId = predicateGram.getInverseTerm();
		long canonId = predicateGram.getCannonTerm();
		st.setPredicateId(predicateGram.getId());
		st.setPredicateText(predText);
		long tId;
		//Subject next
		if (sx.get("subj") == null) { // is it a triple?
			subjText = sx.get("txt").getAsString();
			r = model.processTerm(subjText, IPOS.NOUN_POS);
			idx = (String)r.getResultObject();
			r = model.getTermById(idx); // canonical form if exists
			wg = (IWordGram)r.getResultObject();	
			wg.setSentenceId(sentenceId, tense, epi);
			st.setSubjectId(wg.getId(), ISimpleTriple.WORDGRAM_TYPE);
			st.setSubjectText(subjText);
		} else { // it's a triple
			environment.logDebug("NestedSubject "+sx);
			ISimpleTriple theSubject = processSimpleTriple(sentenceId, sx); // recurse
			environment.logDebug("NestedSubject-1\n"+theSubject.getData());
			tId = theSubject.getId();
			subjText = getTripleText(theSubject);
			environment.logDebug("NestedSubject-2 "+tId+" "+subjText);
			st.setSubjectId(tId, ISimpleTriple.TRIPLE_TYPE);
			st.setSubjectText(subjText);
		}
		environment.logDebug("ProcessSimpleTriple-1\n"+st.getData());
		if (ox.get("subj") == null) { // nested object?
			subjText = ox.get("txt").getAsString();
			r = model.processTerm(subjText, IPOS.NOUN_POS);
			idx = (String)r.getResultObject();
			r = model.getTermById(idx); // returns canonical form if exists
			wg = (IWordGram)r.getResultObject();	
			wg.setSentenceId(sentenceId, tense, epi);
			st.setObjectId(wg.getId(), ISimpleTriple.WORDGRAM_TYPE);
			st.setObjectText(subjText);
		} else { // nested object
			environment.logDebug("NestedObject "+ox);
			ISimpleTriple theObject = processSimpleTriple(sentenceId, ox); // recurse
			tId = theObject.getId();
			environment.logDebug("NestedObject-1\n"+theObject.getData());
			subjText = getTripleText(theObject);
			environment.logDebug("NestedObject-2 "+tId+" "+subjText);
			st.setObjectId(tId, ISimpleTriple.TRIPLE_TYPE);
			st.setObjectText(subjText);
		}
		st.addSentenceId(sentenceId);
		// working or full tuple?
		r = tripleModel.getThisTuple(st);
		environment.logDebug("ProcessSimpleTriple-2\n"+r.getResultObject()+"\n"+st.getData());
		Object o = r.getResultObject();
		ISimpleTriple foo;
		if (o == null) {
			r = tripleModel.getThisWorkingTuple(st);
			environment.logDebug("ProcessSimpleTriple-3\n"+r.getResultObject()+"\n"+st.getData());
			o = r.getResultObject();
			if (o == null) {
				environment.logDebug("ProcessSimpleTriple-4 "+invId+" "+canonId);
			// it does not exist in the database
				if (invId == -1 && canonId == -1) {
					r = tripleModel.putTuple(st);
					tId = ((Long)r.getResultObject()).longValue();
					st.setId(tId);
				} else if (invId > -1 || canonId > -1) {
					environment.logDebug("ProcessSimpleTriple-5 "+invId+" "+canonId);
					//make a canonical form, save that, grab it's id, then save that
					if (invId > -1) {
						environment.logDebug("ProcessSimpleTriple-6 "+invId+" "+canonId);
						// this is an inverted gram
						r = model.getTermById(Long.toString(invId));
						IWordGram newPred = (IWordGram)r.getResultObject();
						foo = new ASRSimpleTriple();
						foo. setPredicateId(invId);
						foo.setPredicateText(newPred.getWords(LANG));
						foo.setSubjectId(st.getObjectId(), ISimpleTriple.WORDGRAM_TYPE);
						foo.setSubjectText(st.getObjectText());
						foo.setObjectId(st.getSubjectId(), ISimpleTriple.WORDGRAM_TYPE);
						foo.setObjectText(st.getSubjectText());
						foo.addSentenceId(sentenceId);

						r =tripleModel.putTuple(foo);
						tId = ((Long)r.getResultObject()).longValue();
						st.setNormalizedTripleId(tId);
						r = tripleModel.putWorkingTuple(st);
						
						environment.logDebug("ProcessSimpleTriple-6A\n"+st.getData()+"\n"+foo.getData());
						st = foo; // return the requested triple
					} else if (canonId > -1) {
						environment.logDebug("ProcessSimpleTriple-7 "+invId+" "+canonId);
						r = model.getTermById(Long.toString(canonId));
						IWordGram newPred = (IWordGram)r.getResultObject();
						foo = new ASRSimpleTriple();
						foo. setPredicateId(canonId);
						foo.setPredicateText(newPred.getWords(LANG));
						foo.setPredicateText(newPred.getWords(LANG));
						foo.setSubjectId(st.getSubjectId(), ISimpleTriple.WORDGRAM_TYPE);
						foo.setSubjectText(st.getSubjectText());
						foo.setObjectId(st.getObjectId(), ISimpleTriple.WORDGRAM_TYPE);
						foo.setObjectText(st.getObjectText());
						foo.addSentenceId(sentenceId);

						r =tripleModel.putTuple(foo);
						tId = ((Long)r.getResultObject()).longValue();
						st.setNormalizedTripleId(tId);
						r = tripleModel.putWorkingTuple(st);
						st = foo;
					}
					
				}
			} 
			else {
				// let's get its normalized version
				foo = (ISimpleTriple)o;
				tId=foo.getNormalizedTripleId();
				r = tripleModel.getTupleById(tId);
				st = (ISimpleTriple)r.getResultObject();
				r = tripleModel.addSentenceIdToTuple(sentenceId, st.getId());
			}

		} else {
			//add sentence Id to it
			foo = (ISimpleTriple)o;
			tId=foo.getId();
			r = tripleModel.addSentenceIdToTuple(sentenceId, tId);
		}
		environment.logDebug("ProcessSimpleTriple++\n"+st.getData());
		return st;
	}
	
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
