/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.noun;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.spacy.SpacyHttpClient;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.os.asr.driver.sp.SpacyDriverEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import java.util.*;

/**
 * @author jackpark
 *
 */
public class NounAssembler {
	private ASREnvironment environment;
	private IAsrModel model;
	private JsonUtil util;

	/**
	 * 
	 */
	public NounAssembler(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		util =new JsonUtil();

	}

	public IResult bigDamnAnalyze(ISentence sentence, JsonObject spacyObj, JsonObject spcy) {
		IResult result = new ResultPojo();;
		//In theory, sentence arrives as a string and sentenceId with spacy POS parsing, etc
		//First, send it to the spacy predicate server
		//String text = sentence.getText();
		//System.out.println("ProcessSentence\n"+sentence.getData());
		// gather predicates, wikidata and dbpedia stuff in the sentence object
		//sentenceProducer.sendMessage(SPACY_TOPIC, sentence.getData().toString(), SPACY_KEY, partition);
		//@see acceptSpacyResponse below	
		IResult r = null; //spacy.processSentence(text);
		JsonObject jo;
		JsonArray ja, jax;
		environment.logDebug("NounAssembler-\n"+spacyObj);

		//environment.logError("BIGJA "+spacyData, null);
		try {
			
			
			JsonArray concepts = findConcepts(spacyObj);
			// spacy predicates, dbp, nouns, etc
			
			ja = spcy.get("dbp").getAsJsonArray();
			// process dbpedia
			processDBpedia(sentence, ja);
			ja = spcy.get("wkd").getAsJsonArray();
			// process wikidata
			processWikidata(sentence, ja);
			// process nouns
			if (spcy.get("nns") != null) {
				ja = spcy.get("nns").getAsJsonArray();
				processNoun(sentence, ja);
			}
			// process propernouns
			if (spcy.get("pnns") != null) {
				ja = spcy.get("pnns").getAsJsonArray();
				processProperNoun(sentence, ja);
			}
			resolveNouns(sentence, concepts);
			environment.logDebug("NounAssembler\n"+sentence.getData());
			// and now, send the results on to the ne
			//TODO
		} catch (Exception e) {
			environment.logError("SE-1: "+e.getMessage(), e);
			e.printStackTrace();
		}		
		return result;
	}
		
	/**
	 * 
	 * @param data
	 * @return
	 */
	JsonArray findConcepts(JsonObject data) {
		environment.logDebug("NounAssemblerFC\n"+data);
		JsonArray result = new JsonArray();
		if (data == null)
			return result;
		JsonArray sentences = data.get("sentences").getAsJsonArray();
		JsonObject theSentence = sentences.get(0).getAsJsonObject();
		JsonArray nodes = theSentence.get("nodes").getAsJsonArray();;
		environment.logDebug("NounAssembler-1\n"+nodes);
		JsonObject jo, conc;
		int len = nodes.size();
		int conlen = 0;
		JsonObject theCon;
		JsonArray them;
		String theConceptText;
		String jsonCon;
		JsonObject cx;
		JsonElement je;
		for (int i=0;i<len;i++) {
			jo = nodes.get(i).getAsJsonObject();
			je = jo.get("concepts");
			if (je != null) {
				conc = je.getAsJsonObject();
				theConceptText = conc.get("text").getAsString();
				environment.logDebug("NounAssembler-3 "+theConceptText);
				cx = new JsonObject();
				cx.addProperty("strt", Integer.toString(i));
				cx.addProperty("txt", theConceptText);
				result.add(cx);

			}
		}

		environment.logDebug("NounAssembler+ "+result);
		//[{"strt":"1","txt":"Elephant shit"},{"strt":"3","txt":"flies"}]
		return result;
	}
	

