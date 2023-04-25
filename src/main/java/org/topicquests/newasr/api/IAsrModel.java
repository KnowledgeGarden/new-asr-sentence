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
	 * 
	 * @param term
	 * @param pos 			can be {@coe null}
	 * @param sentenceId 	can be {@code -1}
	 * @param inTargetId	can be {@code -1}
	 * @param outTargetId	can be {@code -1}
	 * @param tense			can be {@coe null}
	 * @param epi			can be {@coe null}
	 * @return
	 */
	IResult processTerm(String term, String pos, long sentenceId, long inTargetId, long outTargetId, String tense, String epi);
	
	
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
	 * <p>Fetch a term by its {@code id}</p>
	 * <p>If a canonical form of that term exists, return it instead</p>
	 * @param id
	 * @return
	 */
	IResult getTermById(String id);
	
	/**
	 * Same as $getTermById but does not check for canonical form
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
	
	/////////////////////
	// Live wordgram object support]
	////////////////////
	
	
	IResult addPOS(long gramId, String value);
	
	IResult addWikidata(long gramId, String value);
	
	IResult addDBpedia(long gramId, String value);
	
	
	/**
	 * Add a sentence edge to this wordgram
	 * @param gramId
	 * @param senteneId TODO
	 * @param inLinkTargetId can be {@code -1}
	 * @param outlinkTargetId can be {@code -1}
	 * @param tense can be {@code null}
	 * @param epistemicStatus can be {@code null}
	 * @return
	 */
	IResult addSentenceEdge(long gramId, long sentenceId, long inLinkTargetId, long outlinkTargetId, String tense, String epistemicStatus);
	
	IResult addTopicLocator(long gramId, long topicLocator);

	IResult addSynonymTerm(long gramId, long synonymTermId);
	
	IResult addAntonymTerm(long gramId, long antonymTermId);
	
	IResult addHyponymTerm(long gramId, long hypoTermId);
	
	IResult addHypernymTerm(long gramId, long hyperTermId);


}
