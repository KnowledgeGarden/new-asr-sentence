/**
 * 
 */
package org.topicquests.newasr.wg;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.util.JsonUtil;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class WordGramBuilder {
	private ASREnvironment environment;
	private IAsrModel model;
	private JsonUtil util;

	/**
	 * 
	 */
	public WordGramBuilder(ASREnvironment env) {
		environment =env;
		model = environment.getModel();
		util = new JsonUtil();
	}

	/**
	 * Build the WordGram graph for the given {@code sentence}
	 * @param sentence
	 * @return
	 */
	public IResult processSentence(ISentence sentence) {
		IResult result = new ResultPojo();
		
		return result;
	}
}