	void resolveNouns(ISentence sentence, JsonArray concepts) {
		environment.logDebug("resolveNouns\n"+concepts);
		JsonArray result = new JsonArray();
		JsonArray nouns = sentence.getNouns();
		environment.logDebug("resolveNouns-1\n"+nouns);
		JsonArray pNouns = sentence.getProperNouns();
		environment.logDebug("resolveNouns-2\n"+pNouns);
		JsonArray dbp = sentence.getDBpediaData();
		environment.logDebug("resolveNouns-3\n"+dbp);
		//[[],[{"strt":2,"enx":3,"txt":"encourages"}]]

		int len, len2;
		JsonObject jo, jx;
		JsonArray ja;
		if ((nouns != null) && (pNouns != null)) {
			len = pNouns.size();
			for (int i=0;i<len;i++) {
				jo = pNouns.get(i).getAsJsonObject();
				if (!nouns.contains(jo))
					nouns.add(jo);
			}
		}
		if (concepts != null) {
			len = concepts.size();
			for (int i=0;i<len;i++) {
				jo = concepts.get(i).getAsJsonObject();
				if (!nouns.contains(jo))
					nouns.add(jo);
			}
		}
		// isolate DBpedia objects first
		String txt;
		JsonObject match;
		JsonArray toRemove = new JsonArray();
		if (dbp != null) {
			len = dbp.size();
			for (int i=0;i<len;i++) {
				Object ox = dbp.get(i);
				environment.logDebug("RESOLVING-X "+ox);
				jo = dbp.get(i).getAsJsonObject();
				txt = jo.get("strt").getAsString();
				IResult rx = match(txt, nouns);
				environment.logDebug("RESOLVING-1 "+rx+"\n"+jo);
				if (rx != null) {
					match = (JsonObject)rx.getResultObject();
					JsonArray droppers = (JsonArray)rx.getResultObjectA();
					if (match != null)
						result.add(match);
					if (droppers != null) {
						int lx = droppers.size();
						for (int j = 0;j<lx;j++)
							toRemove.add(droppers.get(j));
					}
				}
				
			}
		}
		environment.logDebug("RESOLVING-1a\n"+toRemove);
		len = toRemove.size();
		for (int i=0;i<len;i++)
			nouns.remove(toRemove.get(i));
		// Add what's left
		environment.logDebug("RESOLVING-2\n"+result);
		len = nouns.size();
		Set<JsonElement> thingies = new HashSet<JsonElement>();
		for (int i=0;i<len;i++) {
			jo = nouns.get(i).getAsJsonObject();
			thingies.add(jo);
		}
		if (!thingies.isEmpty()) {
			Iterator<JsonElement> itr = thingies.iterator();
			while (itr.hasNext())
				result.add(itr.next());
		}
		environment.logDebug("RESOLVING+\n"+result);
		//[{"strt":1,"txt":"shit"},{"strt":3,"txt":"flies"},{"strt":"1","txt":"Elephant shit"},{"strt":"3","txt":"flies"}]
		// after adding set
		//[{"strt":"3","txt":"flies"},{"strt":1,"txt":"shit"},{"strt":3,"txt":"flies"},{"strt":"1","txt":"Elephant shit"}]
		// no clue why set didn't capture "flies"
		// TODO need to cleanup that list
		result = burp(result);
		environment.logDebug("RESOLVING++\n"+result);
		sentence.setResolvedNouns(result);
	}
	
