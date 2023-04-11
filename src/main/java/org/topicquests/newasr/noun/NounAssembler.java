/**
 * 
 */
package org.topicquests.newasr.noun;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.ISentence;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

	public IResult bigDamnAnalyze(ISentence sentence) {
		IResult result = new ResultPojo();;
		
		return result;
	}
		
}
