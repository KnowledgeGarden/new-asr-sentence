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
public interface ISimpleTriple extends IAddressable {
//	public static final String
//		WORDGRAM_TYPE		= "wgt",
//		TRIPLE_TYPE			= "tplt";
	public static final String
		WG_SUBJ_KEY			= "w_subj",
		TR_SUBJ_KEY			= "t_subj",
		SUBJ_TXT_KEY		= "subjTX",
		PRED_KEY			= "pred",
		PRED_TXT_KEY		= "predTX",
		WG_OBJ_KEY			= "w_obj",
		TR_OBJ_KEY			= "t_obj",
		OBJ_TXT_KEY			= "objTX",
//		PSI_KEY				= "psi",
		NORMALIZED_ID_KEY	= "norm",
		SENTENCE_KEY		= "sent";
	
	/**
	 * 
	 * @param id
	 * @param type can not be {@code null}
	 */
	void setWgSubjectId(long id);
	void setTrSubjectId(long id);
	String getSubjectText();
	void setSubjectText(String text);
	/**
	 * Must use object type to cast result
	 * @return
	 */
	long getWgSubjectId();
	//String getSubjectType()
;	long getTrSubjectId();

	void setPredicateId(long id);
	long getPredicateId();
	void setPredicateText(String text);
	String getPredicateText();
	
;	void setWgObjectId(long id);
	void setTrObjectId(long id);
	String getObjectText();
	void setObjectText(String text);
	//Object getObject();
	long getWgObjectId();
	//String getObjectType();
	long getTrObjectId();
	
	void addSentenceId(long sentenceId);
	JsonArray listSentenceIds();
	
	//void setPSI(String psi);
	//String getPSI();
	
	void setNormalizedTripleId(long id);
	/**
	 * Can return {@code -1}
	 * @return
	 */
	long getNormalizedTripleId();
	
	//void computePSI();
	
	JsonObject getData();
	
	String toString();

}
