/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.bootstrap.PredicateImporter;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class PredicateImportTest extends TestingRoot {
	private PredicateImporter predImporter;

	/**
	 * 
	 */
	public PredicateImportTest() {
		super();

		predImporter = environment.getPredicateImporter();
		IResult r = predImporter.bootPredicates();
		environment.logDebug("PredicateImportTest done "+r.getErrorString());
		System.out.println("A "+r.getErrorString());
		
		environment.shutDown();
		System.exit(0);
	}

}
