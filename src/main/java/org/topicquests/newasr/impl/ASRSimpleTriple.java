/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAddressable;
import org.topicquests.newasr.api.ISimpleTriple;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class ASRSimpleTriple implements ISimpleTriple {
	private JsonObject data;
	/**
	 * 
	 */
	public ASRSimpleTriple() {
		data = new JsonObject();
		this.setNormalizedTripleId(-1); // set default
		this.setWgSubjectId(-1);
		this.setTrSubjectId(-1);
		this.setWgObjectId(-1);
		this.setTrObjectId(-1);
	}
	
	public ASRSimpleTriple(JsonObject d) {
		data = d;
	}

	@Override
	public void setId(long id) {
		data.addProperty(IAddressable.ID_KEY, new Long(id));
	}

	@Override
	public long getId() {
		return data.get(IAddressable.ID_KEY).getAsJsonPrimitive().getAsLong();
	}


	@Override
	public void setPredicateId(long id) {
		data.addProperty(ISimpleTriple.PRED_KEY, new Long(id));
	}

	@Override
	public long getPredicateId() {
		return data.get(ISimpleTriple.PRED_KEY).getAsJsonPrimitive().getAsLong();
	}


	ISimpleTriple getTriple(long id) {
		ASREnvironment env = ASREnvironment.getInstance();
		IResult r = env.getTripleModel().getTupleById(id);
		return (ISimpleTriple)r.getResultObject();
	}

	//@Override
	//public String getObjectType() {
	//	return data.get(ISimpleTriple.OBJ_TYP_KEY).getAsString();
	//}

	@Override
	public void addSentenceId(long sentenceId) {
		JsonArray sx = listSentenceIds();
		if (sx == null) {
			sx = new JsonArray();
			data.add(ISimpleTriple.SENTENCE_KEY, sx);
		}
		sx.add(new Long(sentenceId));
	}

	@Override
	public JsonArray listSentenceIds() {
		JsonElement je = data.get(ISimpleTriple.SENTENCE_KEY);
		if (je != null)
			return je.getAsJsonArray();
		return null;
	}
/*
	@Override
	public void setPSI(String psi) {
		data.addProperty(ISimpleTriple.PSI_KEY, psi);
	}

	@Override
	public String getPSI() {
		return data.get(ISimpleTriple.PSI_KEY).getAsString();
	}
*/
	@Override
	public void setNormalizedTripleId(long id) {
		data.addProperty(ISimpleTriple.NORMALIZED_ID_KEY, new Long(id));
	}

	@Override
	public long getNormalizedTripleId() {
		return data.get(ISimpleTriple.NORMALIZED_ID_KEY).getAsJsonPrimitive().getAsLong();
	}

	@Override
	public void setWgSubjectId(long id) {
		data.addProperty(ISimpleTriple.WG_SUBJ_KEY, new Long(id));
	}
	@Override
	public void setTrSubjectId(long id) {
		data.addProperty(ISimpleTriple.TR_SUBJ_KEY, new Long(id));
	}

	@Override
	public long getWgSubjectId() {
		return data.get(ISimpleTriple.WG_SUBJ_KEY).getAsJsonPrimitive().getAsLong();
	}
	@Override
	public long getTrSubjectId() {
		return data.get(ISimpleTriple.TR_SUBJ_KEY).getAsJsonPrimitive().getAsLong();
	}
	@Override
	public void setWgObjectId(long id) {
		data.addProperty(ISimpleTriple.WG_OBJ_KEY, new Long(id));
	}
	@Override
	public void setTrObjectId(long id) {
		data.addProperty(ISimpleTriple.TR_OBJ_KEY, new Long(id));
	}

	@Override
	public long getWgObjectId() {
		return data.get(ISimpleTriple.WG_OBJ_KEY).getAsJsonPrimitive().getAsLong();
	}
	@Override
	public long getTrObjectId() {
		return data.get(ISimpleTriple.TR_OBJ_KEY).getAsJsonPrimitive().getAsLong();
	}

	@Override
	public JsonObject getData() {
		return data;
	}

	@Override
	public void setSubjectText(String text) {
		data.addProperty(ISimpleTriple.SUBJ_TXT_KEY, text);
	}

	@Override
	public String getSubjectText() {
		JsonElement je = data.get(ISimpleTriple.SUBJ_TXT_KEY);
		if (je != null)
			return je.getAsString();
		ISimpleTriple st = this.getTriple(getTrSubjectId());
		return st.toString();	}

	@Override
	public void setPredicateText(String text) {
		data.addProperty(ISimpleTriple.PRED_TXT_KEY, text);
	}

	@Override
	public String getPredicateText() {
		return data.get(ISimpleTriple.PRED_TXT_KEY).getAsString();
	}

	@Override
	public void setObjectText(String text) {
		data.addProperty(ISimpleTriple.OBJ_TXT_KEY, text);
	}

	@Override
	public String getObjectText() {
		JsonElement je = data.get(ISimpleTriple.OBJ_TXT_KEY);
		if (je != null)
			return je.getAsString();
		ISimpleTriple st = this.getTriple(getTrObjectId());
		return st.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{subj: "+getSubjectText()+", pred: "+getPredicateText()+", obj: "+getObjectText()+"}");
		return buf.toString();
	}
}
