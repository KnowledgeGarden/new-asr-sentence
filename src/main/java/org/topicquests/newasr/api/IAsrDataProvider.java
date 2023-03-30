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
	

}
