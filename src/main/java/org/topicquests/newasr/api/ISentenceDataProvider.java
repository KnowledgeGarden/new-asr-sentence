/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 */
package org.topicquests.newasr.api;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public interface ISentenceDataProvider {

	IResult putSentence(ISentence s);
	
	IResult getSentence(long id);

	IResult updateSentence(ISentence s);
}
