/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ISentenceDataProvider;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class PostgresSentenceDatabase implements ISentenceDataProvider {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;

	/**
	 * 
	 */
	public PostgresSentenceDatabase(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getDatabaseDriver();
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
