/**
 * 
 */
package org.topicquests.newasr;

import java.util.ArrayList;
import java.util.List;

import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.impl.ASRSentence;
import org.topicquests.newasr.kafka.SentenceProducer;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.os.asr.driver.sp.SpacyDriverEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonObject;

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
	//private SpacyDriverEnvironment spacyServerEnvironment;
	private SentenceProducer sentenceProducer;
	
	private final String SENTENCE_TOPIC, SPACY_TOPIC, EXPECTATION_TOPIC, SPACY_KEY, SENTENCE_KEY;
	private final Integer partition;

	/**
	 * 
	 */
	public SentenceEngine(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		sentences = new ArrayList<JsonObject>();
		util = new JsonUtil();
		//spacyServerEnvironment = environment.getSpacyServerEnvironment();
		sentenceProducer = environment.getSentenceProducer();
		String pTopic = (String)environment.getKafkaTopicProperties().get("SentenceProducerTopic");
		SENTENCE_TOPIC = pTopic;
		EXPECTATION_TOPIC = (String)environment.getKafkaTopicProperties().get("ExpectationFailureTopic");
		SPACY_KEY = "data"; 		//TODO FIXME
		SENTENCE_KEY = "data"; 		//TODO FIXME
		partition = new Integer(0);	//TODO FiXME
		pTopic = (String)environment.getKafkaTopicProperties().get("SentenceSpacyProducerTopic");
		SPACY_TOPIC = pTopic;
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
		System.out.println("ProcessSentence\n"+sentence.getData());
		// gather predicates, wikidata and dbpedia stuff in the sentence object
		sentenceProducer.sendMessage(SPACY_TOPIC, sentence.getData().toString(), SPACY_KEY, partition);
		//@see acceptSpacyResponse below				
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
