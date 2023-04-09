/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public interface IQueries {

	public static final String	
		
		GET_LOCATOR =
			"SELECT topiclocator FROM public.node WHERE id=?",
			
		
		GET_DBPEDIA =
			"SELECT topiclocator FROM public.node WHERE id=?",
		
		GET_WIKIDATA =
			"SELECT topiclocator FROM public.node WHERE id=?",

			
	
		GET_NODE =
			"SELECT * FROM public.node where id=?",
		GET_SENTENCE_EDGES =
			"SELECT * FROM public.sentence_edges WHERE id=?",
		PUT_SENTENCE_EDGE =	
			"INSERT INTO public.sentence_edges (id, inLink, outLink, tense, epi) VALUES (?, ?, ?, ?, ?)",
		PUT_NODE =
			"INSERT INTO public.node (id, words, pos, topicid, dbpedia, wikidata, tense, negation, epi, active, cannon) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
		ADD_POS =
			"UPDATE public.node SET pos = ? WHERE i?", //note array value
		ADD_LOCATOR =
			"UPDATE public.node SET topicid = ? WHERE i?", //note array value
		ADD_DBPEDIA =
			"UPDATE public.node SET dbpedia = ? WHERE i?", //note array value
		ADD_WIKIDATA =
			"UPDATE public.node SET wikidata = ? WHERE i?", //note array value
//		ADD_SYNONYM =
//			"UPDATE public.node SET synonym = ? WHERE i?", //note array value
//		ADD_ANTONYM =
//			"UPDATE public.node SET antonym = ? WHERE i?", //note array value
		SET_ACTIVE =
			"UPDATE public.node SET active = ? WHERE i?",
		SET_CANNON =
			"UPDATE public.node SET cannon = ? WHERE i?",

		PUT_PROPERTY =
			"INSERT INTO public.properties (id, _key, _val) VALUES (?, ?, ?)",
		GET_PROPERTIES =
			"SELECT * FROM public.properties where id=?",
		REMOVE_PROPERTY =
			"DELETE FROM public.properties where id=? _key=? _val=?"; // TODO may need AND
}
