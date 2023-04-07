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
		if (spacyData == null) {
			r = spacyServerEnvironment.processSentence(text);
			spacyObj = (JSONObject)r.getResultObject();
			JSONObject res = (JSONObject)spacyObj.get("results");
			JSONArray spacyArray = (JSONArray)res.get("sentences");
			res = (JSONObject)spacyArray.get(0);
			environment.logDebug("SentenceEngine-1 "+res);
			sentence.setSpacyData(res.toJSONString());
		}
		try {
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


