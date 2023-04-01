/**
 * 
 */
package org.topicquests.newasr.wg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IPOS;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * @author jackpark
 *
 */
public class WordGramBuilder {
	private ASREnvironment environment;
	private IAsrModel model;
	private JsonUtil util;
	private final String
		NOUN 	= "NOUN";
	/**
	 * 
	 */
	public WordGramBuilder(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		util = new JsonUtil();
	}

	/*****************8
	 Predicates for "Scientists have been thought to believe that climate change is caused by  carbon dioxide"
	 [{
		"strt": 1,
		"txt": "have been thought to believe"
	 }, {
		"strt": 9,
		"txt": "is caused by"
	 }]
	 DBpedia
	 [
		["climate change", "http://dbpedia.org/resource/Global_warming", "0.9999928412462271"],
		["carbon dioxide", "http://dbpedia.org/resource/Carbon_dioxide", "0.9993317763086826"]
	 ]
	 Wikidata - when it will be available
	 ["Germany", "Q1206012", "LOC", ["country in Central Europe"]]
	 ********************/
	/**
	 * Build the WordGram graph for the given {@code sentence}
	 * @param sentence
	 * @return
	 */
	public IResult processSentence(ISentence sentence) {
		long sentenceId =sentence.getId();
		IResult result = new ResultPojo();
		String text = sentence.getText();
		JsonArray predicates = sentence.getPredicatePhrases();
		JsonArray dbpedia = sentence.getDBpediaData();
		JsonArray wikidata = sentence.getWikiData();
		String spacyResults = sentence.getSpacyData();
		Iterator<JsonElement> itr;
		JsonObject jo;
		JsonArray ja;
		String snippet, idx;
		int loc;
		IWordGram wg;
		IResult r;
		List<IWordGram> predList = new ArrayList<IWordGram>();
		List<JsonObject> dbpList = new ArrayList<JsonObject>();
		List<JsonObject> wdList = new ArrayList<JsonObject>();
		List<IWordGram> dbpWgList = new ArrayList<IWordGram>();
		List<IWordGram> wdWgList = new ArrayList<IWordGram>();
		List<IWordGram> nounWgList = new ArrayList<IWordGram>();
		///////////////////
		// Process the predicates
		///////////////////
		if (predicates != null) {
			itr = predicates.iterator();
			while (itr.hasNext()) {
				jo = itr.next().getAsJsonObject();
				snippet = jo.get("txt").getAsString();
				r = model.processTerm(snippet, IPOS.VERB_POS);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				idx = (String)r.getResultObject();
				r = model.getThisTermById(idx);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				wg = (IWordGram)r.getResultObject();
				predList.add(wg);
			}
		}
		///////////////////
		// Process DBpedia
		///////////////////
		if (dbpedia != null) {
			itr = dbpedia.iterator();
			while (itr.hasNext()) {
				ja = itr.next().getAsJsonArray();
				jo =new JsonObject();
				snippet = ja.get(0).getAsString();
				jo.addProperty("txt", snippet);
				jo.addProperty("url", ja.get(1).getAsString());
				dbpList.add(jo);
				r = model.processTerm(snippet, IPOS.NOUN_POS);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				idx = (String)r.getResultObject();
				r = model.getThisTermById(idx);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				wg = (IWordGram)r.getResultObject();
				dbpWgList.add(wg);

			}
		}
		///////////////////
		// Process the Wikidata
		///////////////////
		if (wikidata != null) {
			itr = wikidata.iterator();
			while (itr.hasNext()) {
				ja = itr.next().getAsJsonArray();
				jo =new JsonObject();
				snippet = ja.get(0).getAsString();
				jo.addProperty("txt", snippet);
				jo.addProperty("id", ja.get(1).getAsString());
				jo.addProperty("type", ja.get(2).getAsString());
				jo.addProperty("desc", ja.get(3).getAsString());
				wdList.add(jo);
				r = model.processTerm(snippet, IPOS.NOUN_POS);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				idx = (String)r.getResultObject();
				r = model.getThisTermById(idx);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				wg = (IWordGram)r.getResultObject();
				dbpWgList.add(wg);
			}
		}
		///////////////////
		// At this point, we may have:
		//	* Predicate phrases and their wordgrams
		//  * Named Entities and their wordgrams
		// We must now make sense of what's left
		// for that, we turn to the spaCy POS stuff
		///////////////////
		String txt, conc, pos, lemma;
		try {
			JsonObject spacyParse = util.parse(spacyResults);
			JsonArray nodes = spacyParse.get("nodes").getAsJsonArray();
			int len = nodes.size();
			for (int i=0; i<len; i++) {
				jo = nodes.get(i).getAsJsonObject();
				txt =jo.get("text").getAsString();
				pos= jo.get("pos").getAsString();
				lemma= jo.get("lemma").getAsString();
				if (jo.get("concepts")  != null)
					conc = jo.get("concepts").getAsJsonObject().get("text").getAsString();
				else
					conc = null;
				if (pos.equals(NOUN) && conc != null) {
					r = model.processTerm(txt, IPOS.NOUN_POS);  // should we use lemma?
					if (r.hasError())
						result.addErrorString(r.getErrorString());
					idx = (String)r.getResultObject();
					r = model.getThisTermById(idx);
					if (r.hasError())
						result.addErrorString(r.getErrorString());
					wg = (IWordGram)r.getResultObject();
					if (!(dbpWgList.contains(wg) || wdWgList.contains(wg)))
						nounWgList.add(wg);
				}
			}
			environment.logError("BUILDER1 "+sentenceId+"\n"+predList+"\n"+dbpList+"\n"+dbpWgList+"\n"+nounWgList,null);
		} catch (Exception e) {
			result.addErrorString(e.getMessage());
			environment.logError("WGB-1 "+e.getMessage(), e);
			e.printStackTrace();
		}
		
		
		return result;
	}
}
/**
A spacy sentence
{
	"nodes": [{
		"concepts": {
			"concepts": ["http:\/\/ontology.apa.org\/apaonto\/termsonlyOUT%20(5).owl#Scientists", "http:\/\/localhost\/plosthes.2017-1#8580"],
			"text": "Scientists"
		},
		"pos": "NOUN",
		"start": 0,
		"i": 0,
		"lemma": "scientist",
		"tag": "NNS",
		"text": "Scientists",
		"dep": "nsubjpass"
	}, {
		"pos": "AUX",
		"start": 11,
		"i": 1,
		"lemma": "have",
		"tag": "VBP",
		"text": "have",
		"dep": "aux"
	}, {
		"pos": "AUX",
		"start": 16,
		"i": 2,
		"lemma": "be",
		"tag": "VBN",
		"text": "been",
		"dep": "auxpass"
	}, {
		"pos": "VERB",
		"start": 21,
		"i": 3,
		"lemma": "think",
		"tag": "VBN",
		"text": "thought",
		"dep": "ROOT"
	}, {
		"pos": "PART",
		"start": 29,
		"i": 4,
		"lemma": "to",
		"tag": "TO",
		"text": "to",
		"dep": "aux"
	}, {
		"pos": "VERB",
		"start": 32,
		"i": 5,
		"lemma": "believe",
		"tag": "VB",
		"text": "believe",
		"dep": "xcomp"
	}, {
		"pos": "SCONJ",
		"start": 40,
		"i": 6,
		"lemma": "that",
		"tag": "IN",
		"text": "that",
		"dep": "mark"
	}, {
		"pos": "NOUN",
		"start": 45,
		"i": 7,
		"lemma": "climate",
		"tag": "NN",
		"text": "climate",
		"dep": "compound"
	}, {
		"concepts": {
			"concepts": ["http:\/\/purl.obolibrary.org\/obo\/ExO_0000014", "http:\/\/purl.obolibrary.org\/obo\/ENVO_01000629"],
			"text": "climate change"
		},
		"pos": "NOUN",
		"start": 53,
		"i": 8,
		"lemma": "change",
		"tag": "NN",
		"text": "change",
		"dep": "nsubjpass"
	}, {
		"pos": "AUX",
		"start": 60,
		"i": 9,
		"lemma": "be",
		"tag": "VBZ",
		"text": "is",
		"dep": "auxpass"
	}, {
		"pos": "VERB",
		"start": 63,
		"i": 10,
		"lemma": "cause",
		"tag": "VBN",
		"text": "caused",
		"dep": "ccomp"
	}, {
		"pos": "ADP",
		"start": 70,
		"i": 11,
		"lemma": "by",
		"tag": "IN",
		"text": "by",
		"dep": "agent"
	}, {
		"pos": "SPACE",
		"start": 73,
		"i": 12,
		"lemma": " ",
		"tag": "_SP",
		"text": " ",
		"dep": "dep"
	}, {
		"pos": "NOUN",
		"start": 74,
		"i": 13,
		"lemma": "carbon",
		"tag": "NN",
		"text": "carbon",
		"dep": "compound"
	}, {
		"concepts": {
			"concepts": ["http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/sbmi.uth.tmc.edu\/ontology\/ochv#2393", "http:\/\/purl.allotrope.org\/ontologies\/material#AFM_0001116", "http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.bioontology.org\/ontology\/UATC\/V03AN02", "http:\/\/purl.bioontology.org\/ontology\/CSP\/2604-9089", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/11\/msv\/carbonDioxideConcentration"],
			"text": "carbon dioxide"
		},
		"pos": "NOUN",
		"start": 81,
		"i": 14,
		"lemma": "dioxide",
		"tag": "NN",
		"text": "dioxide",
		"dep": "pobj"
	}],
	"tree": {
		"left": [{
			"concepts": {
				"concepts": ["http:\/\/ontology.apa.org\/apaonto\/termsonlyOUT%20(5).owl#Scientists", "http:\/\/localhost\/plosthes.2017-1#8580"],
				"text": "Scientists"
			},
			"pos": "NOUN",
			"start": 0,
			"i": 0,
			"lemma": "scientist",
			"tag": "NNS",
			"text": "Scientists",
			"dep": "nsubjpass"
		}, {
			"pos": "AUX",
			"start": 11,
			"i": 1,
			"lemma": "have",
			"tag": "VBP",
			"text": "have",
			"dep": "aux"
		}, {
			"pos": "AUX",
			"start": 16,
			"i": 2,
			"lemma": "be",
			"tag": "VBN",
			"text": "been",
			"dep": "auxpass"
		}],
		"pos": "VERB",
		"start": 21,
		"i": 3,
		"lemma": "think",
		"right": [{
			"left": [{
				"pos": "PART",
				"start": 29,
				"i": 4,
				"lemma": "to",
				"tag": "TO",
				"text": "to",
				"dep": "aux"
			}],
			"pos": "VERB",
			"start": 32,
			"i": 5,
			"lemma": "believe",
			"right": [{
				"left": [{
					"pos": "SCONJ",
					"start": 40,
					"i": 6,
					"lemma": "that",
					"tag": "IN",
					"text": "that",
					"dep": "mark"
				}, {
					"concepts": {
						"concepts": ["http:\/\/purl.obolibrary.org\/obo\/ExO_0000014", "http:\/\/purl.obolibrary.org\/obo\/ENVO_01000629"],
						"text": "climate change"
					},
					"left": [{
						"pos": "NOUN",
						"start": 45,
						"i": 7,
						"lemma": "climate",
						"tag": "NN",
						"text": "climate",
						"dep": "compound"
					}],
					"pos": "NOUN",
					"start": 53,
					"i": 8,
					"lemma": "change",
					"tag": "NN",
					"text": "change",
					"dep": "nsubjpass"
				}, {
					"pos": "AUX",
					"start": 60,
					"i": 9,
					"lemma": "be",
					"tag": "VBZ",
					"text": "is",
					"dep": "auxpass"
				}],
				"pos": "VERB",
				"start": 63,
				"i": 10,
				"lemma": "cause",
				"right": [{
					"pos": "ADP",
					"start": 70,
					"i": 11,
					"lemma": "by",
					"tag": "IN",
					"text": "by",
					"dep": "agent"
				}],
				"tag": "VBN",
				"text": "caused",
				"dep": "ccomp"
			}],
			"tag": "VB",
			"text": "believe",
			"dep": "xcomp"
		}, {
			"pos": "SPACE",
			"start": 73,
			"i": 12,
			"lemma": " ",
			"right": [{
				"concepts": {
					"concepts": ["http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/sbmi.uth.tmc.edu\/ontology\/ochv#2393", "http:\/\/purl.allotrope.org\/ontologies\/material#AFM_0001116", "http:\/\/purl.obolibrary.org\/obo\/CHEBI_16526", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.bioontology.org\/ontology\/UATC\/V03AN02", "http:\/\/purl.bioontology.org\/ontology\/CSP\/2604-9089", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/4\/id\/200906065434156638", "http:\/\/purl.jp\/bio\/11\/msv\/carbonDioxideConcentration"],
					"text": "carbon dioxide"
				},
				"left": [{
					"pos": "NOUN",
					"start": 74,
					"i": 13,
					"lemma": "carbon",
					"tag": "NN",
					"text": "carbon",
					"dep": "compound"
				}],
				"pos": "NOUN",
				"start": 81,
				"i": 14,
				"lemma": "dioxide",
				"tag": "NN",
				"text": "dioxide",
				"dep": "pobj"
			}],
			"tag": "_SP",
			"text": " ",
			"dep": "dep"
		}],
		"tag": "VBN",
		"text": "thought",
		"dep": "ROOT"
	},
	"id": "s_0",
	"text": "Scientists have been thought to believe that climate change is caused by  carbon dioxide"
} 
 */
