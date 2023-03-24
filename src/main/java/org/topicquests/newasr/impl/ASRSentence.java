/**
 * 
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.api.IConstants;
import org.topicquests.newasr.api.ISentence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class ASRSentence implements ISentence {
	private JsonObject data;
	/**
	 * 
	 */
	public ASRSentence() {
		data = new JsonObject();
	}

	public ASRSentence(JsonObject json) {
		data = json;
	}
	@Override
	public void setId(long id) {
		data.addProperty(IConstants.ID_KEY, new Long(id));
	}

	@Override
	public long getId() {
		return data.get(IConstants.ID_KEY).getAsLong();
	}

	@Override
	public void setText(String text) {
		data.addProperty(ISentence.TEXT_FIELD, text);
	}

	@Override
	public String getText() {
		return data.get(ISentence.TEXT_FIELD).getAsString();
	}

	@Override
	public void setPredicatePhrases(JsonArray preds) {
		data.add(ISentence.PRED_FIELD, preds);
	}

	@Override
	public JsonArray getPredicatePhrases() {
		JsonElement je = data.get(ISentence.PRED_FIELD);
		if (je != null)
			return je.getAsJsonArray();
		return null;
	}

	@Override
	public JsonObject getData() {
		return data;
	}

}