	JsonArray burp(JsonArray hits) {
		JsonArray result = new JsonArray();
		if (hits == null || hits.isEmpty())
			return result;
		JsonArray larger = new JsonArray();
		JsonObject hit, hit2;
		String txt, txt2;
		String [] smiggles;
		int where1, where2;
		int len = hits.size();
		for (int i=0;i<len;i++) {
			hit = hits.get(i).getAsJsonObject();
			txt = hit.get("txt").getAsString();
			smiggles = txt.split(" ");
			if (smiggles.length > 1)
				larger.add(hit);
		}
		environment.logDebug("SMIGGLE+\n"+larger);
		//[{"strt":"1","txt":"Elephant shit"}]
		int len2 = larger.size();
		boolean same = false;
		for (int i=0;i<len;i++) {
			same = false;
			len2 = larger.size();
			hit = hits.get(i).getAsJsonObject();
			txt = hit.get("txt").getAsString();
			where1 = hit.get("strt").getAsJsonPrimitive().getAsInt();
			//environment.logDebug("SMIGGLE-0 "+len2+" "+txt+"\n"+larger);
			for (int j=0;j<len2;j++) {
				hit2 = larger.get(j).getAsJsonObject();
				txt2 = hit2.get("txt").getAsString();
				where2 = hit2.get("strt").getAsJsonPrimitive().getAsInt();
				//environment.logDebug("SMIGGLE-1 "+where1+" "+where2+" | "+txt+" | "+txt2+"\n"+larger);
				if (where1 == where2) {
					if (txt.equals(txt2) ||txt2.contains(txt)) {
						//environment.logDebug("SMIGGLE-2 "+where1+" "+where2+" | "+txt+" | "+txt2+"\n"+larger);
						same = true;
						break;
					}
					//environment.logDebug("SMIGGLE-4 "+where1+" "+where2+" | "+txt+" | "+txt2);
				}
			}
			if (!same)
				larger.add(hit);
		}
		environment.logDebug("SMIGGLE2+\n"+larger);
		//[{"strt":"1","txt":"Elephant shit"},{"strt":"3","txt":"flies"}]

		return larger;
	}
	
	
	IResult match(String txt, JsonArray nouns) {
		IResult output = new ResultPojo();
		JsonObject result = new JsonObject();
		JsonArray droppers = new JsonArray();
		output.setResultObject(result);
		output.setResultObjectA(droppers);
		String comp = txt.toLowerCase().trim();
		int len = nouns.size();
		JsonObject temp;
		String label;
		String [] textC = txt.split(" ");
		int numWords = textC.length;
		JsonObject jo;
		int strt = 0;
		for (int i=0;i<len;i++) {
			temp = nouns.get(i).getAsJsonObject();
			environment.logDebug("MATCHING "+txt+"\n"+temp);
			label = temp.get("txt").getAsString().toLowerCase();
			strt = temp.get("strt").getAsJsonPrimitive().getAsInt();
			// exact match
			if (label.equals(comp)) {
				result.addProperty("strt", Integer.toString(strt));
				result.addProperty("txt", txt);
				return output;
			} else { // speculative check - are the next words compatible?
				if (comp.contains(label)) {
					boolean found = true;
					droppers.add(temp);
					for (int j = 1; j<numWords;j++) {
						jo = nouns.get(++i).getAsJsonObject();
						environment.logDebug("MATCHING-2 "+txt+"\n"+jo);
						label = jo.get("txt").getAsString().toLowerCase();
						environment.logDebug("MATCHING-3 "+txt+" "+label);
						droppers.add(jo);
						if (!comp.contains(label)) {
							environment.logDebug("MATCHING-4 ");
							found = false;
							break;
						}

					}
					if (found) {
						result.addProperty("strt", Integer.toString(strt));
						result.addProperty("txt", txt);
						return output;
					} else
						return null;
				}
			}
		}
		environment.logDebug("MATCHING+ ");

		return null;
	}
	
	void processDBpedia(ISentence sentence, JsonArray dbp) {
		environment.logDebug("DBPEDIA "+dbp);
		if (dbp != null)
			sentence.setDBpediaData(dbp);
	}
	void processWikidata(ISentence sentence, JsonArray wd) {
		if (wd != null)
			sentence.setWikiData(wd);
	}
	void processNoun(ISentence sentence, JsonArray noun) {
		if (noun != null)
			sentence.setNoun(noun);
	}
	void processProperNoun(ISentence sentence, JsonArray noun) {
		if (noun != null)
			sentence.setProperNoun(noun);
	}

}
