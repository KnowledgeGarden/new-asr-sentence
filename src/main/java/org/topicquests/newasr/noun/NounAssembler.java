/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.noun;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.json.JsonSet;
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
	private JsonSet set;

	/**
	 * 
	 */
	public NounAssembler(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		util =new JsonUtil();
		set = new JsonSet();
	}

	/**
	 * 
	 * @param sentence
	 * @param spacyData  [ {concepts, sentences}, {concepts, sentences},...]
	 * @param spcy
	 * @return
	 */
	public IResult bigDamnAnalyze(ISentence sentence, JsonArray spacyData, JsonObject spcy) {
		IResult result = new ResultPojo();;
		//In theory, sentence arrives as a string and sentenceId with spacy POS parsing, etc
		//First, send it to the spacy predicate server
		//String text = sentence.getText();
		//System.out.println("ProcessSentence\n"+sentence.getData());
		// gather predicates, wikidata and dbpedia stuff in the sentence object
		//sentenceProducer.sendMessage(SPACY_TOPIC, sentence.getData().toString(), SPACY_KEY, partition);
		IResult r = null; //spacy.processSentence(text);
		JsonObject jo;
		JsonArray ja, jax;
		environment.logDebug("NounAssembler-\n"+spacyData);
		boolean hasNominals = sentence.hasNominals();
		JsonArray nominals = sentence.getNominalPhrases();

		environment.logDebug("BIGJA "+hasNominals+"\n"+nominals);
		try {
			
			
			JsonArray concepts = findConcepts(spacyData);
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
			resolveNouns(sentence, concepts, nominals);
			environment.logDebug("NounAssembler\n"+sentence.getData());
			// and now, send the results on to the ne
			//TODO
		} catch (Exception e) {
			environment.logError("SE-1: "+e.getMessage(), e);
			e.printStackTrace();
		}		
		return result;
	}
		
	//////////////////////////
	// nouns: [{"strt":"1","txt":"The pandemic"},{"strt":"18","txt":"(NAFLD"},{"strt":"38","txt":"specifically with dietary palm oil"},{"strt":"40","txt":"(PO"},{"strt":"3","txt":"obesity","dbp":{"strt":"obesity","kid":"http://dbpedia.org/resource/Obesity","dbp":"1.0"}},{"strt":"5","txt":"type"},{"strt":"8","txt":"2 diabetes mellitus"},{"strt":"16","txt":"nonalcoholic fatty liver disease","dbp":{"strt":"nonalcoholic fatty liver disease","kid":"http://dbpedia.org/resource/Non-alcoholic_fatty_liver_disease","dbp":"1.0"}},{"strt":"26","txt":"dietary intake"},{"strt":"29","txt":"saturated fats"}]
	// nominals: [{"strt":1,"txt":"pandemic of"},{"strt":26,"txt":"intake of"}]
	/////////////////////////
	/**
	 * Heuristic behavior: remove nouns which are part of a nominal phrase
	 * @param nominals
	 * @param nouns
	 * @return
	 */
	JsonArray processNominals(JsonArray nominals, JsonArray nouns) {
		environment.logDebug("ProcessNominals\n"+nominals+"\n"+nouns);
		JsonArray result = new JsonArray();
		if (nominals == null || nominals.isEmpty())
			return result;
		JsonObject jo, match;
		int len = nominals.size();
		String txt;
		IResult r;
		JsonArray temp, droppers=null;
		for (int i=0;i<len;i++) {
			jo = nominals.get(i).getAsJsonObject();
			txt = jo.get("txt").getAsString();
			r = _match(txt, jo, nouns);
			if (r != null) {
				temp = (JsonArray)r.getResultObjectA();
				//droppers = (JsonArray)r.getResultObjectA()));
				if (droppers == null)
					droppers = temp;
				else
					droppers.addAll(temp);
			}
		}
		environment.logDebug("ProcessNominals "+droppers);
		return droppers;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	JsonArray findConcepts(JsonArray sentences) {
		environment.logDebug("NounAssemblerFC\n"+sentences);
		JsonArray result = new JsonArray();
		int len = sentences.size();
		JsonObject jo;
		JsonElement je;
		for (int i=0;i<len;i++) {
			je = sentences.get(i);
			environment.logDebug("NounAssemblerFCF\n"+je);
			jo =je.getAsJsonObject();
			_findCons(jo.get("sentences").getAsJsonArray(), result);
		}
		return result;
	}
	void _findCons(JsonArray theSentence, JsonArray result) {
		if (theSentence == null)
			return;
		JsonArray nodes = theSentence.get(0).getAsJsonObject().get("nodes").getAsJsonArray();;
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
				if (conc.get("text") != null) {
					theConceptText = conc.get("text").getAsString();
					environment.logDebug("NounAssembler-3 "+theConceptText);
					cx = new JsonObject();
					cx.addProperty("strt", Integer.toString(i));
					cx.addProperty("txt", theConceptText);
					result.add(cx);
				}

			}
		}

		environment.logDebug("NounAssembler+ "+result);
		//[{"strt":"1","txt":"Elephant shit"},{"strt":"3","txt":"flies"}]
	}
	

	void resolveNouns(ISentence sentence, JsonArray concepts, JsonArray nominals) {
		environment.logDebug("resolveNouns\n"+concepts);
		JsonArray result = new JsonArray();
		JsonArray nouns = sentence.getNouns();
		environment.logDebug("resolveNouns-1\n"+nouns);
		//[{"strt":0,"txt":"Scientists"},{"strt":7,"txt":"climate"},{"strt":8,"txt":"change"},{"strt":12,"txt":"carbon"},{"strt":13,"txt":"dioxide"}]

		JsonArray pNouns = sentence.getProperNouns();
		environment.logDebug("resolveNouns-2\n"+pNouns);
		JsonArray dbp = sentence.getDBpediaData();
		environment.logDebug("resolveNouns-3\n"+dbp);
		//[[],[{"strt":2,"enx":3,"txt":"encourages"}]]

		/////////////////////////////////////
		// We should assume that proper nouns, if available, are bound to be longer than
		// ordinary nouns.We should therefore add nouns to pnouns.
		////////////////////////////////////
		int len, len2;
		JsonObject jo, jx;
		JsonArray ja;
		if ((nouns != null) && (pNouns != null)) {
			len = pNouns.size();
			for (int i=0;i<len;i++) {
				jo = nouns.get(i).getAsJsonObject();
				if (!pNouns.contains(jo))
					pNouns.add(jo);
			}
		}
		/////////////////////////////////////
		// Then set that backto nouns
		/////////////////////////////////////
		nouns = pNouns;
		set.newSet();
		/////////////////////////////////////
		// Add concepts to a JsonSet
		/////////////////////////////////////
		if (concepts != null) {
			len = concepts.size();
			for (int i=0;i<len;i++) {
				jo = concepts.get(i).getAsJsonObject();
				set.addTriple(jo);
			}
		}
		//////////////////////////////////////
		// now merge nouns into that JsonSet
		//////////////////////////////////////
		environment.logDebug("NOUNS-1\n"+nouns);
		environment.logDebug("NOUNS-2\n"+set.getData());
		if (nouns!= null) {
			len = nouns.size();
			for (int i=0;i<len;i++) {
				jo = nouns.get(i).getAsJsonObject();
				set.addTriple(jo);
			}
		}
		JsonArray fudge = set.getData();
		environment.logDebug("NOUNS-3\n"+fudge);
		//[{"strt":"0","txt":"Scientists"},{"strt":"8","txt":"climate change"},{"strt":"13","txt":"carbon dioxide"}]
		// JsonSet works!!!
		nouns = fudge;
		//////////////////////////////////
		// now update nouns with DBpedia - if any
		//////////////////////////////////
		String txt;
		JsonObject match;
		JsonArray toRemove = new JsonArray();
		if (dbp != null) {
			len = dbp.size();
			JsonArray droppers;
			for (int i=0;i<len;i++) {
				Object ox = dbp.get(i);
				environment.logDebug("RESOLVING-X "+ox);
				jo = dbp.get(i).getAsJsonObject();
				txt = jo.get("strt").getAsString();
				// match dbpedia in nouns
				IResult rx = match(txt, jo, nouns);
				environment.logDebug("RESOLVING-1 "+rx+"\n"+jo);
				
			}
		}
		JsonArray pn = processNominals(nominals, nouns);
		
		if (pn != null && !pn.isEmpty()) {
			len = pn.size();
			for (int i=0;i<len;i++)
				nouns.remove(pn.get(i));
		}
		environment.logDebug("BigResolve\n"+nouns);
		//[{"strt":"1","txt":"The pandemic"},{"strt":"18","txt":"(NAFLD"},{"strt":"38","txt":"specifically with dietary palm oil"},{"strt":"40","txt":"(PO"},{"strt":"3","txt":"obesity","dbp":{"strt":"obesity","kid":"http://dbpedia.org/resource/Obesity","dbp":"1.0"}},{"strt":"5","txt":"type"},{"strt":"8","txt":"2 diabetes mellitus"},{"strt":"16","txt":"nonalcoholic fatty liver disease","dbp":{"strt":"nonalcoholic fatty liver disease","kid":"http://dbpedia.org/resource/Non-alcoholic_fatty_liver_disease","dbp":"1.0"}},{"strt":"26","txt":"dietary intake"},{"strt":"29","txt":"saturated fats"}]

		sentence.setResolvedNouns(nouns);
	}
	
	
	
	IResult match(String txt, JsonObject dbp, JsonArray nouns) {
		environment.logDebug("MATCHING "+txt+"\n"+nouns);
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
		boolean isMultiWord = numWords>1;
		JsonObject jo;
		int strt = 0;
		for (int i=0;i<len;i++) {
			temp = nouns.get(i).getAsJsonObject();
			environment.logDebug("MATCHING-1 "+txt+"\n"+temp);
			label = temp.get("txt").getAsString().toLowerCase();
			strt = temp.get("strt").getAsJsonPrimitive().getAsInt();
			// exact match
			if (label.equals(comp)) {
				temp.add("dbp", dbp);
				//result.addProperty("strt", Integer.toString(strt));
				//result.addProperty("txt", txt);
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
	
	IResult _match(String txt, JsonObject nominal, JsonArray nouns) {
		environment.logDebug("XMATCHING "+txt+"\n"+nouns);
		IResult output = new ResultPojo();
		JsonObject result = new JsonObject();
		JsonArray droppers = new JsonArray();
		output.setResultObject(result);
		output.setResultObjectA(droppers);
		String comp = txt.toLowerCase().trim();
		int len = nouns.size();
		JsonObject temp;
		String label, lx;
		String [] textC = txt.split(" ");
		int numWords = textC.length;
		boolean isMultiWord = numWords>1;
		JsonObject jo;
		int strt = 0;
		//for each noun
		for (int i=0;i<len;i++) {
			temp = nouns.get(i).getAsJsonObject();
			environment.logDebug("XMATCHING-1 "+txt+"\n"+temp);
			label = temp.get("txt").getAsString().toLowerCase();
			strt = temp.get("strt").getAsJsonPrimitive().getAsInt();
			// exact match
			if (label.equals(comp)) {
				droppers.add(temp);
				return output;
			} else { // speculative check - are the next words compatible?
				if (!isMultiWord && comp.contains(label)) {
					droppers.add(temp);
					return output;
				} else {
					for (int j = 0; j<numWords;j++) {
						lx = textC[i];
						environment.logDebug("XMATCHING-2 "+lx+" "+comp);
						
						if (comp.contains(lx)) {
							environment.logDebug("XMATCHING-3 ");
							droppers.add(temp);
							return output;
						}

					}
			
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
