/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.ParagraphEngine;
import org.topicquests.newasr.SentenceEngine;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ITupleModel;
import org.topicquests.newasr.para.ParagraphHandler;

/**
 * @author jackpark
 *
 */
public class TestingRoot {
	protected ASREnvironment environment;
	protected IAsrModel model;
	protected SentenceEngine sentenceEngine;
	protected ITupleModel tripleModel;
	protected ParagraphHandler paraHandler;
	protected ParagraphEngine paragraphEngine;

	/**
	 * 
	 */
	public TestingRoot() {
		environment = new ASREnvironment();
		model = environment.getModel();
		sentenceEngine = environment.getSentenceEngine();
		tripleModel = environment.getTripleModel();
		paraHandler = environment.getParagraphHandler();
		paragraphEngine = environment.getParagraphEngine();
	}

}
