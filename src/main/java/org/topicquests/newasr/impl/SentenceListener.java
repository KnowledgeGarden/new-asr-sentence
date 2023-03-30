/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IKafkaDispatcher;
import org.topicquests.newasr.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class SentenceListener implements IKafkaDispatcher, IMessageConsumerListener {
	private ASREnvironment environment;
	private IAsrModel model;
	private JsonUtil util;

	/**
	 * 
	 */
	public SentenceListener(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		util = new JsonUtil();
	}

	@Override
	public boolean acceptRecord(ConsumerRecord record) {
		String json = (String)record.value();
		environment.logDebug("SentenceyListener.acceptRecord "+json);
		boolean result = false;
		if (json == null)
			return result;
		try {
			JsonObject data = util.parse(json);
			result = model.acceptNewSentence(data);
		} catch (Exception e) {
			environment.logError("SentenceyListener: "+e.getMessage(), e);
			e.printStackTrace();
		}

		return result;	
	}

}
