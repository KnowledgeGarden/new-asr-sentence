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
public class FirstNodeTest extends TestingRoot {
	public static final String
		TERM	= "Fool Bar",
		POS		= "noun";
	/**
	 * 
	 */
	public FirstNodeTest() {
		super();
		
		//IWordGram wg = new WordGram();
		IResult r = model.processTerm(TERM, POS);
		System.out.println("A "+r.getErrorString());
		System.out.println("B "+r.getResultObject());
		String id = (String)r.getResultObject();
		r = model.getTermById(id);
		System.out.println("C "+r.getResultObject());
		IWordGram g = (IWordGram)r.getResultObject();
		if (g != null)
			System.out.println("D "+g.getData());
		environment.logDebug("Did");

		environment.shutDown();
		System.exit(0);
	}

}
