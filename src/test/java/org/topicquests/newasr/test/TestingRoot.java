/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.SentenceEngine;
import org.topicquests.newasr.api.IAsrModel;

/**
 * @author jackpark
 *
 */
public class TestingRoot {
	protected ASREnvironment environment;
	protected IAsrModel model;
	protected SentenceEngine sentenceEngine;

	/**
	 * 
	 */
	public TestingRoot() {
		environment = new ASREnvironment();
		model = environment.getModel();
		sentenceEngine = environment.getSentenceEngine();
	}

}
