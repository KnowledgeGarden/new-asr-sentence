/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import com.google.gson.JsonArray;

/**
 * @author jackpark
 *
 */
public interface ISimpleTriple extends IAddressable {
	public static final String
		WORDGRAM_TYPE		= "wgt",
		TRIPLE_TYPE			= "tplt";
	
	void setSubjectId(long id);
	long getSubjectId();
	void setSubjectType(String type);
	String getSubjectType9()
;	
	long setPredicateId(long id);
	long getPredicateId();
	
;	void setObjectId(long id);
	long getObjectId();
	void setObjectType(String type);
	String getObjectType();
	
	void addSentenceId(long sentenceId);
	JsonArray listSentenceIds();

}