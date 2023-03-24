/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.impl.WordGram;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class SecondNodeTest extends TestingRoot {
	private final String
		TERM		= "My favorite phrase revisited again more",
		SENTENCE_ID	= "101",
		IN_ID		= "999",
		OUT_ID		= "12898",
		TOPIC_LOX	= "23323";
	
	/**
	 * 
	 */
	public SecondNodeTest() {
		super();
		
		//IWordGram wg = new WordGram();
		IResult r = model.newDictionaryEntry(TERM);
		String id = (String)r.getResultObject();
		System.out.println("A "+id);
		IWordGram g = new WordGram();
		g.setId(Long.valueOf(id));
		g.addTopicLosator(TOPIC_LOX);
		g.setWords(TERM);
		r = model.putWordGram(g);
		System.out.println("B "+g.getData());
		System.out.println("C "+r.getErrorString());
		System.out.println("D "+r.getResultObject());
		r = model.getTermById(id);
		System.out.println("E "+r.getResultObject());
		g = (IWordGram)r.getResultObject();
		if (g != null)
			System.out.println("F "+g.getData());

		environment.shutDown();
		System.exit(0);


	}

}
