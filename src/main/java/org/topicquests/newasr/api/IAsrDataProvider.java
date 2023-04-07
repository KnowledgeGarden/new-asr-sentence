/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import org.topicquests.support.api.IResult;

import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public interface IAsrDataProvider {

	/**
	 * Insert an {@link IWordGram} into the database
	 * @param node
	 * @return
	 */
	IResult putNode(IWordGram node);

	/**
	 * Return an {@link IWordGram} or {@code null}
	 * @param nodeId
	 * @return
	 */
	IResult getNode(long nodeId);
	
	IResult addNodeProperty(long id, String key, String value);
	
	/**
	 * <p>Insert a collection of key/value pairs against a single nodeID</p>
	 * <p>Used for bootstrapping a new term and its POV</p>
	 * @param id
	 * @param keysVals
	 * @return
	 */
	IResult addNodeProperties(long id, JsonObject keysVals);
	
	IResult removeNodeProperty(long id, String key, String value);
	
	////////////////////////
	// live wordgram support
	////////////////////////
IResult addPOS(long gramId, String value);
	
	IResult addWikidata(long gramId, String value);
	
	IResult addDBpedia(long gramId, String value);
	
	
	/**
	 * Add a sentence edge to this wordgram
	 * @param gramId
	 * @param sentenceId TODO
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
