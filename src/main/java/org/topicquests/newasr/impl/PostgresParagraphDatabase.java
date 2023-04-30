/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.api.IParagraphDataProvider;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class PostgresParagraphDatabase implements IParagraphDataProvider {
	private ASREnvironment environment;
	private PostgresConnectionFactory dbDriver = null;
	/**
	 * 
	 */
	public PostgresParagraphDatabase(ASREnvironment e) {
		environment = e;
		dbDriver = environment.getDatabaseDriver();
	}

	@Override
	public IResult putParagraph(IParagraph p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResult getParagraph(long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
