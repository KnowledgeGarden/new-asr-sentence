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
			"INSERT INTO public.triple "+
			"(wg_subj_id, tr_subj_id, wg_pred_id, wg_obj_id, tr_obj_id) "+
			"VALUES (?, ?, ?, ?, ?) RETURNING id",
		//	"INSERT INTO public.triple (subj_id, pred_id, obj_id, subj_typ, obj_typ, subj_txt, pred_txt, obj_txt) "+
		//	"VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
			
		PUT_WORKING_TRIPLE = // we don't bother with text objects in working triples
			"INSERT INTO public.working_triple "+
			"(wg_subj_id, tr_subj_id, wg_pred_id, wg_obj_id, tr_obj_id) "+
			"VALUES (?, ?, ?, ?, ?) RETURNING id",
			//+ "(subj_id, pred_id, obj_id, subj_typ, obj_typ, norm_id) "+
			//"VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
			
		PUT_SENTENCE_ID =
			"INSERT INTO public.sentenceids (id, sentence_id) VALUES (?, ?)",
		
		GET_TRIPLE_PHASE_1 =
			"WITH RECURSIVE rec_tr (id, wg_subj_id, tr_subj_id, wg_pred_id, wg_obj_id, tr_obj_id) AS ( "
			+ "SELECT * FROM public.triple WHERE id=2 "
			+ "UNION ALL "
			+ "SELECT t.* FROM public.triple t "
			+ "JOIN rec_tr ON ((t.id = rec_tr.tr_subj_id) OR (t.id = rec_tr.tr_obj_id)) "
			+ ") "
			+ "SELECT * from rec_tr",
		
		GET_TRIPLE_FULL =
			"WITH RECURSIVE rec_tr (id, wg_subj_id, tr_subj_id, wg_pred_id, wg_obj_id, tr_obj_id) AS (\n"
			+ "SELECT * FROM TRIPLE WHERE id=?\n"
			+ "UNION ALL\n"
			+ "SELECT t.* FROM public.triple t\n"
			+ "JOIN rec_tr ON ((t.id = rec_tr.tr_subj_id) OR (t.id = rec_tr.tr_obj_id))\n"
			+ ")\n"
			+ "SELECT t.*,\n"
			+ "  sn.words AS subj_node_text,\n"
			+ "  pn.words AS pred_node_text,\n"
			+ "  obn.words AS ob_node_text \n"
			+ "FROM rec_tr t\n"
			+ "LEFT OUTER JOIN node sn ON (t.wg_subj_id = sn.id)\n"
			+ "LEFT OUTER JOIN node pn ON (t.wg_pred_id = pn.id)\n"
			+ "LEFT OUTER JOIN node obn ON (t.wg_obj_id = obn.id)",
			
		GET_TRIPLE =
			"SELECT "+
				  "t.*, "+
				  "sn.words AS subj_node_text, "+
				  "pn.words AS pred_node_text, "+
				  "obn.words AS ob_node_text  "+
				"FROM triple t  "+
				"LEFT OUTER JOIN node sn ON (t.subj_typ = 'wgt' AND t.subj_id = sn.id) "+
				"LEFT OUTER JOIN node pn ON (t.pred_id = pn.id) "+
				"LEFT OUTER JOIN node obn ON (t.obj_typ='wgt' AND t.obj_id = obn.id)",
	
			//"SELECT t.id, t.subj_id, t.pred_id, t.obj_id, t.subj_typ, t.obj_typ, t.subj_txt, t.pred_txt, t.obj_txt, s.sentence_id "+
			//"SELECT public.triple.*, public.sentenceIds.sentence_id "+
			//"FROM public.triple  "+
			//"JOIN public.sentenceIds  ON (public.triple.id = public.sentenceIds.id) WHERE public.triple.id=? ",
		
		NEW_GET_TRIPLE =
			"SELECT * from public.triple WHERE id=?",
		GET_THIS_TRIPLE =
			"SELECT * FROM public.triple WHERE "+
			"wg_subj_id=? AND tr_subj_id=? AND wg_pred_id=? AND wg_obj_id=? AND tr_obj_id=?",
			//+ "subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?",

		GET_TRIPLE_SENTENCES =
			"SELECT * FROM public.sentenceids WHERE id=?",
			
		GET_TRIPLE_SENTENCE =
			"SELECT * FROM public.sentenceids WHERE id=? AND sentence_id=?",
			
		GET_WORKING_TRIPLE =
			"SELECT * FROM public.working_triple WHERE id=?",
			
		LIST_TRIPLES = // does not join sentence ids
			"SELECT * FROM public.triple LIMIT=? OFFSET=?",
		
		GET_THIS_WORKING_TRIPLE =
			"SELECT * FROM public.working_triple WHERE "+
			"wg_subj_id=? AND tr_subj_id=? AND wg_pred_id=? AND wg_obj_id=? AND tr_obj_id=?",

			//+ "WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?",

		UPDATE_WORKING_TRIPLE =
			"UPDATE public.working_triple SET norm_id=? WHERE subj_id=? AND pred_id=? AND obj_id=? AND subj_typ=? AND obj_typ=?";


}
