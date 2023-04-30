/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.api.IParagraphDataProvider;
import org.topicquests.newasr.api.IParagraphModel;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class ASRParagraphModel implements IParagraphModel {
	private ASREnvironment environment;
	private IParagraphDataProvider database;

	/**
	 * 
	 */
	public ASRParagraphModel(ASREnvironment env) {
		environment =env;
		database = new PostgresParagraphDatabase(environment);

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

	@Override
	public IResult updateParagraph(IParagraph p) {
		// TODO Auto-generated method stub
		return null;
	}

}
