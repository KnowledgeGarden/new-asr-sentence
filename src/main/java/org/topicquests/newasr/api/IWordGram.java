/**
 * 
 */
package org.topicquests.newasr.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
/**
 * @author jackpark
 *
 */
public interface IWordGram extends IAddressable {
	public static final String
		ID_KEY			= "id",
		LOX_KEY			= "lox", 	// locators
		IN_KEY			= "inLinks", // {sentenceId, gramId}
		OUT_KEY			= "outLinks", // {sentenceId, gramId}
		DBPED_KEY		= "dbp",	// dbpedia
		WIKID_KEY		= "wikd",	// wikidata
		TENSE_KEY		= "tense",  // predicate tense,e.g.past,present,
		NEGATION_KEY	= "neg",	// boolean
		EPI_KEY		= 	"epi",		// epistemic status,e.g. speculative,can be null
		POS_KEY			= "pos",	// part of speech
		WORDS_KEY		= "words",	// the text for this gram
		INVERSE_KEY		= "inverse",// inverse predicate - only for passive predicates
		CANNON_KEY		= "cannon",	// canonical NER
		SYNONYM_KEY		= "synon",	// synonyms
		ANTONYM_KEY		= "anton",	// antonyms
		EXTENSION_KEY	= "extns";	// for extension properties	
	
	
	JsonObject getData();
	
	String getWords();
	
	void setWords(String words);
	
	JsonArray listInLinks();
	JsonArray listOutLinks();
	
	void addInLink(long sentenceId, long gramId);
	void addOutlink(long sentenceId, long gramId);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray listTopicLocators();
	
	void addTopicLosator(String locator);
	
	void setTopicLocators(JsonArray locators);
	
	void setNegation(boolean isNeg);
	boolean getNegation();
	
	/**
	 * Can return {@code null}
	 * @return - list of JSON objects
	 */
	String getDBpedia();
	
	void setDBpedia(String dbPediaJson);
	
	/**
	 * Can return {@code null}
	 * @return list of wikidata identifiers
	 */
	String getWikidata();
	
	void setWikidata(String wikidataId);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray listPOS();
	
	void addPOS(String pos);
	
	void setPOS(JsonArray pos);
	
	void setInverseTerm(long inverseTermId);
	/**
	 * Can return {@code -1} if no inverse termexists
	 * @return
	 */
	long getInverseTerm();
	
	/**
	 * Shortcut
	 * @return
	 */
	boolean hasInverseTerm();
	
	void setCannonTerm(long cannonTermId);
	/**
	 * Can return {@code -1} if no cannonical term exists
	 * @return
	 */
	long getCannonTerm();
	
	/**
	 * Shortcut
	 * @return
	 */
	boolean hasCannonicalTerm();
	
	void addSynonymTerm(long synonymTermId);
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray listSynonyms();
	
	void addAntonymTerm(long antonymTermId);
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonArray listAntonyms();
	
	/**
	 * Predicate tense,e.g.past
	 * @param tense
	 */
	void setTense(String tense);
	/**
	 * Can return {@code null}
	 * @return
	 */
	String getTense();
	/**
	 * Epistemic status e.g. speculative
	 * @param status
	 */
	void setEpistemicStatus(String status);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	String getEpistemicStatus();
	
	/**
	 * <p>An <em>extension property</em> is one we haven't thought of yet.</p>
	 * <p>To enable processing, the {@code key} should be distinctive, such as, e.g. {@code _myKey}</p>
	 * @param key
	 * @param value
	 */
	void addExtensionProperty(String key, String value);
	//void addExtensionProperty(String key, JsonObject value);
	//void addExtensionProperty(String key, JsonArray value);

	/**
	 * A {@link JsonElement} can be cast to many objects such as String, long, JsonArray, etc
	 * @param key
	 * @return
	 */
	JsonElement getExtensionProperty(String key);
	
	/**
	 * Can return {@code null}
	 * @return
	 */
	JsonObject getExtensionPropeties();
}
