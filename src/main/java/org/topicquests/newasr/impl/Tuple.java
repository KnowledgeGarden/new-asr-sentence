/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.api.ITuple;
import org.topicquests.newasr.api.IWordGram;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class Tuple implements ITuple {
	protected JsonObject data;

	/**
	 * 
	 */
	public Tuple() {
		data = new JsonObject();
	}
	
	public Tuple(JsonObject d) {
		data = d;
	}

	@Override
	public void setId(long id) {
		data.addProperty(IWordGram.ID_KEY, new Long(id));
	}

	@Override
	public long getId() {
		return data.get(IWordGram.ID_KEY).getAsLong();
	}

	@Override
	public void setSubject(long subjectId, String type) {
		data.addProperty(ITuple.SUBJ_KEY, new Long(subjectId));
		data.addProperty(ITuple.SUBJ_TYPE_KEY, type);
	}
	

	@Override
	public void setPredicate(long predicateId) {
		data.addProperty(ITuple.PRED_KEY, new Long(predicateId));
	}

	@Override
	public void setObject(long objectId, String type) {
		data.addProperty(ITuple.OBJ_KEY, new Long(objectId));
		data.addProperty(ITuple.OBJ_TYPE_KEY, type);
	}

	@Override
	public void computePSI() {
		long subjId = data.get(ITuple.SUBJ_KEY).getAsJsonPrimitive().getAsLong();
		String subjType = data.get(SUBJ_TYPE_KEY).getAsString();
		long predId = data.get(ITuple.PRED_KEY).getAsJsonPrimitive().getAsLong();
		long objId = data.get(ITuple.OBJ_KEY).getAsJsonPrimitive().getAsLong();
		String objType = data.get(OBJ_TYPE_KEY).getAsString();
		String psi = Long.toString(subjId)+subjType+
					Long.toString(predId)+
					Long.toString(objId)+objType;
		data.addProperty(ITuple.PSI_KEY, psi);

	}

	@Override
	public String getPSI() {
		return data.get(ITuple.PSI_KEY).getAsString();
	}

	@Override
	public void addSentenceId(long id) {
		JsonArray sents =listSentenceIds();
		if (sents == null) {
			sents = new JsonArray();
			data.add(ITuple.SENTENCES_KEY, sents);
		}
		sents.add(new Long(id));
	}

	@Override
	public JsonArray listSentenceIds() {
		JsonElement je = data.get(ITuple.SENTENCES_KEY);
		if (je != null)
			return je.getAsJsonArray();
		return null;
	}
	@Override
	public JsonObject getData() {
		return data;
	}

}
