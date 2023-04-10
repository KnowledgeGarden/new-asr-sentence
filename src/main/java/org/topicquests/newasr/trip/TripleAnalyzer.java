/**
 * 
 */
package org.topicquests.newasr.trip;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
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
	private ITripleModel tripleModel;
	/**
	 * 
	 */
	public TripleAnalyzer(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		tripleModel = environment.getTripleModel();
		// TODO Auto-generated constructor stub
	}
	
	public JsonObject bigDamnAnalyze(JsonArray predicates, JsonArray nouns) {
		environment.logDebug("BigDamAnalysis\n"+predicates+"\n"+nouns);
		JsonObject result = new JsonObject();
		int counts = 20;
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
			where = jo.get("strt").getAsJsonPrimitive().getAsInt();
			construct[where] = jo;
		}
		//populate with nouns
		int nounLength = nouns.size();
		
		for (int i=0;i<nounLength;i++) {
			jo = nouns.get(i).getAsJsonObject();
			where = jo.get("strt").getAsJsonPrimitive().getAsInt();
			construct[where] = jo;
		}
		for (int i=0;i<counts;i++) {
			if (construct[i] != null)
				things.add(construct[i]);
		}
		//if (predicates.size() == 1)
		environment.logDebug("BigDamAnalysis+\n"+things);
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
