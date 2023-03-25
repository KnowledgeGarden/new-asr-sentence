/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.kafka;


import org.topicquests.backside.kafka.producer.MessageProducer;
import org.topicquests.support.api.IEnvironment;

/**
 * @author park
 *
 */
public class SentenceProducer extends MessageProducer {

  /**
   * @param env
   */
  public SentenceProducer(IEnvironment env, String clientId) {
    super(env, clientId, true);
  }

  /**
   * This will be a JSON string of the form {verb:<verb>,cargo:<cargo>)
   * @param msg
   * @param partition
   */
  public void sendMessage(String topic, String message, String key, Integer partition) {
    environment.logDebug("SentenceProducer "+message);
    super.sendMessage(topic, message, key, partition);
  }

  /* (non-Javadoc)
   * @see org.topicquests.backside.kafka.apps.AbstractKafkaApp#close()
   */
  @Override
  public void close() {
    super.close();
  }

}
