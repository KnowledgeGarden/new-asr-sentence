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
		environment.logDebug("SentenceEngineStarting\n"+sentence.getData());
		JsonObject jo;		
		JsonArray spacyData = sentence.getSpacyData();
		String text = sentence.getText();
		JsonArray ja;
		try {
			IResult r = spacy.processSentence(text);
			String json = (String)r.getResultObject();
			environment.logDebug("PS1 "+json);
			//{"data":[[],[{"strt":2,"enx":3,"txt":"encourages"}]],
			//"dbp":[],"wkd":[],
			//"nns":[{"strt":1,"txt":"shit"},{"strt":3,"txt":"flies"}],"pnns":[],
			//"vrbs":[{"strt":2,"txt":"encourages"}]}
			JsonObject spcy = util.parse(json);
			//JsonObject spacyObj;
			//JsonArray sentences =null;
			JSONObject res = null;
			if (spacyData == null) {
				environment.logDebug("SentenceEngine-x ");
			//This is the big spaCy full parse, etc
				r = spacyServerEnvironment.processParagraph(text);
				environment.logDebug("SentenceEngine-y\n"+r.getResultObject());
				
				spacyData = (JsonArray)r.getResultObject();
				sentence.setSpacyData(spacyData);
				
				////////////////////////////////
				// NOTE
				// sentences theoretically contains a dozen sentence objects,
				// one for each model
				// IF spaCy fails, it will be empty
				////////////////////////////////
				
				//spacyObj is a sentence object
				environment.logDebug("SentenceEngine-yy\n"+spacyData);
			} 
			//spacyData ==> [ {concepts, sentences}, {concepts, sentences},...]
			
			//Object foo = spacyObj.get("sentences");
			environment.logDebug("SentenceEngine-z "+spacyData);
			//{"nodes":[{"pos":"NOUN","star
			//res = (JSONObject)spacyObj.get("results");
			//environment.logDebug("SentenceEngine-0 "+res);
			//JSONArray spacyArray = (JSONArray)res.get("sentences");
			//res = (JSONObject)spacyArray.get(0);
			//environment.logDebug("SentenceEngine-1 "+foo);
			//{"text":"Elephant shit encourages flies","results":{"c
			JsonArray preds = spcy.get("data").getAsJsonArray();
			r = nounAssem.bigDamnAnalyze(sentence, spacyData, spcy);
			// process verbs
			if (spcy.get("vrbs") != null) {
				ja = spcy.get("vrbs").getAsJsonArray();
				processVerb(sentence, ja);
			}
			predAssem.processSentencePredicates(sentence, preds);

			
			// and now, the wordgrams
			r = builder.processSentence(sentence);
			environment.logDebug("SentenceEngineDone\n"+sentence.getData());
			// and now, send the results on to the ne
			//TODO
		} catch(Exception e) {
			
		}
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


