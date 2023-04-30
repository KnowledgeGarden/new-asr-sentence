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
public interface IParagraphQueries {

	public static final String
		PUT_PARAGRAPH =
			"iNSERT INTO public.paragraph (doc_id, data) VALUES(?, ?) RETURNING id",
		GET_PARAGRAPH =
			"SELECT * FROM public.paragraph WHERE id=?",
		UPDATE_PARAGRAPH =
			"UPDATE public.paragraph SET data=? WHERE id=?";
		
}
