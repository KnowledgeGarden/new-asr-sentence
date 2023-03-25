/*
 * Copyright 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.kafka;

import org.topicquests.backside.kafka.consumer.StringConsumer;
import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.newasr.ASREnvironment;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 * 
 */
public class KafkaHandler {
	private ASREnvironment environment;
	private StringConsumer consumer;
	private KafkaProducer producer;
	private final boolean isRewind;
	private final int pollSeconds = 2;
	private final String
		CONSUMER_TOPIC,
		PRODUCER_TOPIC,
		PRODUCER_KEY,
		AGENT_GROUP = "foo"; //"BiomedSentenceAgent";

	/**
	 * 
	 */
	public KafkaHandler(ASREnvironment env, IMessageConsumerListener listener) {
		environment = env;
		String rw = environment.getStringProperty("ConsumerRewind");
		isRewind = rw.equalsIgnoreCase("T");
		CONSUMER_TOPIC = (String)environment.getKafkaTopicProperties().get("ConsumerTopic");
		PRODUCER_TOPIC = (String)environment.getKafkaTopicProperties().get("ProducerTopic");
		consumer = new StringConsumer(environment, AGENT_GROUP,
					CONSUMER_TOPIC, listener, isRewind, pollSeconds);
		producer = new KafkaProducer(environment, AGENT_GROUP);
		PRODUCER_KEY = AGENT_GROUP;
	}
	
	public void shipEvent(JSONObject event) {
		producer.sendMessage(PRODUCER_TOPIC, event.toJSONString(), PRODUCER_KEY, new Integer(0));
	}
	
	
	public void shutDown() {
		consumer.close();
	}

}
