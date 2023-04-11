/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.impl;

import org.topicquests.newasr.api.ISimpleTriple;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jackpark
 *
 */
public class ASRSimpleTriple implements ISimpleTriple {
	private JsonObject data;
	/**
	 * 
	 */
	public ASRSimpleTriple() {
		data = new JsonObject();
		this.setNormalizedTripleId(-1); // set default
	}
	
	public ASRSimpleTriple(JsonObject d) {
		data = d;
	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSubject(Object subj, String type) {
		if (type == null)
			throw new RuntimeException("Missing Subject Type");
		// TODO Auto-generated method stub

	}

	@Override
	public Object getSubject() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String getSubjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long setPredicateId(long id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getPredicateId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setObject(Object obj, String type) {
		if (type == null)
			throw new RuntimeException("Missing Object Type");
		// TODO Auto-generated method stub

	}

	@Override
	public Object getObject() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String getObjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSentenceId(long sentenceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonArray listSentenceIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPSI(String psi) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPSI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNormalizedTripleId(long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNormalizedTripleId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
