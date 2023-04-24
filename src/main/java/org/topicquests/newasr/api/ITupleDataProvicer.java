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

	
	IResult putTuple(ISimpleTriple tup);
	
	IResult getTupleById(long id);
	
	IResult getThisTuple(ISimpleTriple template);

	IResult putWorkingTuple(ISimpleTriple tup);
	//IResult getWorkingTupleById(long id);
	IResult getThisWorkingTuple(ISimpleTriple template);

	
	IResult addSentenceIdToTuple(long sentenceId, long tupleId);
	
	//IResult getTupleBySubjectTypeAndId(String type, long id);
	
	IResult listTuples(int start, int count);

}
