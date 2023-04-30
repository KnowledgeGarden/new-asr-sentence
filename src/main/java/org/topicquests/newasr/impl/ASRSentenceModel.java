/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ISentenceDataProvider;
import org.topicquests.newasr.api.ISentenceModel;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class ASRSentenceModel implements ISentenceModel {
	private ASREnvironment environment;
	private ISentenceDataProvider database;
	/**
	 * 
	 */
	public ASRSentenceModel(ASREnvironment env) {
		environment =env;
		database = new PostgresSentenceDatabase(environment);
	}

	@Override
	public IResult putSentence(ISentence s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult getSentence(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult updateSentence(ISentence s) {
		// TODO Auto-generated method stub
		return null;
	}

}
