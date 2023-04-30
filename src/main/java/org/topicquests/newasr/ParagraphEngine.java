/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

import java.util.ArrayList;
import java.util.List;

import org.topicquests.newasr.SentenceEngine.SentenceThread;
import org.topicquests.newasr.api.IExpectationTypes;
import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.api.IParagraphDataProvider;
import org.topicquests.newasr.api.IParagraphModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ISentenceModel;
import org.topicquests.newasr.impl.ASRParagraph;
import org.topicquests.newasr.impl.ASRSentence;
import org.topicquests.newasr.impl.PostgresParagraphDatabase;
import org.topicquests.newasr.kafka.CommonKafkaProducer;
import org.topicquests.newasr.para.ParagraphHandler;
import org.topicquests.os.asr.driver.sp.SpacyDriverEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class ParagraphEngine {
	private ASREnvironment environment;
	private CommonKafkaProducer sentenceProducer;
	private SpacyDriverEnvironment spacyServerEnvironment;
	private ParagraphHandler paraHandler;
	private SentenceEngine sentenceEngine;
	private BulletinBoard bulletinBoard;
	private IParagraphModel paragraphModel;
	private ISentenceModel sentenceModel;
	private List<JsonObject> paragraphs;
	private boolean IS_RUNNING = true;
	private ParagraphThread runner;
	private final String SENTENCE_TOPIC, SENTENCE_KEY;

	/**
	 * 
	 */
	public ParagraphEngine(ASREnvironment env) {
		environment =env;
		paragraphs = new ArrayList<JsonObject>();
		sentenceProducer = environment.getSentenceProducer();
		spacyServerEnvironment = environment.getSpacyServer();
		sentenceEngine = environment.getSentenceEngine();
		paraHandler = environment.getParagraphHandler();
		bulletinBoard = environment.getBulletinBoard();
		paragraphModel = environment.getParagraphModel();
		sentenceModel = environment.getSentenceModel();
		String pTopic = (String)environment.getKafkaTopicProperties().get("SentenceProducerTopic");
		SENTENCE_TOPIC = pTopic;
		SENTENCE_KEY = "data"; 		//TODO FIXME

	}

	/**
	 * 
	 * @param paragraph of the form {@link IParagraph}
	 * @return
	 */
	public boolean addParagraph(JsonObject paragraph) {
		environment.logDebug("AcceptingParagraph\n"+paragraph);

		synchronized(paragraphs) {
			paragraphs.add(paragraph);
			paragraphs.notify();
		}
		return true; // default

	}
	public void startProcessing() {
		IS_RUNNING = true;
		runner = new ParagraphThread();
		runner.start();
		System.out.println("StartingParagraphEngine");
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

	/**
	 * <p>The process is <br/>
	 * <ul><li>run the paragraph text through spaCy</li>
	 * <li>run the paragraph through ParagraphHandler</li>
	 * <li>post results on BulletinBoard</li>
	 * <li>For each sentence, pass it to SentenceEngine</li></ul></p>
	 * @param p
	 */
	void processParagraph(IParagraph p) {
		String text = p.getText();
		IResult r = paragraphModel.putParagraph(p);
		// store the paragraph to get its Id
		long paragraphId = ((Long)r.getResultObject()).longValue();
		//long paragraphId = p.getId();
		JsonArray spacyData;
		// coreferences
		r = paraHandler.findCoreferences(text);
		Object a = r.getResultObject(); //corefs
		Object b = r.getResultObjectA(); //mentions
		if (a != null)
			bulletinBoard.setCoreferenceChains((JsonArray)a);
		if (b != null)
			bulletinBoard.setMentions((JsonArray)b);
		//spacy
		r = spacyServerEnvironment.processParagraph(text);
		//environment.logDebug("SentenceEngine-y\n"+r.getResultObject());
		
		spacyData = (JsonArray)r.getResultObject();
		// spacyData must now be broken into separate sentences
		// each sentence must be stored in sentence database to retrieve its id
	}
	class ParagraphThread extends Thread {
		
		public void run() {
			JsonObject sent = null;
			while (IS_RUNNING) {
				synchronized(paragraphs) {
					while (paragraphs.isEmpty()) {
						try {
							paragraphs.wait();
						} catch (Exception e) {}
					}
					sent = paragraphs.remove(0);
				}
				if (sent != null) {
					IParagraph s = new ASRParagraph(sent);
					processParagraph(s);
				}
			}
		}
	}
	public void shutDown() {
		synchronized(paragraphs)  {
			IS_RUNNING = false;
			paragraphs.notify();
		}
	}
}
