/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.impl.ASRSentence;
import org.topicquests.newasr.pred.PredicateAssembler;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author jackpark
 *
 */
public class FirstPredicateTest extends TestingRoot {
	private PredicateAssembler handler;
	private final String
		S1	="Greenhouse gasses have been thought to cause climate change",
		S2  ="Scientists believe that climate change is caused by  carbon dioxide",
		S3	="Climate change has been thought to have been caused by greenhouse gasses",
		S4	="Scientists have been thought to believe that climate change is caused by  carbon dioxide",
		ONE_PRED	= "[[{'strt': 2, 'enx': 6, 'txt': 'have been thought to'}], [{'strt': 6, 'enx': 7, 'txt': 'cause'}]]",
		TWO_PRED	= "[[{'strt': 5, 'enx': 6, 'txt': 'is'}], [{'strt': 1, 'enx': 2, 'txt': 'believe'}, {'strt': 6, 'enx': 7, 'txt': 'caused'}, {'strt': 6, 'enx': 8, 'txt': 'caused by'}]]",
		THREE_PRED	= "[[{'strt': 2, 'enx': 6, 'txt': 'has been thought to'}, {'strt': 2, 'enx': 8, 'txt': 'has been thought to have been'}], [{'strt': 8, 'enx': 9, 'txt': 'caused'}, {'strt': 8, 'enx': 10, 'txt': 'caused by'}]]",
		FOUR_PRED		= "[[{'strt': 1, 'enx': 5, 'txt': 'have been thought to'}, {'strt': 9, 'enx': 10, 'txt': 'is'}], [{'strt': 5, 'enx': 6, 'txt': 'believe'}, {'strt': 10, 'enx': 11, 'txt': 'caused'}, {'strt': 10, 'enx': 12, 'txt': 'caused by'}]]";
	private ISentence sentence;
	/**
	 * 
	 */
	public FirstPredicateTest() {
		super();
		handler = new PredicateAssembler(environment);
		
		sentence = new ASRSentence(); 
		sentence.setText(S1);
		JsonArray ja = (JsonArray)JsonParser.parseString(ONE_PRED);
		IResult r = handler.processSentencePredicates(sentence, ja);
		System.out.println("SENTENCE:\n"+sentence.getText()+"\n"+sentence.getPredicatePhrases());
		sentence = new ASRSentence(); 
		sentence.setText(S2);
		ja = (JsonArray)JsonParser.parseString(TWO_PRED);
		r = handler.processSentencePredicates(sentence, ja);
		System.out.println("SENTENCE:\n"+sentence.getText()+"\n"+sentence.getPredicatePhrases());
		sentence = new ASRSentence(); 
		sentence.setText(S3);
		ja = (JsonArray)JsonParser.parseString(THREE_PRED);
		r = handler.processSentencePredicates(sentence, ja);
		System.out.println("SENTENCE:\n"+sentence.getText()+"\n"+sentence.getPredicatePhrases());
		sentence = new ASRSentence(); 
		sentence.setText(S4);
		ja = (JsonArray)JsonParser.parseString(FOUR_PRED);
		r = handler.processSentencePredicates(sentence, ja);
		System.out.println("SENTENCE:\n"+sentence.getText()+"\n"+sentence.getPredicatePhrases());
		environment.shutDown();
		System.exit(0);
	}
/*
Counting: 1 [{"strt":6,"enx":7,"txt":"cause"}]
ProcessOne [[{"strt":2,"enx":6,"txt":"have been thought to"}],[{"strt":6,"enx":7,"txt":"cause"}]]
Counting: 2 [{"strt":1,"enx":2,"txt":"believe"},{"strt":6,"enx":7,"txt":"caused"},{"strt":6,"enx":8,"txt":"caused by"}]
ProcessSeveral [[{"strt":5,"enx":6,"txt":"is"}],[{"strt":1,"enx":2,"txt":"believe"},{"strt":6,"enx":7,"txt":"caused"},{"strt":6,"enx":8,"txt":"caused by"}]]
Counting: 1 [{"strt":8,"enx":9,"txt":"caused"},{"strt":8,"enx":10,"txt":"caused by"}]
ProcessOne [[{"strt":2,"enx":6,"txt":"has been thought to"},{"strt":2,"enx":8,"txt":"has been thought to have been"}],[{"strt":8,"enx":9,"txt":"caused"},{"strt":8,"enx":10,"txt":"caused by"}]]

 */
}
