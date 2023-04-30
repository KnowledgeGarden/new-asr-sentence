/**
 * 
 */
package org.topicquests.newasr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class BulletinBoard {
	private ASREnvironment environment;
	private JsonArray _mentions= null;
	private JsonArray _corefs = null;
	/**
	 * 
	 */
	public BulletinBoard(ASREnvironment env) {
		environment =env;
	}
	///////////////
	// Mentions
	// [
	//	["Saccharomyces cerevisiae var. boulardii", "the most significant probiotic yeast species"],
	//	["2", "S. cerevisiae var. boulardii", "a eukaryotic organism that has been used in scientific investigations since the time of its discovery [ 2 ]", "scientific investigations", "the time of its discovery", "its discovery", "its"],
	//	["This model organism", "its alterable and flexible genome", "its"]
	// ]
	/////////////////
	public void setMentions(JsonArray mentions) {
		_mentions = mentions;
	}
	
	/**
	 * Can return {@code null}
	 */
	public JsonArray getMentions() {
		return _mentions;
	}
	
	/**
	 * can return {@code null}
	 * @param which
	 * @return
	 */
	public JsonArray getMentionForSentence(int which) {
		if (_mentions != null) 
			return _mentions.get(which).getAsJsonArray();
		return null;
	}
	////////////////
	// Coreference chains
	//[{
	//	"chainId": "CHAIN8",
	//	"cargo": [{
	//		"chain": "\"Saccharomyces cerevisiae var. boulardii\"",
	//		"sentence": "1"
	//	}, {
	//		"chain": "\"S. cerevisiae var. boulardii\"",
	//		"sentence": "2"
	//	}, {
	//		"chain": "\"its\"",
	//		"sentence": "2]"
	//	}]
	// }, {
	//	"chainId": "CHAIN11",
	//	"cargo": [{
	//		"chain": "\"a eukaryotic organism that has been used in scientific investigations since the time of its discovery [ 2 ]\"",
	//		"sentence": "2"
	// }, {
	//		"chain": "\"This model organism\"",
	//		"sentence": "3"
	// }, {
	//		"chain": "\"its\"",
	//		"sentence": "3]"
	//	}]
	// }]
	////////////////////
	public void setCoreferenceChains(JsonArray corefs) {
		_corefs = corefs;
	}
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	public JsonArray getCoreferenceChains() {
		return _corefs;
	}
	/**
	 * can return {@code null} or an empty {@link JsonArray}
	 * @param which
	 * @return
	 */
	public JsonArray getCoreferenceChainsForSentence(int which) {
		if (_corefs != null) {
			JsonArray result = new JsonArray();
			int len = _corefs.size();
			JsonObject jo;
			for (int i=0;i<len;i++) {
				jo = _corefs.get(i).getAsJsonObject();
				if (hasRef(jo, which))
					result.add(jo);
			}
			return result;
		}
		return null;
	}
	
	boolean hasRef(JsonObject jo, int which) {
		JsonArray cargo = jo.get("cargo").getAsJsonArray();
		JsonObject jx;
		int len = cargo.size();
		int sentenceId;
		for (int i=0;i<len;i++) {
			jx = cargo.get(i).getAsJsonObject();
			sentenceId = jx.get("sentence").getAsJsonPrimitive().getAsInt();
			if (sentenceId == which)
				return true;
		}
		return false;
	}
	
}
