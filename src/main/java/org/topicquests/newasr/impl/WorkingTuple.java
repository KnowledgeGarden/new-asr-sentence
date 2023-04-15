/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.api.IWorkiingTuple;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class WorkingTuple extends Tuple implements IWorkiingTuple {
	/**
	 * 
	 */
	public WorkingTuple() {
		data = new JsonObject();
	}
	
	public WorkingTuple(JsonObject d) {
		data = d;
	}



	@Override
	public IResult processData() {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}


}
