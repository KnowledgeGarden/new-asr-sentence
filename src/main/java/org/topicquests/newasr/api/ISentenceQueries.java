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
public interface ISentenceQueries {

	public static final String
	PUT_SENTENCE =
		"iNSERT INTO public.sentence (para_id, doc_id, data) VALUES(?, ?, ?) RETURNING id",
	GET_SENTENCE =
		"SELECT * FROM public.sentence WHERE id=?",
	UPDATE_SENTENCE =
		"UPDATE public.sentence SET data=? WHERE id=?";

}
