/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import org.topicquests.newasr.SentenceEngine;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public interface IAsrModel {

	void setSentenceEngine(SentenceEngine se);
	
	/**
	 * Process a given {@code sentence}
	 * @param sentenceId
	 * @param sentence
	 * @return
	 */
	IResult processSentence(long sentenceId, String sentence);
	
	/**
	 * <p>Create an {@link IWordGram} from {@code term} -used for bootstrapping and elsewhere</p>
	 * <p>If term already exists, just returns its ID.
	 * @param term
	 * @param pos can be {@code null}
	 * @return returns term's ID as String
	 */
	IResult processTerm(String term, String pos);
	
	
	/**
	 * <p>For importing predicates from csv files<p>
	 * <p>Note:o when a term specifies an inverse predicate or a canonical predicate,
	 *	it is required that those have been defined an imported prior to the new predicate</p>
	 * @param term
	 * @param tense 
	 * @param inverseTerm		can be {@code null}
	 * @param cannonicalTerm	can be {@code null}
	 * String epistemicStatus	can be {@code null}
	 * @param isNegative
	 * @return
	 */
	IResult processPredicate(String term, String tense, String inverseTerm, String cannonicalTerm,
			String epistemicStatus, boolean isNegative);
	
	/**
	 * Fetch a term by its {@code id}
	 * @param id
	 * @return
	 */
	IResult getTermById(String id);
	
	/**
	 * Same as $getTermById but does not check cache
	 * @param id
	 * @return
	 */
	IResult getThisTermById(String id);
	
	/////////////////////////
	// For workflow:
	//	We create a new dictionary entry for a term,
	//  Then we craft an IWordGram with that new id
	/////////////////////////
	IResult newDictionaryEntry(String term);
	
	IResult putWordGram(IWordGram newGram);
	
	////////////////////////////
	// Kafka handling
	////////////////////////////
	
	/**
	 * After spaCy processes a sentence
	 * @param sentence
	 * @return TODO
	 */
	boolean acceptSpacyResponse(JsonObject sentence);
	
	/**
	 * From the paragraph agent
	 * @param sentence
	 * @return TODO
	 */
	boolean acceptNewSentence(JsonObject sentence);
}
