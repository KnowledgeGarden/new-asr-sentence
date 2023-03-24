/**
 * 
 */
package org.topicquests.newasr.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IKafkaDispatcher;

/**
 * @author jackpark
 *
 */
public class KafkaListener implements IKafkaDispatcher, IMessageConsumerListener {
	private ASREnvironment environment;

	/**
	 * 
	 */
	public KafkaListener(ASREnvironment env) {
		environment =env;
	}

	@Override
	public boolean acceptRecord(ConsumerRecord record) {
		// TODO Auto-generated method stub
		return false;
	}

}
