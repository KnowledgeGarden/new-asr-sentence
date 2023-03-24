/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;

/**
 * @author jackpark
 *
 */
public class TestingRoot {
	protected ASREnvironment environment;
	protected IAsrModel model;

	/**
	 * 
	 */
	public TestingRoot() {
		environment = new ASREnvironment();
		model = environment.getModel();
	}

}
