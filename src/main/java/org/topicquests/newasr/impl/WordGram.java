/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * @author jackpark
 *
 */
public class WordGram implements IWordGram {
	private JsonObject data;
	private ASREnvironment environment;
	private IAsrModel model;
	private boolean isLive = false;

	/**
	 * @param env TODO
	 * 
	 */
	public WordGram(ASREnvironment env) {
		environment = env;
		model = environment.getModel();
		data = new JsonObject();
		/**
		 * When {@code true} updates go to the database
		 */
		isLive = false;
	}
	
	public WordGram(JsonObject d, ASREnvironment env) {
		environment = env;
		model = environment.getModel();
		data = d;
		isLive = true;
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
	public JsonObject getData() {
		return data;
	}

	@Override
	public String getWords(String languageCode) {
		return data.get(IWordGram.WORDS_KEY).getAsString();
	}

	@Override
	public void setWords(String words, String languageCode) {
		data.addProperty(IWordGram.WORDS_KEY, words);
	}

	@Override
	public void setSentenceId(long sentenceId, String tense, String epi) {
		JsonElement jo = data.get(IWordGram.SENT_EDGES_KEY);
		JsonObject sentenceEdges;
		if (jo == null) {
			sentenceEdges = new JsonObject();
			data.add(IWordGram.SENT_EDGES_KEY, sentenceEdges);
		} else
			sentenceEdges = jo.getAsJsonObject();
		jo = sentenceEdges.get(Long.toString(sentenceId));
		JsonObject edge;
		if (jo == null) {
			edge = new JsonObject();
			sentenceEdges.add(Long.toString(sentenceId), edge);
		} else
			edge = jo.getAsJsonObject();
		if (tense != null)
			edge.addProperty(IWordGram.TENSE_KEY, tense);
		if (epi != null)
			edge.addProperty(IWordGram.EPI_KEY, epi);
		
	}
	@Override
	public IResult addSentenceEdge(long sentenceId, long inLinkTargetId, long outlinkTargetId, String tense, String epistemicStatus) {
		JsonElement jo = data.get(IWordGram.SENT_EDGES_KEY);
		JsonObject sentenceEdges;
		if (jo == null) {
			sentenceEdges = new JsonObject();
			data.add(IWordGram.SENT_EDGES_KEY, sentenceEdges);
		} else
			sentenceEdges = jo.getAsJsonObject();
		jo = sentenceEdges.get(Long.toString(sentenceId));
		JsonObject edge;
		if (jo == null){
			edge = new JsonObject();
			sentenceEdges.add(Long.toString(sentenceId), edge);
		} else
			edge = jo.getAsJsonObject();
		if (tense != null)
			edge.addProperty(IWordGram.TENSE_KEY, tense);
		if (epistemicStatus != null)
			edge.addProperty(IWordGram.EPI_KEY, epistemicStatus);
		if (inLinkTargetId > -1)
			edge.addProperty(IWordGram.IN_KEY, inLinkTargetId);
		if (outlinkTargetId > -1)
			edge.addProperty(IWordGram.OUT_KEY, outlinkTargetId);
		if (isLive)
			return model.addSentenceEdge(getId(), sentenceId, inLinkTargetId, outlinkTargetId, tense, epistemicStatus);
		else
			return null;
	}
	@Override
	public void setSentenceEdges(JsonObject edges) {
		data.add(IWordGram.SENT_EDGES_KEY, edges);
	}

	public JsonObject getSentenceEdges() {
		JsonElement je = data.get(IWordGram.SENT_EDGES_KEY);
		if (je != null)
			return je.getAsJsonObject();
		return null;
	}
	
	/**
	 * Can return {@code null}
	 * @param sentenceId
	 * @return
	 */
	@Override
	public JsonObject getSentenceEdge(long sentenceId) {
		JsonObject result = null;
		JsonElement jo = data.get(IWordGram.SENT_EDGES_KEY);
		if (jo == null) return null;
		JsonObject sentenceEdges = jo.getAsJsonObject();
		jo = sentenceEdges.get(Long.toString(sentenceId));
		if (jo == null) return null;
		result = jo.getAsJsonObject();
	
		return result;	
	}

	@Override
	public void addInLink(long sentenceId, long gramId) {
		JsonObject sentenceEdge = getSentenceEdge(sentenceId);
		if (sentenceEdge == null) {
			System.out.println("WARNING: SentenceId not set");
			this.setSentenceId(sentenceId, null, null);
			sentenceEdge = getSentenceEdge(sentenceId);
		}
		sentenceEdge.addProperty(WordGram.IN_KEY, Long.toString(gramId));
		//JsonArray ja = listInLinks();
		//if (ja == null) {
		//	ja = new JsonArray();
		//}
		//JsonObject jo = new JsonObject();
		//jo.addProperty(Long.toString(sentenceId), gramId);
		//ja.add(jo);
		String thelink = Long.toString(sentenceId) + ","  +Long.toString(gramId);
		data.addProperty(IWordGram.IN_KEY, thelink);
	}

	@Override
	public void addOutlink(long sentenceId, long gramId) {
		JsonObject sentenceEdge = getSentenceEdge(sentenceId);
		if (sentenceEdge == null) {
			System.out.println("WARNING: SentenceId not set");
			this.setSentenceId(sentenceId, null, null);
			sentenceEdge = getSentenceEdge(sentenceId);
		}
		sentenceEdge.addProperty(WordGram.OUT_KEY, Long.toString(gramId));

		//JsonArray ja = listOutLinks();
		//if (ja == null) {
		//	ja = new JsonArray();
		//}
		//JsonObject jo = new JsonObject();
		//jo.addProperty(Long.toString(sentenceId), gramId);
		//ja.add(jo);
		String thelink = Long.toString(sentenceId) + ","  +Long.toString(gramId);
		data.addProperty(IWordGram.OUT_KEY, thelink);
	}

	@Override
	public JsonArray listTopicLocators() {
		JsonElement jo = data.get(IWordGram.LOX_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}

	@Override
	public void addTopicLocator(long locator) {
		JsonArray ja = listTopicLocators();
		if (ja == null) {
			ja = new JsonArray();
		}
		ja.add(locator);
		data.add(IWordGram.LOX_KEY, ja);
		if (isLive)
			model.addTopicLocator(getId(), locator);
	}
	@Override
	public void setTopicLocators(JsonArray locators) {
		data.add(IWordGram.LOX_KEY, locators);
	}


	@Override
	public String getDBpedia() {
		JsonElement jo = data.get(IWordGram.DBPED_KEY);
		if (jo == null) return null;
		return jo.getAsString();
	}

	@Override
	public void setDBpedia(String dbPediaJson) {
		data.addProperty(IWordGram.DBPED_KEY, dbPediaJson);

	}

	@Override
	public String getWikidata() {
		JsonElement jo = data.get(IWordGram.WIKID_KEY);
		if (jo == null) return null;
		return jo.getAsString();
	}

	@Override
	public void setWikidata(String wikidataId) {
		data.addProperty(IWordGram.WIKID_KEY, wikidataId);
	}

	@Override
	public JsonArray listPOS() {
		JsonElement jo = data.get(IWordGram.POS_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}

	@Override
	public void addPOS(String pos) {
		JsonArray ja = listPOS();
		if (ja == null) {
			ja = new JsonArray();
		}
		ja.add(pos);
		data.add(IWordGram.POS_KEY, ja);
		if (isLive)
			model.addPOS(getId(), pos);
	}
	
	@Override
	public void setPOS(JsonArray pos) {
		data.add(IWordGram.POS_KEY, pos);
	}


	@Override
	public void setInverseTerm(long inverseTermId) {
		data.addProperty(IWordGram.INVERSE_KEY, inverseTermId);
	}

	@Override
	public long getInverseTerm() {
		JsonElement jo = data.get(IWordGram.INVERSE_KEY);
		if (jo == null) return -1;
		return jo.getAsLong();
	}

	@Override
	public void setCannonTerm(long cannonTermId) {
		data.addProperty(IWordGram.CANNON_KEY, cannonTermId);
	}

	@Override
	public long getCannonTerm() {
		JsonElement jo = data.get(IWordGram.CANNON_KEY);
		if (jo == null) return -1;
		return jo.getAsLong();
	}

	@Override
	public void addSynonymTerm(long synonymTermId) {
		data.addProperty(IWordGram.SYNONYM_KEY, synonymTermId);
		if (isLive)
			model.addSynonymTerm(getId(), synonymTermId);
	}

	@Override
	public JsonArray listSynonyms() {
		JsonElement jo = data.get(IWordGram.SYNONYM_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}

	@Override
	public void addAntonymTerm(long antonymTermId) {
		data.addProperty(IWordGram.ANTONYM_KEY, antonymTermId);
		if (isLive)
			model.addAntonymTerm(getId(), antonymTermId);
	}

	@Override
	public JsonArray listAntonyms() {
		JsonElement jo = data.get(IWordGram.ANTONYM_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}
	
	@Override
	public void addHyponymTerm(long hypoTermId) {
		data.addProperty(IWordGram.HYPONYM_KEY, hypoTermId);
		if (isLive)
			model.addHyponymTerm(getId(), hypoTermId);
	}

	@Override
	public JsonArray listHyponyms() {
		JsonElement jo = data.get(IWordGram.HYPONYM_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}

	@Override
	public void addHypernymTerm(long hyperTermId) {
		data.addProperty(IWordGram.HYPERNYM_KEY, hyperTermId);
		if (isLive)
			model.addHypernymTerm(getId(), hyperTermId);
	}

	@Override
	public JsonArray listHypernyms() {
		JsonElement jo = data.get(IWordGram.HYPERNYM_KEY);
		if (jo == null) return null;
		return jo.getAsJsonArray();
	}

	@Override
	public boolean hasInverseTerm() {
		return (this.getInverseTerm() > -1);
	}

	@Override
	public boolean hasCannonicalTerm() {
		return (this.getCannonTerm() > -1);
	}

	@Override
	public void addExtensionProperty(String key, String value) {
		JsonElement je = data.get(IWordGram.EXTENSION_KEY);
		JsonObject extendedProperties = null;
		if (je == null) {
			extendedProperties = new JsonObject();
			data.add(IWordGram.EXTENSION_KEY, extendedProperties);
		}
		extendedProperties.addProperty(key, value);
	}
/*	@Override
	public void addExtensionProperty(String key, JsonObject value) {
		JsonElement je = data.get(IWordGram.EXTENSION_KEY);
		JsonObject extendedProperties = null;
		if (je == null) {
			extendedProperties = new JsonObject();
			data.add(IWordGram.EXTENSION_KEY, extendedProperties);
		}
		extendedProperties.add(key, value);
	}

	@Override
	public void addExtensionProperty(String key, JsonArray value) {
		JsonElement je = data.get(IWordGram.EXTENSION_KEY);
		JsonObject extendedProperties = null;
		if (je == null) {
			extendedProperties = new JsonObject();
			data.add(IWordGram.EXTENSION_KEY, extendedProperties);
		}
		extendedProperties.add(key, value);
	}
*/

	@Override
	public JsonElement getExtensionProperty(String key) {
		JsonElement je = data.get(IWordGram.EXTENSION_KEY);
		JsonObject extendedProperties = null;
		if (je == null) 
			return null;
		else 
			extendedProperties = je.getAsJsonObject();
		return extendedProperties.get(key);
	}

	@Override
	public void setTense(String tense) {
		data.addProperty(IWordGram.TENSE_KEY, tense);
	}

	@Override
	public String getTense() {
		JsonElement jo = data.get(IWordGram.TENSE_KEY);
		if (jo == null) return null;
		return jo.getAsString();
	}

	@Override
	public void setEpistemicStatus(String status) {
		data.addProperty(IWordGram.EPI_KEY, status);
	}

	@Override
	public String getEpistemicStatus() {
		JsonElement jo = data.get(IWordGram.EPI_KEY);
		if (jo == null) return null;
		return jo.getAsString();
	}

	@Override
	public JsonObject getExtensionPropeties() {
		JsonElement extendedProperties = data.get(IWordGram.EXTENSION_KEY);
		if (extendedProperties == null)
			return null;
		return extendedProperties.getAsJsonObject();
	}

	@Override
	public void setNegation(boolean isNeg) {
		data.addProperty(IWordGram.NEGATION_KEY, isNeg);
	}

	@Override
	public boolean getNegation() {
		JsonElement je = data.get(IWordGram.NEGATION_KEY);
		if (je == null)
			return false;
		return je.getAsBoolean();
	}

	




}
