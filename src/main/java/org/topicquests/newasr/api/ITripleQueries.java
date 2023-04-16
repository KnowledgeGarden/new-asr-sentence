/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

/**
 * @author jackpark
 *
 */
public interface ITripleQueries {

	public static final String
	
		PUT_TRIPLE =
			"INSERT INTO public.triple (subj_id, pred_id, obj_id, subj_typ, obj_typ, psi, norm_id) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?) RETURNS id",
			
		PUT_WORKING_TRIPLE =
			"INSERT INTO public.working_triple (subj_id, pred_id, obj_id, subj_typ, obj_typ, psi, norm_id) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?) RETURNS id",
			
		PUT_SENTENCE_ID =
			"INSERT INTO public.sentenceids (id, sentence_id) VALUES (?, ?)",
			
		GET_TRIPLE =
			"SELECT * FROM public.triple WHERE id=?",
			
		GET_TRIPLE_SENTENCES =
			"SELECT * FROM public.sentenceids WHERE id=?",
			
		GET_WORKING_TRIPLE =
			"SELECT * FROM public.triple WHERE id=?";
	

}
