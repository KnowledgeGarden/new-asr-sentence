/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

import org.topicquests.newasr.api.IExpectationTypes;
import org.topicquests.newasr.api.IParagraphDataProvider;
import org.topicquests.newasr.api.ISentence;
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
	private BulletinBoard bulletinBoard;
	private IParagraphDataProvider database;
	
	private final String SENTENCE_TOPIC, SENTENCE_KEY;

	/**
	 * 
	 */
	public ParagraphEngine(ASREnvironment env) {
		environment =env;
		sentenceProducer = environment.getSentenceProducer();
		spacyServerEnvironment = environment.getSpacyServer();
		paraHandler = environment.getParagraphHandler();
		bulletinBoard = environment.getBulletinBoard();
		database = new PostgresParagraphDatabase(environment);
		String pTopic = (String)environment.getKafkaTopicProperties().get("SentenceProducerTopic");
		SENTENCE_TOPIC = pTopic;
		SENTENCE_KEY = "data"; 		//TODO FIXME

	}

	public IResult processParagraph(long docId, String paragraph) {
		IResult result = new ResultPojo();
		
		return result;
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

}
