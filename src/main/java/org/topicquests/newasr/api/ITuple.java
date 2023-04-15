/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public interface ITuple extends IAddressable {
	public static final String
		SUBJ_KEY			= "subj",
		SUBJ_TYPE_KEY		= "subT",
		PRED_KEY			= "pred",
		OBJ_KEY				= "obj",
		OBJ_TYPE_KEY		= "objT",
		PSI_KEY				= "psi",
		SENTENCES_KEY		= "sent";
	public static final String
		WORDGRAM_TYPE		= "wgt",
		TUPLE_TYPE			= "tup";
	/**
	 * 
	 * @param subjectId one of {@link IWordGram} or {@link ITuple}
	 * @param type
	 */
	void setSubject(long subjectId, String type);
	
	/**
	 * 
	 * @param predicateId
	 */
	void setPredicate(long predicateId);
	
	/**
	 * 
	 * @param subj one of {@link IWordGram} or {@link ITuple}
	 * @param type
	 */
	void setObject(long objectId, String type);
	
	void computePSI();
	
	String getPSI();
	
	void addSentenceId(long id);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray listSentenceIds();
	
	JsonObject getData();
}
