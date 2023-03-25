/**
 * 
 */
package org.topicquests.newasr.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public interface ISentence extends IAddressable {
	public static final String
		TEXT_FIELD		= "txt",
		PRED_FIELD		= "preds",
		SPACY_FiELD		= "spacy",	//JSON blob from spacy POS
		WD_FIELD		= "wd",		//Wikidata identities
		DBP_FIELD		= "dbp"; 	//DBpedia blobs

	JsonObject getData();
	
	void setText(String text);
	String getText();
	
	void setPredicatePhrases(JsonArray preds);
	
	/**
	 * Might return {@code null}
	 * @return
	 */
	JsonArray getPredicatePhrases();
	
	void setSpacyData(String spacyJson);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	String getSpacyData();
	
	void addWikidataId(String wikidata);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray getWikiData();
	
	void addDBpediaData(String dbpJson);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray getDBpediaData();
}
