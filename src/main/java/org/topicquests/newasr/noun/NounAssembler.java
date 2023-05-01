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
		JsonArray predicates = sentence.getPredicatePhrases();
		environment.logDebug("NounAssembler-\n"+predicates);

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
			JsonObject spacySentence = spacyData.get(0).getAsJsonObject();
			resolveNouns(sentence, concepts, nominals, spacySentence);
			environment.logDebug("NounAssembler-2\n"+sentence.getData());
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
			_findCons(jo.get("sentence").getAsJsonArray(), result);
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
	

	void resolveNouns(ISentence sentence, JsonArray concepts, JsonArray nominals, JsonObject spacySentence) {
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
		JsonArray droppers = null, adders = null, temp;
		if (dbp != null) {
			len = dbp.size();
			for (int i=0;i<len;i++) {
				Object ox = dbp.get(i);
				environment.logDebug("RESOLVING-X "+ox);
				jo = dbp.get(i).getAsJsonObject();
				txt = jo.get("strt").getAsString();
				// match dbpedia in nouns
				IResult rx = match(txt, jo, nouns, sentence, spacySentence);
				if (rx != null) {
					temp = (JsonArray)rx.getResultObjectA();
					if (temp != null && !temp.isEmpty()) {
						if (droppers == null)
							droppers = new JsonArray();
						droppers.addAll(temp);
					}
					match = (JsonObject)rx.getResultObject();
					if (match != null) {
						if (adders == null)
							adders = new JsonArray();
						adders.add(match);
					}
					environment.logDebug("RESOLVING-1 "+rx+"\n"+jo);
				}
				
			}
			if (adders != null)
				nouns.addAll(adders);
		}
		if (droppers !=null) {
			len = droppers.size();
			for (int i=0;i<len;i++)
				nouns.remove(droppers.get(i));
		}
		environment.logDebug("RESOLVING-BIG "+adders+"\n"+nouns+"\n"+droppers);

		JsonArray pn = processNominals(nominals, nouns);
		
		if (pn != null && !pn.isEmpty()) {
			len = pn.size();
			for (int i=0;i<len;i++)
				nouns.remove(pn.get(i));
		}
		droppers = heuristicCleanup(nouns);
		environment.logDebug("RESOLVING-BIGBIG "+droppers);
		if (!droppers.isEmpty()) {
			len = droppers.size();
			for (int i=0;i<len;i++)
				nouns.remove(droppers.get(i));
			
		}
		environment.logDebug("BigResolve\n"+nouns);
		//[{"strt":"1","txt":"The pandemic"},{"strt":"18","txt":"(NAFLD"},{"strt":"38","txt":"specifically with dietary palm oil"},{"strt":"40","txt":"(PO"},{"strt":"3","txt":"obesity","dbp":{"strt":"obesity","kid":"http://dbpedia.org/resource/Obesity","dbp":"1.0"}},{"strt":"5","txt":"type"},{"strt":"8","txt":"2 diabetes mellitus"},{"strt":"16","txt":"nonalcoholic fatty liver disease","dbp":{"strt":"nonalcoholic fatty liver disease","kid":"http://dbpedia.org/resource/Non-alcoholic_fatty_liver_disease","dbp":"1.0"}},{"strt":"26","txt":"dietary intake"},{"strt":"29","txt":"saturated fats"}]

		sentence.setResolvedNouns(nouns);
	}
	
	JsonArray heuristicCleanup(JsonArray nouns) {
		JsonArray result = new JsonArray();
		int len = nouns.size();
		JsonObject jo;
		String txt;
		for (int i=0;i<len;i++) {
			jo = nouns.get(i).getAsJsonObject();
			environment.logDebug("HC "+jo);
			if (jo.get("txt") != null) {
				txt = jo.get("txt").getAsString();
				if (txt.startsWith("("))
					result.add(jo);
			} else
				result.add(jo);
		}
		return result;
	}
	String burpParens(String text) {
		String result = text.trim();
		result = result.replaceAll("\\(", "");
		result = result.replaceAll("\\)", "");
		return result;
	}
	
	int findInNodes(String txt, JsonObject spacySentence) {
		environment.logDebug("FINDNODESX "+txt);
		JsonArray sx = spacySentence.get("sentences").getAsJsonArray();
		JsonObject theSent = sx.get(0).getAsJsonObject();
		int where = -1;
		String [] tA = txt.split(" ");
		int tlen = tA.length;
		boolean isPhrase = tlen > 1;
		environment.logDebug("FINDNODESY "+tlen+" "+isPhrase);
		JsonArray nodes = theSent.get("nodes").getAsJsonArray();
		int len = nodes.size();
		
		JsonObject nod, nod2;
		int loc = -1;
		String label, l2;
		boolean found = true;
		for (int i=0;i<len;i++) {
			nod = nodes.get(i).getAsJsonObject();
			loc = i;
			label = nod.get("text").getAsString();
			environment.logDebug("FINDNODES-1 "+loc+" "+label+"\n"+nod);
			if (txt.contains(label)) {
				environment.logDebug("FINDNODES-2 "+isPhrase+" "+loc+" "+label+" "+txt);
				found = true;
				if (isPhrase) {
					for (int j=1;j<tlen;j++) {
						nod2 = nodes.get(i+j).getAsJsonObject();
						l2 = nod2.get("text").getAsString();
						environment.logDebug("FINDNODES-3 "+l2+"\n"+nod2);
						if (!txt.contains(l2)) {
							found = false;
							break;
						}
					}
				}
				if (found)
					return loc;
			}
		}
		
		return where;
	}
	
	/**
	 * <p>When finding DBpedia nodes, may have to look behind as well as ahead</p>
	 * 
	 * @param txt
	 * @param dbp
	 * @param nouns
	 * @param sentence
	 * @return
	 */
	IResult match(String txt, JsonObject dbp, JsonArray nouns, ISentence sentence, JsonObject spacySentence) {
		environment.logDebug("MATCHING "+txt+"\n"+nouns);
		//////////////////////////
		// We make a text array
		// Note some words can be surrounded in parens, e.g. (T2DM) which are considered
		// acronyms of other terms in the sentence.
		// We must strip the sentence of parens first
		// We must then burp the final array of spurious spaces
		//////////////////////////
		String sentenceText = burpParens(sentence.getText()).trim();
		String textArray [] = sentenceText.split(" ");
		int foundLoc = findLabelInArray(txt, textArray);
		////////////////////////////
		// when a phrase is found in the sentence, it must be validated with the nodes
		////////////////////////////
		int nodeLoc = findInNodes(txt, spacySentence);
		environment.logDebug("MATCHING-F "+foundLoc+" "+nodeLoc);
		String labelArray [] = txt.trim().split(" ");
		int labelLen = labelArray.length;
		boolean multiWordLabel =  labelLen > 1;
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
		/////////////////////////
		// Gping fishing in the nouns
		// If found, update the noun with dbp
		// Otherwise, create a new noun with dbpedia
		//////////////////////////
		boolean isFound = false;
		boolean startMatch = false;
		for (int i=0;i<len;i++) {
			// for each noun
			temp = nouns.get(i).getAsJsonObject();
			environment.logDebug("MATCHING-1 "+txt+"\n"+temp);
			label = temp.get("txt").getAsString().toLowerCase();
			strt = temp.get("strt").getAsJsonPrimitive().getAsInt();
			if (foundLoc > -1)
				startMatch = foundLoc == strt;
			// exact match
			if (label.equals(comp)) {
				temp.add("dbp", dbp);
				//result.addProperty("strt", Integer.toString(strt));
				//result.addProperty("txt", txt);
				return output;
			} else if (startMatch) {
				// labels do not match but the text was found here
				droppers.add(temp); //get rid of this one
				// make new one
				result.addProperty("strt", Integer.toString(nodeLoc));
				result.addProperty("txt", txt);
				result.add("dbp", dbp);
				return output;
			} else if (label.contains(txt) && foundLoc > -1) {
				//high risk heuristic
				droppers.add(temp); //get rid of this one
				// make new one
				result.addProperty("strt", Integer.toString(nodeLoc));
				result.addProperty("txt", txt);
				result.add("dbp", dbp);
				return output;
			} else { // speculative check - are the next words compatible?
				/////////////////////
				// this is weak and probably will fail
				////////////////////
				if (comp.contains(label)) {
					boolean found = true;
					droppers.add(temp);
					for (int j = 1; j<numWords;j++) {
						jo = nouns.get(++i).getAsJsonObject();
						environment.logDebug("MATCHING-2 "+txt+"\n"+jo);
						label = jo.get("txt").getAsString().toLowerCase();
						environment.logDebug("MATCHING-3 "+txt+" "+label);
						if (containsDBP(temp))
							droppers.add(jo);
						if (!comp.contains(label)) {
							environment.logDebug("MATCHING-4 ");
							found = false;
							break;
						}

					}
					if (found) {
						environment.logDebug("MATCHING-4XXX "+foundLoc+" "+strt+" "+txt);
						if (foundLoc > -1)
							result.addProperty("strt", Integer.toString(nodeLoc));
						else
							result.addProperty("strt", Integer.toString(strt));
						result.addProperty("txt", txt);
						result.add("dbp", dbp);
						return output;
					} 
					if (!isFound && foundLoc > -1)
						result.addProperty("strt", Integer.toString(nodeLoc));
						result.addProperty("txt", txt);
						result.add("dbp", dbp);
						return output;
				}
			}
		}
		environment.logDebug("MATCHING+ "+result);

		return null;
	}
	
	int findLabelInArray(String label, String [] textArray) {
		int result = -1;
		String labelArray [] =label.split(" ");
		int labelLen = labelArray.length;
		int textLen = textArray.length;
		String lx, tx;
		int where = -1;
		boolean found = false;
		for (int i=0;i<labelLen;i++) {
			lx = labelArray[i].toLowerCase();
			for (int j=0;j<textLen;j++) {
				tx = textArray[j].toLowerCase();
				if (lx.equals(tx)) {
					where = j;
					found = true;
					for (int k=i+1;k<labelLen;k++) {
						tx = textArray[k].toLowerCase();
						for (int l = j+1; l<textLen; l++) {
							tx = textArray[j].toLowerCase();
							if (!lx.equals(tx)) {
								found = false;
								where = -1;
							} 
						}

					}
				}
				if (found)
					result = where;
			}
		}
		
		return result;
	}
	
	boolean containsDBP(JsonObject node) {
		return (node.get("dbp") != null);
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
			if (label.equals(comp) /*&& !containsDBP(temp)*/) {
				droppers.add(temp);
				return output;
			} else { // speculative check - are the next words compatible?
				if (!isMultiWord && comp.contains(label) /*&& !containsDBP(temp)*/) {
					droppers.add(temp);
					return output;
				} else {
					for (int j = 0; j<numWords;j++) {
						lx = textC[j];
						environment.logDebug("XMATCHING-2 "+lx+" "+label);
						
						if (label.contains(lx) /*&& !containsDBP(temp)*/) {
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
