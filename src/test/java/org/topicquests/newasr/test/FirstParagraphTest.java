/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class FirstParagraphTest extends TestingRoot {
	private final String
	B = "Saccharomyces cerevisiae var. boulardii is the most significant probiotic yeast species. S. cerevisiae var. boulardii is a eukaryotic organism that has been used in scientific investigations since the time of its discovery [2]. This model organism has unique importance because of its alterable and flexible genome.",
	C="";
	/**
	 * 
	 */
	public FirstParagraphTest() {
		super();
		
		IResult r = paraHandler.findCoreferences(B);
		System.out.println("A\n"+r.getResultObject());
		System.out.println("B\n"+r.getResultObjectA());

		environment.shutDown();
		System.exit(0);
	}

}
