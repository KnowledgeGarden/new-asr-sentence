/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.impl.ASRSentence;

/**
 * @author jackpark
 *
 */
public class FirstSentenceTest extends TestingRoot {
	private final String
		S1	="Greenhouse gasses have been thought to cause climate change",
		S4	="Scientists have been thought to believe that climate change is caused by  carbon dioxide";

	private ISentence sentence;

	/**
	 * 
	 */
	public FirstSentenceTest() {
		super();
		
		sentence = new ASRSentence(); 
		sentence.setText(S4);
		sentence.setId(System.currentTimeMillis());
		sentenceEngine.acceptNewSentence(sentence.getData());
		
		//environment.shutDown(); // cannot shut down due to thread
		//System.exit(0);
	}

}
