/**
 * 
 */
package org.topicquests.newasr.trip;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class TripleAnalyzer {
	private ASREnvironment environment;
	private IAsrModel model;
	private final String
		COMMA		= ",",
		AND			= "and",
		OR			= "or";
	/**
	 * 
	 */
	public TripleAnalyzer(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		// TODO Auto-generated constructor stub
	}
	/**
	 * BAD structure
	 	[{
				"strt": "1",
				"txt": "Some scientists"
			}, {
				"strt": 2,
				"txt": " believe"
			}, {
				"strt": "5",
				"txt": "other scientists"
			}, {
				"strt": 6,
				"txt": " believe"
			}, {
				"strt": "9",
				"txt": "carbon dioxide",
				"dbp": {
					"strt": "carbon dioxide",
					"kid": "http://dbpedia.org/resource/Carbon_dioxide",
					"dbp": "0.9990415953521904"
				}
			}, {
				"strt": 10,
				"txt": " causes"
			}, {
				"strt": "12",
				"txt": "climate change",
				"dbp": {
					"strt": "climate change",
					"kid": "http://dbpedia.org/resource/Global_warming",
					"dbp": "0.9999940930657609"
				}
			}]
	 */
	/**
	 * <p>Isolating triple structures in a sentence is complex; it is made more complex when
	 * a sentence is conjunctive, disjunctive, or both
	 * complicate the isolation of triple subjects and objects".</>
	 * <p>We collect evidence of conjuncts and disjuncts and use those to break a sentence into its collections
	 * of triples</p>
	 * <p>This code serves one and only one purpose: to capture what we call <em>raw</em> triples, those statements
	 * as they exist in the sentence</p>
	 * @param sentence
	 * @param predicates
	 * @param nouns
	 * @return
	 */
	public JsonArray bigDamnTripleAnalyze(ISentence sentence, JsonArray predicates, JsonArray nouns) {
		boolean hasConjuncts = sentence.hasConjuncts();
		boolean hasDisjuncts = sentence.hasDisjuncts();
		JsonArray conjuncts = sentence.getConjuncts();
		JsonArray disjuncts = sentence.getDisjuncts();
		
		String text = sentence.getText();
		environment.logDebug("BigDamnTripleAnalysis "+hasConjuncts+" "+hasDisjuncts+" "+"\n"+predicates+"\n"+nouns);
		JsonArray result = new JsonArray();
		int counts = text.length();
		int predLength = predicates.size();
		List<JsonObject>things = new ArrayList<JsonObject>();
		JsonObject [] construct = new JsonObject[counts];
		for (int i=0;i<counts;i++) 
			construct[i]=null;
		//populate with predicates
		int where = 0;
		JsonObject jo;
		for (int i=0;i<predLength;i++) {
			jo = (predicates.get(i).getAsJsonObject());
			environment.logDebug("BigDamnTripleAnalysis-1a "+predLength+" "+i+"\n"+jo);
			where = jo.get("strt").getAsJsonPrimitive().getAsInt();
			construct[where] = jo;
		}
		environment.logDebug("BigDamnTripleAnalysis-1\n"+construct);
		//populate with nouns
		int nounLength = nouns.size();
		
		for (int i=0;i<nounLength;i++) {
			jo = nouns.get(i).getAsJsonObject();
			where = jo.get("strt").getAsJsonPrimitive().getAsInt();
			construct[where] = jo;
		}
		// populate with conjuncts if any
		if (conjuncts != null && !conjuncts.isEmpty()) {
			nounLength = conjuncts.size();
			for (int i=0;i<nounLength;i++) {
				jo = conjuncts.get(i).getAsJsonObject();
				where = jo.get("strt").getAsJsonPrimitive().getAsInt();
				construct[where] = jo;
			}
		}
		// populate with disjuncts if any
		if (disjuncts != null && !disjuncts.isEmpty()) {
			nounLength = disjuncts.size();
			for (int i=0;i<nounLength;i++) {
				jo = disjuncts.get(i).getAsJsonObject();
				where = jo.get("strt").getAsJsonPrimitive().getAsInt();
				construct[where] = jo;
			}
		}		environment.logDebug("BigDamnTripleAnalysis-2\n"+construct);
		for (int i=0;i<counts;i++) {
			if (construct[i] != null)
				things.add(construct[i]);
		}
		environment.logDebug("BigDamnTripleAnalysis-3\n"+things+"\n"+conjuncts+"\n"+disjuncts);
		///////////////////////////
		// normalize the list
		///////////////////////////
		//if (predicates.size() == 1)
		//JsonArray triples = null;
		if (hasConjuncts || hasDisjuncts)
			result = makeTriples(things, predicates);
		else
			result.add( makeTriple(0, 0, things, predicates));
		environment.logDebug("BigDamnTripleAnalysis+\n"+things+"\n"+result);
		return result;
	}

	/**
	 * For conjunctive/disjunctive sentences
	 * @param things
	 * @param preds
	 * @return
	 */
	JsonArray makeTriples(List<JsonObject> things, JsonArray preds) {
		environment.logDebug("MAKETriples "+preds);
		if (preds.size() == 1)
			return singlePredTriples(things, preds);
		return multiPredTriples(things,preds);
	}
	
	JsonArray singlePredTriples(List<JsonObject> things, JsonArray preds) {
		environment.logDebug("SinglePredTriples "+preds);
		JsonArray result = new JsonArray();
		JsonObject thePred = preds.get(0).getAsJsonObject();
		int predLoc = thePred.get("strt").getAsJsonPrimitive().getAsInt();
		List<JsonObject> LHS = new ArrayList<JsonObject>();
		List<JsonObject> RHS = new ArrayList<JsonObject>();
		int len = things.size();
		environment.logDebug("SinglePredTriples-1 "+predLoc+" "+len);
		JsonObject jo;
		boolean predFound = false;
		for (int i=0;i<len;i++) {
			jo = things.get(i).getAsJsonObject();
			if (jo.equals(thePred)) 
				predFound = true;
			else if (!predFound)
				LHS.add(jo);
			else
				RHS.add(jo);
		}
		environment.logDebug("SPT-1 "+thePred+"\n"+LHS+"\n"+RHS);
		LHS = trimList(LHS);
		RHS = trimList(RHS);
		int lhsLen = LHS.size();
		int rhsLen = RHS.size();
		JsonObject subj, obj;
		for (int i=0;i<lhsLen;i++) {
			subj = LHS.get(i);
			for (int j=0;j<rhsLen;j++) {
				obj = RHS.get(j);
				result.add(createTriple(subj, thePred, obj));
			}
		}
		environment.logDebug("SinglePredTriplesBIG\n"+result);

		return result;		
	}
	
	JsonObject createTriple(JsonObject subj, JsonObject pred, JsonObject obj) {
		JsonObject result = new JsonObject();
		result.add("subj", subj);
		result.add("pred", pred);
		result.add("obj", obj);
		return result;
	}
	
	List<JsonObject> trimList(List<JsonObject> theList) {
		List<JsonObject> result = new ArrayList<JsonObject>();
		int len = theList.size();
		JsonObject jo = null, joprev = null;
		String txt;
		boolean conjFound = false;
		for (int i=0;i<len;i++) {
			jo = theList.get(i);
			txt = jo.get("txt").getAsString();
			if (!isConj(txt))
				result.add(jo);
		}
		environment.logDebug("TRIMLIST++ \n"+theList+"\n"+result);
		return result;
	}
	
	boolean isConj(String txt) {
		return (txt.equals(COMMA) || txt.equals(AND) || txt.equals(OR));
	}
	JsonArray multiPredTriples(List<JsonObject> things, JsonArray preds) {
		environment.logDebug("MultiPredTriples "+preds);
		JsonArray result = new JsonArray();
	
		return result;		
	}
	/**
	 * Recursive
	 * @param predlocs
	 * @param index
	 * @param things
	 * @return
	 */
	JsonObject makeTriple(int index, int predIndex, List<JsonObject> things, JsonArray preds) {
		environment.logDebug("MakeTriple "+index+" "+predIndex+"\n"+things+"\n"+preds);
		//MakeTriple 0 0
		//[{"strt":"1","txt":"Some scientists"}, {"strt":2,"txt":" believe"}, {"strt":"5","txt":"other scientists"}, {"strt":6,"txt":" believe"}, {"strt":"9","txt":"carbon dioxide","dbp":{"strt":"carbon dioxide","kid":"http://dbpedia.org/resource/Carbon_dioxide","dbp":"0.9990415953521904"}}, {"strt":10,"txt":" causes"}, {"strt":"12","txt":"climate change","dbp":{"strt":"climate change","kid":"http://dbpedia.org/resource/Global_warming","dbp":"0.9999940930657609"}}]
		//[{"strt":2,"txt":" believe"},{"strt":6,"txt":" believe"},{"strt":10,"txt":" causes"}]
			//MakeTriple 1 1
			//[{"strt":"1","txt":"Some scientists"}, {"strt":2,"txt":" believe"}, {"strt":"5","txt":"other scientists"}, {"strt":6,"txt":" believe"}, {"strt":"9","txt":"carbon dioxide","dbp":{"strt":"carbon dioxide","kid":"http://dbpedia.org/resource/Carbon_dioxide","dbp":"0.9990415953521904"}}, {"strt":10,"txt":" causes"}, {"strt":"12","txt":"climate change","dbp":{"strt":"climate change","kid":"http://dbpedia.org/resource/Global_warming","dbp":"0.9999940930657609"}}]
			//[{"strt":2,"txt":" believe"},{"strt":6,"txt":" believe"},{"strt":10,"txt":" causes"}]
				//MakeTriple 2 2
				//[{"strt":"1","txt":"Some scientists"}, {"strt":2,"txt":" believe"}, {"strt":"5","txt":"other scientists"}, {"strt":6,"txt":" believe"}, {"strt":"9","txt":"carbon dioxide","dbp":{"strt":"carbon dioxide","kid":"http://dbpedia.org/resource/Carbon_dioxide","dbp":"0.9990415953521904"}}, {"strt":10,"txt":" causes"}, {"strt":"12","txt":"climate change","dbp":{"strt":"climate change","kid":"http://dbpedia.org/resource/Global_warming","dbp":"0.9999940930657609"}}]
				//[{"strt":2,"txt":" believe"},{"strt":6,"txt":" believe"},{"strt":10,"txt":" causes"}]
		int pointer = index;
		if (index > 0) {
			// we are looking for the next subject
			// first pass you jump over subj,pred
			// second pass you jump over subj,pred,obj,pred
			pointer = 2*index;
		}
			
		JsonObject result = new JsonObject();
		int predLength = preds.size();
		environment.logDebug("MakeTriple-1 "+predLength+" "+predIndex+"\n"+result);
		//3 0
		//{}
			//3 1
			//{}
				//3 2
				//{}
		result.add("subj", things.get(pointer));
		result.add("pred", preds.get(predIndex));
		environment.logDebug("MakeTriple-2 "+predLength+" "+predIndex+"\n"+result);
		//3 0
		//{"subj":{"strt":"1","txt":"Some scientists"},"pred":{"strt":2,"txt":" believe"}}
			//3 1
			//{"subj":{"strt":"5","txt":"other scientists"},"pred":{"strt":6,"txt":" believe"}}
				//3 2
				//{"subj":{"strt":"9","txt":"carbon dioxide","dbp":{"strt":"carbon dioxide","kid":"http://dbpedia.org/resource/Carbon_dioxide","dbp":"0.9990415953521904"}},"pred":{"strt":10,"txt":" causes"}}
		if (predLength ==1 || predIndex == predLength) {
			environment.logDebug("MakeTriple-3 "+ things.get(pointer+2));
			result.add("obj", things.get(pointer+2));
		} else {
			environment.logDebug("MakeTriple-4 "+predLength+" "+predIndex+"\n"+result);
			//3 0
			//{"subj":{"strt":"1","txt":"Some scientists"},"pred":{"strt":2,"txt":" believe"}}
				//3 1
				//{"subj":{"strt":"5","txt":"other scientists"},"pred":{"strt":6,"txt":" believe"}}
			if (predIndex >= predLength-1) {
				if (result.get("obj") == null) {
					environment.logDebug("MakeTriple-5 "+pointer+" "+result.get("pred")+"\n"+things);
					result.add("obj", things.get(pointer+2));
				}
				return result;
			}
			JsonObject jo = makeTriple(index+1, predIndex+1, things,preds); // recurse
			result.add("obj", jo);
		}
		environment.logDebug("MakeTriple+\n"+result);

		return result;
	}
	JsonObject simpleTriple(List<JsonObject> sentence, JsonObject pred, JsonArray nouns) {
		JsonObject result= new JsonObject();
		
		return result;
	}
}
/**
 "Greenhouse gas causes climate change"
 [{"strt":0,"txt":"Greenhouse"}, {"strt":1,"txt":"gas"}, {"strt":2,"txt":" causes"}, {"strt":3,"txt":"climate"}, {"strt":4,"txt":"change"}]
 "Greenhouse gasses cause climate change"
 [{"strt":0,"txt":"Greenhouse"}, {"strt":1,"txt":"gasses"}, {"strt":2,"txt":" cause"}, {"strt":3,"txt":"climate"}, {"strt":4,"txt":"change"}]
"Carbon dioxide has been thought to cause climate change"
[{"strt":0,"txt":"Carbon"}, {"strt":1,"txt":"dioxide"}, {"strt":2,"txt":"has been thought to cause"}, {"strt":7,"txt":"climate"}, {"strt":8,"txt":"change"}]
"Scientists have been thought to believe that climate change is caused by  carbon dioxide"


 */
