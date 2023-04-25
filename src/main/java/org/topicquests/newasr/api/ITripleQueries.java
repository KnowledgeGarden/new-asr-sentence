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
			"INSERT INTO public.triple (subj_id, pred_id, obj_id, subj_typ, obj_typ, subj_txt, pred_txt, obj_txt) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
			
		PUT_WORKING_TRIPLE = // we don't bother with text objects in working triples
			"INSERT INTO public.working_triple (subj_id, pred_id, obj_id, subj_typ, obj_typ, norm_id) "+
			"VALUES (?, ?, ?, ?, ?, ?,) RETURNING id",
			
		PUT_SENTENCE_ID =
			"INSERT INTO public.sentenceids (id, sentence_id) VALUES (?, ?)",
		
		GET_TRIPLE =
			//"SELECT t.id, t.subj_id, t.pred_id, t.obj_id, t.subj_typ, t.obj_typ, t.subj_txt, t.pred_txt, t.obj_txt, s.sentence_id "+
			"SELECT public.triple.*, public.sentenceIds.sentence_id "+
			"FROM public.triple  "+
			"JOIN public.sentenceIds  ON (public.triple.id = public.sentenceIds.id) WHERE public.triple.id=? ",
		
		NEW_GET_TRIPLE =
			"SELECT * from public.triple WHERE id=?",
		GET_THIS_TRIPLE =
			"SELECT * FROM public.triple WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?",

		GET_TRIPLE_SENTENCES =
			"SELECT * FROM public.sentenceids WHERE id=?",
			
		GET_WORKING_TRIPLE =
			"SELECT * FROM public.working_triple WHERE id=?",
			
		LIST_TRIPLES = // does not join sentence ids
			"SELECT * FROM public.triple LIMIT=? OFFSET=?",
		
		GET_THIS_WORKING_TRIPLE =
			"SELECT * FROM public.working_triple WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?",

		UPDATE_WORKING_TRIPLE =
			"UPDATE public.working_triple SET norm_id=? WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?";


}
