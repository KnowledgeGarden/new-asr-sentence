/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.ITuple;
import org.topicquests.newasr.api.ITupleDataProvicer;
import org.topicquests.newasr.api.ITupleModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class ASRTupleModel implements ITupleModel {
	private ASREnvironment environment;
	private ITupleDataProvicer database;
	/**
	 * 
	 */
	public ASRTupleModel(ASREnvironment env) {
		environment = env;
		database = new PostgresTupleDatabase(environment);
	}

	@Override
	public IResult putTuple(ITuple tup) {
		return database.putTuple(tup);
	}

	@Override
	public IResult getTupleById(long id) {
		return database.getTupleById(id);
	}

	@Override
	public IResult getTupleByPSI(String psi) {
		return database.getTupleByPSI(psi);
	}

	@Override
	public IResult addSentenceIdToTuple(long sentenceId, long tupleId) {
		return database.addSentenceIdToTuple(sentenceId, tupleId);
	}

	@Override
	public IResult getTupleBySubjectTypeAndId(String type, long id) {
		return database.getTupleBySubjectTypeAndId(type, id);
	}

	@Override
	public IResult listTuples(int start, int count) {
		return database.listTuples(start,count);
	}

	@Override
	public IResult putWorkingTuple(ITuple tup) {
		return database.putWorkingTuple(tup);
	}

	@Override
	public IResult getWorkingTupleById(long id) {
		return database.getWorkingTupleById(id);
	}

	@Override
	public IResult getWorkingTupleByPSI(String psi) {
		return database.getWorkingTupleByPSI(psi);
	}

}
