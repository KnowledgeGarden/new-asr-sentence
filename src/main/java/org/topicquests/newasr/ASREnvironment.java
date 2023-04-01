/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

import java.util.Map;

import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.newasr.api.IAsrDataProvider;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IDictionary;
import org.topicquests.newasr.api.IDictionaryClient;
import org.topicquests.newasr.api.IKafkaDispatcher;
import org.topicquests.newasr.dictionary.DictionaryHttpClient;
import org.topicquests.newasr.dictionary.DictionaryClient;
import org.topicquests.newasr.impl.ASRModel;
import org.topicquests.newasr.impl.SentenceListener;
import org.topicquests.newasr.impl.SpacyListener;
import org.topicquests.newasr.impl.PostgresWordGramGraphProvider;
import org.topicquests.newasr.kafka.KafkaHandler;
import org.topicquests.newasr.kafka.SentenceProducer;
import org.topicquests.newasr.pred.PredicateAssembler;
import org.topicquests.newasr.wg.WordGramBuilder;
import org.topicquests.newasr.wg.WordGramUtil;
import org.topicquests.os.asr.driver.sp.SpacyDriverEnvironment;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.support.RootEnvironment;
import org.topicquests.support.config.Configurator;


/**
 * @author jackpark
 *
 */
public class ASREnvironment extends RootEnvironment {
	private PostgresConnectionFactory dbDriver = null;
	private IDictionaryClient dictionarHttpyClient;
	private IDictionary dictionary;
	private IAsrModel model;
	private IAsrDataProvider database;
	private WordGramUtil wgUtil;
	private PredicateAssembler predAssem;
	private WordGramBuilder builder;
	private KafkaHandler sentenceConsumer;
	private KafkaHandler spacyConsumer;
	private Map<String,Object>kafkaProps;
	private IKafkaDispatcher sentenceListener;
	private IKafkaDispatcher spacyListener;
	//private SpacyDriverEnvironment spacyServerEnvironment;
	private SentenceEngine sentenceEngine;
	private SentenceProducer sentenceProducer;
	public static final String AGENT_GROUP = "Sentence";

	/**
	 * 
	 */
	public ASREnvironment() {
		super("asr-sentence-config.xml", "logger.properties");
		String schemaName = getStringProperty("DatabaseSchema");
		String dbName = getStringProperty("DatabaseName");
		dbDriver = new PostgresConnectionFactory(dbName, schemaName);
		dictionarHttpyClient = new DictionaryHttpClient(this);
		dictionary = new DictionaryClient(this);
		database = new PostgresWordGramGraphProvider(this);
		model = new ASRModel(this);
		wgUtil = new WordGramUtil(this);
		kafkaProps = Configurator.getProperties("kafka-topics.xml");
		sentenceListener = new SentenceListener(this);
		spacyListener = new SpacyListener(this);
		String cTopic = (String)kafkaProps.get("SentenceConsumerTopic");
		sentenceConsumer = new KafkaHandler(this, (IMessageConsumerListener)sentenceListener, cTopic, AGENT_GROUP);
		cTopic = (String)kafkaProps.get("SentenceSpacyConsumerTopic");
		//pTopic = (String)kafkaProps.get("SentenceSpacyProducerTopic");
		spacyConsumer = new KafkaHandler(this, (IMessageConsumerListener)spacyListener, cTopic, AGENT_GROUP);
		sentenceProducer = new SentenceProducer(this, AGENT_GROUP);
		predAssem = new PredicateAssembler(this);
		builder = new WordGramBuilder(this);
		//spacyServerEnvironment = new SpacyDriverEnvironment();
		sentenceEngine = new SentenceEngine(this);
		
		sentenceEngine.startProcessing();
		
		// shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread()
	    {
	      public void run()
	      {
	        shutDown();
	      }
	    });
	}
	
	public WordGramBuilder getWordGramBuilder() {
		return builder;
	}
	public SentenceEngine getSentenceEngine() {
		return sentenceEngine;
	}
	
	public SentenceProducer getSentenceProducer() {
		return sentenceProducer;
	}
	/**
	 * There are two spaCy systems in the present code:
	 * one is on an http service, the other is over kafka
	 * @return
	 */
	//public SpacyDriverEnvironment getSpacyServerEnvironment() {
	//	return spacyServerEnvironment;
	//}
	
	public KafkaHandler getSentenceConsumer () {
		return sentenceConsumer;
	}
	public KafkaHandler getSpacyConsumer () {
		return spacyConsumer;
	}
	public PredicateAssembler getPredicateAssembler() {
		return predAssem;
	}
	public Map<String, Object> getKafkaTopicProperties() {
		return kafkaProps;
	}

	public IAsrDataProvider getDatabase() {
		return database;
	}
	public IDictionaryClient getDictionaryClient() {
		return dictionarHttpyClient;
	}
	public IDictionary getDictionary() {
		return dictionary;
	}
	public IAsrModel getModel() {
		return model;
	}
	public PostgresConnectionFactory getDatabaseDriver() {
		return dbDriver;
	}
	
	@Override
	public void shutDown() {
		System.out.println("Shutting down");
		sentenceEngine.shutDown();
		sentenceConsumer.shutDown();
		spacyConsumer.shutDown();

	}

}
