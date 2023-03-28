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
		//defaults
		setParagraphId(-1);
		setDocumentId(-1);
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
	public void setParagraphId(long id) {
		data.addProperty(ISentence.PARAGRAPH_ID, new Long(id));
	}

	@Override
	public long getParagraphId() {
		return data.get(ISentence.PARAGRAPH_ID).getAsLong();
	}

	@Override
	public void setDocumentId(long id) {
		data.addProperty(ISentence.DOCUMENT_ID, new Long(id));
	}

	@Override
	public long getDocumentId() {
		return data.get(ISentence.DOCUMENT_ID).getAsLong();
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

	@Override
	public void setSpacyData(String spacyJson) {
		data.addProperty(ISentence.SPACY_FiELD, spacyJson);
	}

	@Override
	public String getSpacyData() {
		JsonElement je = data.get(ISentence.SPACY_FiELD);
		if (je != null)
			return je.getAsString();
		return null;
	}

	@Override
	public void addWikidataId(String wikidata) {
		JsonArray dbp = getWikiData();
		if (dbp == null)  {
			dbp = new JsonArray();
			data.add(ISentence.WD_FIELD, dbp);
		}
		dbp.add(wikidata);
	}

	@Override
	public JsonArray getWikiData() {
		JsonElement je = data.get(ISentence.WD_FIELD);
		if (je != null)
			return je.getAsJsonArray();
		return null;
	}

	@Override
	public void addDBpediaData(String dbpJson) {
		JsonArray dbp = getDBpediaData();
		if (dbp == null)  {
			dbp = new JsonArray();
			data.add(ISentence.DBP_FIELD, dbp);
		}
		dbp.add(dbpJson);
	}

	@Override
	public JsonArray getDBpediaData() {
		JsonElement je = data.get(ISentence.DBP_FIELD);
		if (je != null)
			return je.getAsJsonArray();
		return null;
	}

	

}
