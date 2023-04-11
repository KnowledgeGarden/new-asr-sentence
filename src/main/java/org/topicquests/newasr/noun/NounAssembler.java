/**
 * 
 */
package org.topicquests.newasr.noun;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;

/**
 * @author jackpark
 *
 */
public class NounAssembler {
	private ASREnvironment environment;
	private IAsrModel model;

	/**
	 * 
	 */
	public NounAssembler(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		// TODO Auto-generated constructor stub
	}

}
