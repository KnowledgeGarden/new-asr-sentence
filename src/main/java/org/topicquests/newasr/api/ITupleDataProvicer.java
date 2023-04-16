/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public interface ITupleDataProvicer {

	
	IResult putTuple(ITuple tup);
	
	IResult getTupleById(long id);
	
	IResult getTupleByPSI(String psi);
	
	IResult putWorkingTuple(ITuple tup);
	IResult getWorkingTupleById(long id);
	IResult getWorkingTupleByPSI(String psi);

	
	IResult addSentenceIdToTuple(long sentenceId, long tupleId);
	
	IResult getTupleBySubjectTypeAndId(String type, long id);
	
	IResult listTuples(int start, int count);

}
