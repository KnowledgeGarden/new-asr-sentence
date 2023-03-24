/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;


import org.topicquests.support.api.IResult;

import com.google.gson.JsonObject;


/**
 * @author park
 * Sentences removed from dictionary
 */
public interface IDictionary {
			
	/**
	 * Can return null if this dictionary does not have the word
	 * @param id
	 * @return
	 */
	String getTerm(String id);
		
	/**
	 * Can return <code>null</code> if word doesn't exist
	 * @param term
	 * @return
	 */
	String getTermId(String term);
	
	long getTermIdAsLong(String term);
	
	/**
	 * Quick test; returns <code>true</code> if nothing in the dictionary
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * If word does not exist, it will be added with a new Id;
	 * Otherwise, the word's existing Id will be returned
	 * @param term
	 * @return
	 */
	IResult addTerm(String term);
		
	JsonObject getDictionary();
	
}
