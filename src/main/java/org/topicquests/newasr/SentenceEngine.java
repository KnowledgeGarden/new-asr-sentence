/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

import java.util.ArrayList;
import java.util.List;

import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IExpectationTypes;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.impl.ASRSentence;
import org.topicquests.newasr.kafka.CommonKafkaProducer;
import org.topicquests.newasr.noun.NounAssembler;
import org.topicquests.newasr.pred.PredicateAssembler;
import org.topicquests.newasr.spacy.SpacyHttpClient;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.newasr.wg.WordGramBuilder;
import org.topicquests.os.asr.driver.sp.SpacyDriverEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class SentenceEngine {
	private ASREnvironment environment;
	private IAsrModel model;
	private JsonUtil util;
	private List<JsonObject> sentences;
	private boolean IS_RUNNING = true;
	private SentenceThread runner;
	private SpacyDriverEnvironment spacyServerEnvironment;
	private PredicateAssembler predAssem;
	private NounAssembler nounAssem;
	private WordGramBuilder builder;

	private CommonKafkaProducer sentenceProducer;
	private SpacyHttpClient spacy;
	
	private final String SENTENCE_TOPIC, SENTENCE_KEY;

	/**
	 * 
	 */
	public SentenceEngine(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		predAssem = environment.getPredicateAssembler();
		nounAssem = new NounAssembler(environment);
		builder = environment.getWordGramBuilder();
		sentences = new ArrayList<JsonObject>();
		spacy = new SpacyHttpClient(environment);
		util = new JsonUtil();
		spacyServerEnvironment = new SpacyDriverEnvironment();
		sentenceProducer = environment.getSentenceProducer();
		String pTopic = (String)environment.getKafkaTopicProperties().get("SentenceProducerTopic");
		SENTENCE_TOPIC = pTopic;
		SENTENCE_KEY = "data"; 		//TODO FIXME

	}

	public void startProcessing() {
		IS_RUNNING = true;
		runner = new SentenceThread();
		runner.start();
		System.out.println("StartingEngine");
	}
	
	public boolean acceptNewSentence(JsonObject newSentence) {
		System.out.println("AcceptingSentence "+newSentence);

		synchronized(sentences) {
			sentences.add(newSentence);
			sentences.notify();
		}
		return true; // default
	}
	
	/**
	 * <p>Process a {@code sentence}</p>
	 * @param sentence
	 */
	public void processSentence(ISentence sentence) {
		//In theory, sentence arrives as a string and sentenceId with spacy POS parsing, etc
		//First, send it to the spacy predicate server
		String text = sentence.getText();
		System.out.println("ProcessSentence\n"+sentence.getData());
		// gather predicates, wikidata and dbpedia stuff in the sentence object
		//sentenceProducer.sendMessage(SPACY_TOPIC, sentence.getData().toString(), SPACY_KEY, partition);
		//@see acceptSpacyResponse below	
		IResult r = spacy.processSentence(text);
		String json = (String)r.getResultObject();
		environment.logDebug("PS1 "+json);
		JsonObject jo;
		JsonArray ja, jax;
		JSONObject spacyObj;
		
		// POS and more
		String spacyData = sentence.getSpacyData();
		//environment.logError("BIGJA "+spacyData, null);
		try {
			JSONObject res = null;
			if (spacyData == null) {
				//This is the big spaCy full parse, etc
				r = spacyServerEnvironment.processSentence(text);
				spacyObj = (JSONObject)r.getResultObject();
			} else {
				JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
				spacyObj = (JSONObject)p.parse(spacyData);
			}
			res = (JSONObject)spacyObj.get("results");
			JSONArray spacyArray = (JSONArray)res.get("sentences");
			res = (JSONObject)spacyArray.get(0);
			environment.logDebug("SentenceEngine-1 "+res);
			sentence.setSpacyData(res.toJSONString());
			JsonArray concepts = findConcepts(res);
			// spacy predicates, dbp, nouns, etc
			jo = util.parse(json);
			ja = jo.get("data").getAsJsonArray();
			// process the predicates
			predAssem.processSentencePredicates(sentence, ja);
			ja = jo.get("dbp").getAsJsonArray();
			// process dbpedia
			processDBpedia(sentence, ja);
			ja = jo.get("wkd").getAsJsonArray();
			// process wikidata
			processWikidata(sentence, ja);
			// process nouns
			if (jo.get("nns") != null) {
				ja = jo.get("nns").getAsJsonArray();
				processNoun(sentence, ja);
			}
			// process propernouns
			if (jo.get("pnns") != null) {
				ja = jo.get("pnns").getAsJsonArray();
				processProperNoun(sentence, ja);
			}
			// process verbs
			if (jo.get("vrbs") != null) {
				ja = jo.get("vrbs").getAsJsonArray();
				processVerb(sentence, ja);
			}
			resolveNouns(sentence);
			// and now, the wordgrams
			r = builder.processSentence(sentence);
			environment.logDebug("SentenceEngineDone\n"+sentence.getData());
			// and now, send the results on to the ne
			//TODO
		} catch (Exception e) {
			environment.logError("SE-1: "+e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	JsonArray findConcepts(JSONObject data) {
		JsonArray result = new JsonArray();
		if (data == null)
			return result;
		JSONArray nodes = (JSONArray)data.get("nodes");
		environment.logDebug("SentenceEngineFindConcept-1\n"+nodes);
		JSONObject jo, conc;
		int len = nodes.size();
		int conlen = 0;
		JSONObject theCon;
		JSONArray them;
		for (int i=0;i<len;i++) {
			jo = (JSONObject)nodes.get(i);
			conc = (JSONObject)jo.get("concepts");
			if (conc != null) {
				theCon = (JSONObject)conc.get("concepts");
			}
		}
		
		return result;
	}
	
	void resolveNouns(ISentence sentence) {
		JsonArray result = new JsonArray();
		JsonArray nouns = sentence.getNouns();
		JsonArray pNouns = sentence.getProperNouns();
		JsonArray dbp = sentence.getDBpediaData();
		environment.logDebug("RESOLVING\n"+dbp);
		int len, len2;
		JsonObject jo, jx;
		if ((nouns != null) && (pNouns != null)) {
			len = pNouns.size();
			for (int i=0;i<len;i++) {
				jo = pNouns.get(i).getAsJsonObject();
				if (!nouns.contains(jo))
					nouns.add(jo);
			}
		}
		// isolate DBpedia objects first
		String txt;
		JsonObject match;
		JsonArray toRemove = new JsonArray();
		if (dbp != null) {
			len = dbp.size();
			for (int i=0;i<len;i++) {
				jo = dbp.get(i).getAsJsonObject();
				txt = jo.get("strt").getAsString();
				IResult rx = match(txt, nouns);
				environment.logDebug("RESOLVING-1 "+rx+"\n"+jo);
				if (rx != null) {
					match = (JsonObject)rx.getResultObject();
					JsonArray droppers = (JsonArray)rx.getResultObjectA();
					if (match != null)
						result.add(match);
					if (droppers != null) {
						int lx = droppers.size();
						for (int j = 0;j<lx;j++)
							toRemove.add(droppers.get(j));
					}
				}
				
			}
		}
		environment.logDebug("RESOLVING-1a\n"+toRemove);
		len = toRemove.size();
		for (int i=0;i<len;i++)
			nouns.remove(toRemove.get(i));
		// Add what's left
		environment.logDebug("RESOLVING-2\n"+result);
		len = nouns.size();
		for (int i=0;i<len;i++) {
			jo = nouns.get(i).getAsJsonObject();
			if (!result.contains(jo))
				result.add(jo);
		}
		environment.logDebug("RESOLVING+\n"+result);
		sentence.setResolvedNouns(result);
	}
	
	IResult match(String txt, JsonArray nouns) {
		IResult output = new ResultPojo();
		JsonObject result = new JsonObject();
		JsonArray droppers = new JsonArray();
		output.setResultObject(result);
		output.setResultObjectA(droppers);
		String comp = txt.toLowerCase().trim();
		int len = nouns.size();
		JsonObject temp;
		String label;
		String [] textC = txt.split(" ");
		int numWords = textC.length;
		JsonObject jo;
		int strt = 0;
		for (int i=0;i<len;i++) {
			temp = nouns.get(i).getAsJsonObject();
			environment.logDebug("MATCHING "+txt+"\n"+temp);
			label = temp.get("txt").getAsString().toLowerCase();
			strt = temp.get("strt").getAsJsonPrimitive().getAsInt();
			// exact match
			if (label.equals(comp)) {
				result.addProperty("strt", Integer.toString(strt));
				result.addProperty("txt", txt);
				return output;
			} else { // speculative check - are the next words compatible?
				if (comp.contains(label)) {
					boolean found = true;
					droppers.add(temp);
					for (int j = 1; j<numWords;j++) {
						jo = nouns.get(++i).getAsJsonObject();
						environment.logDebug("MATCHING-2 "+txt+"\n"+jo);
						label = jo.get("txt").getAsString().toLowerCase();
						environment.logDebug("MATCHING-3 "+txt+" "+label);
						droppers.add(jo);
						if (!comp.contains(label)) {
							environment.logDebug("MATCHING-4 ");
							found = false;
							break;
						}

					}
					if (found) {
						result.addProperty("strt", Integer.toString(strt));
						result.addProperty("txt", txt);
						return output;
					} else
						return null;
				}
			}
		}
		environment.logDebug("MATCHING+ ");

		return null;
	}
	
	void processDBpedia(ISentence sentence, JsonArray dbp) {
		if (dbp != null)
			sentence.setDBpediaData(dbp);
	}
	void processWikidata(ISentence sentence, JsonArray wd) {
		if (wd != null)
			sentence.setWikiData(wd);
	}
	void processNoun(ISentence sentence, JsonArray noun) {
		if (noun != null)
			sentence.setNoun(noun);
	}
	void processProperNoun(ISentence sentence, JsonArray noun) {
		if (noun != null)
			sentence.setProperNoun(noun);
	}
	void processVerb(ISentence sentence, JsonArray verb) {
		if (verb != null)
			sentence.setVerb(verb);
	}

	/**
	 * <p>The workhorse: called by way of the model from kafka from spaCy</p>
	 * <p>This is where we build wordgrams which prepare for the next agent, and ship
	 * to Kafka for the next agent to process</p>
	 * @param sentence
	 * @return
	 */
	public boolean acceptSpacyResponse(JsonObject sentence) {
		boolean result = true; // default = success
		ISentence sx = new ASRSentence(sentence);
		JsonArray preds =sx.getPredicatePhrases();
		if (preds == null) 
			environment.sendExpectationFailureEvent(IExpectationTypes.NO_PREDICATE_DETECTED, sentence.toString());
		// TODO Auto-generated method stub
		return result;
	}

	
	class SentenceThread extends Thread {
		
		public void run() {
			JsonObject sent = null;
			while (IS_RUNNING) {
				synchronized(sentences) {
					while (sentences.isEmpty()) {
						try {
							sentences.wait();
						} catch (Exception e) {}
					}
					sent = sentences.remove(0);
				}
				if (sent != null) {
					ISentence s = new ASRSentence(sent);
					processSentence(s);
				}
			}
		}
	}
	public void shutDown() {
		synchronized(sentences)  {
			IS_RUNNING = false;
			sentences.notify();
		}
	}
}


