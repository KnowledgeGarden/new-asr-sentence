/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.ITuple;
import org.topicquests.newasr.api.ITupleModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class ASRTupleModel implements ITupleModel {
	private ASREnvironment environment;

	/**
	 * 
	 */
	public ASRTupleModel(ASREnvironment env) {
		environment = env;
	}

	@Override
	public IResult putTuple(ITuple tup) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult getTupleById(long id) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult getTupleByPSI(String psi) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult addSentenceIdToTuple(long sentenceId, long tupleId) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult getTupleBySubjectTypeAndId(String type, long id) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IResult listTuples(int start, int count) {
		IResult result = new ResultPojo();
		// TODO Auto-generated method stub
		return result;
	}

}
