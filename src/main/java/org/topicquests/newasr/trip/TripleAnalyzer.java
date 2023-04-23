/**
 * 
 */
package org.topicquests.newasr.trip;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.api.ITripleModel;
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
	/**
	 * 
	 */
	public TripleAnalyzer(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		// TODO Auto-generated constructor stub
	}
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
	public JsonObject bigDamnAnalyze(ISentence sentence, JsonArray predicates, JsonArray nouns) {
		boolean hasConjuncts = sentence.hasConjuncts();
		boolean hasDisjuncts = sentence.hasDisjuncts();
		JsonArray conjuncts = sentence.getConjuncts();
		JsonArray disjuncts = sentence.getDisjuncts();
		
		String text = sentence.getText();
		environment.logDebug("BigDamAnalysis "+hasConjuncts+" "+hasDisjuncts+" "+"\n"+predicates+"\n"+nouns);
		JsonObject result = new JsonObject();
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
			environment.logDebug("BigDamAnalysis-1a "+predLength+" "+i+"\n"+jo);
			where = jo.get("strt").getAsJsonPrimitive().getAsInt();
			construct[where] = jo;
		}
		environment.logDebug("BigDamAnalysis-1\n"+construct);
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
		}		environment.logDebug("BigDamAnalysis-2\n"+construct);
		for (int i=0;i<counts;i++) {
			if (construct[i] != null)
				things.add(construct[i]);
		}
		environment.logDebug("BigDamAnalysis-3\n"+things+"\n"+conjuncts+"\n"+disjuncts);
		///////////////////////////
		// normalize the list
		///////////////////////////
		//if (predicates.size() == 1)
		result = makeTriple(0, 0, things, predicates);
		environment.logDebug("BigDamAnalysis+\n"+things+"\n"+result);
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
		int pointer = index;
		if (index > 0) {
			// we are looking for the next subject
			// first pass you jump over subj,pred
			// second pass you jump over subj,pred,obj,pred
			pointer = 2*index;
		}
			
		JsonObject result = new JsonObject();
		int predLength = preds.size();
		environment.logDebug("FooBar "+predLength+" "+predIndex+"\n"+result);
		result.add("subj", things.get(pointer));
		result.add("pred", preds.get(predIndex));
		environment.logDebug("FooBar-1 "+predLength+" "+predIndex+"\n"+result);
		if (predLength ==1 || predIndex == predLength-1)
			result.add("obj", things.get(pointer+2));
		else {
			environment.logDebug("FooBar-2 "+predLength+" "+predIndex+"\n"+result);
			if (predIndex >= predLength-1)
				return result;
			JsonObject jo = makeTriple(index+1, predIndex+1, things,preds);
			result.add("obj", jo);
		}
		environment.logDebug("FooBar+\n"+result);

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
