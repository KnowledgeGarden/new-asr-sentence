/**
 * 
 */
package org.topicquests.newasr.test;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class WordgramTest extends TestingRoot {

	/**
	 * 
	 */
	public WordgramTest() {
		super();
		IResult r = model.getTermById("39");
		System.out.println("A "+r.getErrorString());
		System.out.println("B "+r.getResultObject());
		
		r = model.getThisTermById("39");
		System.out.println("C "+r.getErrorString());
		System.out.println("D "+r.getResultObject());
		
		environment.shutDown();
		System.exit(0);
	}

}
