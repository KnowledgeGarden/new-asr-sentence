/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
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
import org.topicquests.newasr.spacy.SpacyUtil;
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
	//private BulletinBoard bulletinBoard;
	private IParagraphModel paragraphModel;
	private ISentenceModel sentenceModel;
	private List<JsonObject> paragraphs;
	private boolean IS_RUNNING = true;
	private ParagraphThread runner;
	private SpacyUtil spacyUtil;
	private final String SENTENCE_TOPIC, SENTENCE_KEY;

	/**
	 * 
	 */
	public ParagraphEngine(ASREnvironment env) {
		environment =env;
		spacyUtil = new SpacyUtil(environment);
		paragraphs = new ArrayList<JsonObject>();
		sentenceProducer = environment.getSentenceProducer();
		spacyServerEnvironment = environment.getSpacyServer();
		sentenceEngine = environment.getSentenceEngine();
		paraHandler = environment.getParagraphHandler();
		//bulletinBoard = environment.getBulletinBoard();
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
		environment.logError("StartingParagraph", null); //for debugging
		environment.logDebug("ParagraphEngine.processParagraph\n"+p.getData());
		String text = p.getText();
		IResult r = paragraphModel.putParagraph(p);
		// store the paragraph to get its Id
		environment.logDebug("ParagraphEngine.processParagraph-1 "+r.getErrorString());
		long paragraphId = ((Long)r.getResultObject()).longValue();
		long documentId = p.getDocumentId();
		environment.logDebug("ParagraphEngine.processParagraph-1a "+paragraphId);
		p.setId(paragraphId);
		//long paragraphId = p.getId();
		JsonArray spacyData;
		// coreferences
		r = paraHandler.findCoreferences(text);
		Object a = r.getResultObject(); //corefs
		Object b = r.getResultObjectA(); //mentions
		environment.logDebug("ParagraphEngine.processParagraph-2\n"+a+"\n"+b);
		BulletinBoard bb = new BulletinBoard(environment);
		if (a != null)
			bb.setCoreferenceChains((JsonArray)a);
		if (b != null)
			bb.setMentions((JsonArray)b);
		//spacy
		r = spacyServerEnvironment.processParagraph(text);
		//environment.logDebug("SentenceEngine-y\n"+r.getResultObject());
		
		spacyData = (JsonArray)r.getResultObject();
		// spacyData must now be broken into separate sentences
		// each sentence must be stored in sentence database to retrieve its id
		environment.logDebug("ParagraphEngine.processParagraph-3 "+spacyData.size());
		/*int len = spacyData.size();
		JsonObject jo;
		JsonArray sentences;
		for (int i=0;i<len;i++) {
			//for each object
			jo = spacyData.get(i).getAsJsonObject();
			sentences = jo.get("sentences").getAsJsonArray();
		}*/
		IResult spacySentences = spacyUtil.grabSentences(spacyData);
		//environment.logDebug("SpacyGot\n"+spacySentences.getResultObject()+"\n"+spacySentences.getResultObjectA());
		JsonObject sentences = (JsonObject)spacySentences.getResultObject();
		

		JsonArray sentenceConcepts = (JsonArray)spacySentences.getResultObjectA();
		if (sentenceConcepts != null)
			p.setParagraphConcepts(sentenceConcepts);
		if (sentences != null) {
			List<ISentence> mySentences = new ArrayList<ISentence>();
			ISentence theSentence;
			Iterator<String> itr = sentences.keySet().iterator();
			String sid;
			JsonArray sentx;
			JsonObject sentObj, theSent;
			String txt;
			int lenx;
			while (itr.hasNext()) {
				sid = itr.next();
				environment.logDebug("ParagraphEngineProcessing "+sid);
				sentx = sentences.get(sid).getAsJsonArray();
				lenx = sentx.size(); // not all will be same size where model did not break sentences properly
				if (lenx > 0) {
					// sanity check- in theory,we already did this in SpacyUtil
					sentObj =sentx.get(0).getAsJsonObject();
					if (hasVerb(sentObj)) {
						theSent = sentObj.get("sentence").getAsJsonObject();

						txt = theSent.get("text").getAsString();
						theSentence = makeSentence(sentx, txt, paragraphId, documentId);
						System.out.println("FUUUY "+theSentence.getId()+"\n"+theSentence.getData()+"\n"+p.getData());
						p.addSentenceId(theSentence.getId());
						sentenceEngine.acceptNewSentence(theSentence.getData(), bb);
						
					} else
						environment.logDebug("ParagraphEngineRejected\n"+sentObj);
				}
			}
		}
		r = paragraphModel.updateParagraph(p);
	}
	
	ISentence makeSentence(JsonArray spacyData, String text, long paragraphId, long documentId) {
		environment.logDebug("MAKINGSENTENCE "+text);
		ISentence result = new ASRSentence();
		result.setText(text);
		result.setSpacyData(spacyData);
		result.setParagraphId(paragraphId);
		result.setDocumentId(documentId);
		IResult r = sentenceModel.putSentence(result);
		long id = ((Long)r.getResultObject()).longValue();
		result.setId(id);
		return result;
	}
	
	boolean hasVerb(JsonObject sentence) {
		//environment.logDebug("HASVERBS\n"+sentence);
		JsonObject s= sentence.get("sentence").getAsJsonObject();
		JsonArray nodes = s.get("nodes").getAsJsonArray();
		int len = nodes.size();
		JsonObject n;
		String pos;
		for (int i=0;i<len;i++) {
			n =nodes.get(i).getAsJsonObject();
			pos = n.get("pos").getAsString();
			if (pos.equals("VERB")||pos.equals("AUX"))
				return true;
		}
		
		return false;
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
