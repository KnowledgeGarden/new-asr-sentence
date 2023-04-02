/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import com.google.gson.JsonObject;

/**
 * @author jackpark
 * indexed by {@code sentenceId}
 */
public interface ISentenceEdge {

	void setInLink(long targetNodeId);
	/**
	 * Can return {@code -1)
	 * @return
	 */
	long getInLink();
	
	boolean hasInLink();
	
	void setOutLink(long targetNodeId);
	/**
	 * Can return {@code -1)
	 * @return
	 */
	long getOutLink();
	
	boolean hasOutLink();

	void setPredicateTense(String tense);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	String getPredicateTense();
	
	void setEpistemicStatus(String epi);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	String getpistemicStatus();
	
	JsonObject getData();
}
