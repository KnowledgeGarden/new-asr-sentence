/**
 * 
 */
package org.topicquests.newasr.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IKafkaDispatcher;

/**
 * @author jackpark
 *
 */
public class SentenceListener implements IKafkaDispatcher, IMessageConsumerListener {
	private ASREnvironment environment;
	private IAsrModel model;

	/**
	 * 
	 */
	public SentenceListener(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
	}

	@Override
	public boolean acceptRecord(ConsumerRecord record) {
		// TODO Auto-generated method stub
		return false;
	}

}
