/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.api.ISentenceEdge;
import org.topicquests.newasr.api.IWordGram;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class SentenceEdgePojo implements ISentenceEdge {
	private JsonObject data;
	/**
	 * 
	 */
	public SentenceEdgePojo() {
		data = new JsonObject();
		//set defaults
		setInLink(-1);
		setOutLink(-1);
	}

	public SentenceEdgePojo(JsonObject d) {
		data =d;
	}
	
	@Override
	public void setInLink(long targetNodeId) {
		data.addProperty(IWordGram.IN_KEY, new Long(targetNodeId));
	}

	@Override
	public long getInLink() {
		JsonElement je = data.get(IWordGram.IN_KEY);
		if (je == null)
			return -1;
		return je.getAsLong();
	}

	@Override
	public boolean hasInLink() {
		JsonElement je = data.get(IWordGram.IN_KEY);
		if (je != null) return true;
		return false;
	}

	@Override
	public void setOutLink(long targetNodeId) {
		data.addProperty(IWordGram.OUT_KEY, new Long(targetNodeId));
	}

	@Override
	public long getOutLink() {
		JsonElement je = data.get(IWordGram.OUT_KEY);
		if (je == null)
			return -1;
		return je.getAsLong();
	}

	@Override
	public boolean hasOutLink() {
		JsonElement je = data.get(IWordGram.OUT_KEY);
		if (je != null) return true;
		return false;
	}

	@Override
	public void setPredicateTense(String tense) {
		if (tense != null)
			data.addProperty(IWordGram.TENSE_KEY, tense);
	}

	@Override
	public String getPredicateTense() {
		JsonElement je = data.get(IWordGram.TENSE_KEY);
		if (je == null)
			return null;
		return je.getAsString();
	}

	@Override
	public void setEpistemicStatus(String epi) {
		if (epi != null)
			data.addProperty(IWordGram.EPI_KEY, epi);
	}

	@Override
	public String getpistemicStatus() {
		JsonElement je = data.get(IWordGram.EPI_KEY);
		if (je == null)
			return null;
		return je.getAsString();
	}

	@Override
	public JsonObject getData() {
		return data;
	}

}
