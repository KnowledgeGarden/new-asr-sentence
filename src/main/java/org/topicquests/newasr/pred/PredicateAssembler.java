/**
 * 
 */
package org.topicquests.newasr.pred;


import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class PredicateAssembler {
	private ASREnvironment environment;
	private IAsrModel model;

	private final int _ANTECENDS = 0, _PREDICATES = 1;
	private final String
		START_FIELD		= "strt",
		END_FIELD		= "enx",
		TEXT_FIELD		= "txt";
	/**
	 * 
	 */
	public PredicateAssembler(ASREnvironment e) {
		environment = e;
		model = environment.getModel();
	}

	/******************************
		Sentence: Scientists believe that climate change is caused by  carbon dioxide
		Predicates:
			[
				[{
					'strt': 5,
					'enx': 6,
					'txt': 'is'
				}],
				[{
					'strt': 1,
					'enx': 2,
					'txt': 'believe'
				}, {
					'strt': 6,
					'enx': 7,
					'txt': 'caused'
				}, {
					'strt': 6,
					'enx': 8,
					'txt': 'caused by'
				}]
			]
		Sentence: Scientists believe  that co2 causes climate change
		[
			[],
			[{
				'strt': 1,
				'enx': 2,
				'txt': 'believe'
			}, {
				'strt': 5,
				'enx': 6,
				'txt': 'causes'
			}]
		]
		Sentence: Greenhouse gasses have been thought to cause climate change
		[
			[{
				'strt': 2,
				'enx': 6,
				'txt': 'have been thought to'
			}],
			[{
				'strt': 6,
				'enx': 7,
				'txt': 'cause'
			}]
		]
		Sentence: Climate change has been thought to have been caused by greenhouse gasses
		[
			[{
				'strt': 2,
				'enx': 6,
				'txt': 'has been thought to'
			}, {
				'strt': 2,
				'enx': 8,
				'txt': 'has been thought to have been'
			}],
			[{
				'strt': 8,
				'enx': 9,
				'txt': 'caused'
			}, {
				'strt': 8,
				'enx': 10,
				'txt': 'caused by'
			}]
]
	 ******************************/ 

	/*
	 * 
	 * @param sentence
	 * @param predicates
	 * @return
	 */
	public IResult processSentencePredicates(ISentence sentence, JsonArray predicates) {
		IResult result = new ResultPojo();
		JsonArray antecedents = predicates.get(_ANTECENDS).getAsJsonArray();
		System.out.println("ANTS: "+antecedents);
		JsonArray preds = predicates.get(_PREDICATES).getAsJsonArray();
		System.out.println("PREDS: "+preds);
		int predCount = countPredicates(preds);
		if (predCount == 1) 
			processOnePredicate(sentence, antecedents,preds, result);
		else
			processSeveralPredicates(sentence, antecedents, preds, predCount, result);
		return result;
	}
	
	void processOnePredicate(ISentence sentence, JsonArray ants, JsonArray predicate, IResult result) {
		System.out.println("ProcessOne "+predicate);
		JsonArray ja=new JsonArray();
		String sent = sentence.getText();
		JsonObject jo = _processOnePredicate(ants,predicate, sent);
		ja.add(jo);
		sentence.setPredicatePhrases(ja);
	}
	
	/**
	 * 
	 * @param ants can be {@code null}
	 * @param predicate
	 * @param theSentence TODO
	 * @return
	 */
	JsonObject _processOnePredicate(JsonArray ants, JsonArray predicate, String theSentence) {
		environment.logError("ProcessOne "+theSentence,null);
		int plen = predicate.size();
		JsonObject je, jx =null;
		String thePred = "";
		String temp;
		for (int i=0;i<plen;i++) {
			je = predicate.get(i).getAsJsonObject();
			temp =je.get(TEXT_FIELD).getAsString();
			//System.out.println("FOO: "+temp.length()+" "+temp);
			if (temp.length() > thePred.length()) {
				thePred = temp;
				jx = je;
			}
			//System.out.println("BAR: "+temp);

		}
		String theAnt = "";
		if (ants != null) {
			int alen = ants.size();

			for (int i=0;i<alen;i++) {
				je = ants.get(i).getAsJsonObject();
				temp =je.get(TEXT_FIELD).getAsString();
				if (temp.length() > theAnt.length()) {
					theAnt = temp;
					jx = je;
				}
			}
		}
		String predPhrase = theAnt+" "+thePred.trim();
		boolean valid = this.checkPredicate(predPhrase, theSentence);
		if (!valid)
			environment.logError("NotValidPred "+predPhrase+"\n"+theSentence, null);
		JsonObject jo = new JsonObject();
		jo.add("strt", jx.get("strt"));
		jo.addProperty("txt", predPhrase);
		System.out.println("DID: "+jo);
		environment.logError("DID: "+valid+" "+jo, null);
		return jo;
	}
	
	void processSeveralPredicates(ISentence sentence, JsonArray ants, JsonArray predicates, int predicateCount, IResult result) {
		System.out.println("ProcessSeveral "+predicateCount+" "+predicates);
		String sent = sentence.getText();
		// Results go here
		// They are to be JsonObjects with start location and predicate phrase txt
		JsonArray results = new JsonArray();
		sentence.setPredicatePhrases(results);
		JsonObject workingObject;
		JsonArray antCluster = new JsonArray();
		JsonArray predCluster = new JsonArray();
		JsonArray tempCluster = null;
		JsonObject je;
		int startField= 0;
		int temp;
		// Predicates first
		for (int i=0;i<predicates.size();i++) {
			je = predicates.get(i).getAsJsonObject();
			temp = je.get(START_FIELD).getAsJsonPrimitive().getAsInt();
			//System.out.println("GOT: "+je);
			if (i == 0)  {
				tempCluster = new JsonArray();
				tempCluster.add(je);
				startField = temp;
			} else if (temp == startField) {
				tempCluster.add(je);
			} else {
				predCluster.add(tempCluster);
				tempCluster = new JsonArray();
				tempCluster.add(je);
				startField = temp;
			}
		}
		if (!predCluster.contains(tempCluster))
			predCluster.add(tempCluster);
		System.out.println("PREDCLUSTER\n"+predCluster);
		//[[{"strt":1,"enx":2,"txt":"believe"}],[{"str[[{"strt":1,"enx":2,"txt":"believe"}],[{"strt":6,"enx":7,"txt":"caused"},{"strt":6,"enx":8,"txt":"caused by"}]]

		
		int antCount = countAntecedents(ants);
		// wonder if they are the same - not always
		// If count == 1, then we must figure out which pred it belongs to
		// Otherwise, we must pair ants against preds, e.g. an ant's end position is just before its pred
		System.out.println("Ants-Preds "+antCount+" "+predicateCount);
		for (int i=0;i<ants.size();i++) {
			je = ants.get(i).getAsJsonObject();
			temp = je.get(START_FIELD).getAsJsonPrimitive().getAsInt();
			if (i == 0)  {
				tempCluster = new JsonArray();
				tempCluster.add(je);
				startField = temp;
			} else if (temp == startField) {
				tempCluster.add(je);
			} else {
				antCluster.add(tempCluster);
				tempCluster = new JsonArray();
				tempCluster.add(je);
				startField = temp;
			}
		}
		if (!antCluster.contains(tempCluster))
			antCluster.add(tempCluster);
		System.out.println("ANTCLUSTER\n"+antCluster);
		//[[{"strt":5,"enx":6,"txt":"is"}]]
		
		// For every predicate cluster, find its antecent cluster - if any
		int len = predCluster.size();
		int where =-1;
		JsonArray tx, pMatch =null;
		for (int i = 0; i< len; i++) {
			tx = predCluster.get(i).getAsJsonArray();
			je = tx.get(0).getAsJsonObject();
			where = je.get("strt").getAsJsonPrimitive().getAsInt();
			where = matchAntToPreds(where, antCluster);
			if (where > -1) {
				pMatch = antCluster.get(where).getAsJsonArray();
				if (pMatch != null) {
					je = this._processOnePredicate(pMatch, tx, sent);
					results.add(je);
					//System.out.println("PREDMATCH-1\n"+je);
				}
			} else {
				je = this._processOnePredicate(null, tx, sent);
				results.add(je);
				//System.out.println("PREDMATCH-2\n"+je);
			} 
			//System.out.println("PREDMATCH\n"+je+"\n"+pMatch);
		}
		//System.out.println("PS\n"+antCluster+"\n"+predCluster);
	}
	
	boolean checkPredicate(String predicate, String sentence) {
		return sentence.contains(predicate);
	}
	
	int matchAntToPreds(int antEnd, JsonArray predClusters) {
		System.out.println("MATCHING "+antEnd+" "+predClusters);
		JsonArray result = null;
		JsonArray temp;
		JsonObject px;
		int where=-1;
		for (int i=0;i<predClusters.size(); i++) {
			temp = predClusters.get(i).getAsJsonArray();
			px = temp.get(0).getAsJsonObject();
			where = px.get("enx").getAsJsonPrimitive().getAsInt();
			if (antEnd == where ) {
				return i;
			}
		}
		return -1;
	}
	
	
	int countPredicates(JsonArray preds) {
		int result = 0;
		int len = preds.size();
		JsonObject je;
		int startField= -1;
		int temp;
		for (int i=0;i<len;i++) {
			je = preds.get(i).getAsJsonObject();
			temp = je.get(START_FIELD).getAsJsonPrimitive().getAsInt();
			if (temp > startField) {
				//if (startField > 0)
					result++;
				startField = temp;
			}
		}
		System.out.println("CountingP: "+result+" "+preds);
		return result;
	}
	int countAntecedents(JsonArray preds) {
		int result = 0;
		int len = preds.size();
		JsonObject je;
		int startField= 1;
		int temp;
		for (int i=0;i<len;i++) {
			je = preds.get(i).getAsJsonObject();
			temp = je.get(START_FIELD).getAsJsonPrimitive().getAsInt();
			if (temp > startField) {
				//if (startField > 0)
					result++;
				startField = temp;
			}
		}
		System.out.println("CountingA: "+result+" "+preds);
		return result;
	}

}
