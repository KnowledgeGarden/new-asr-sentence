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
			"iNSERT INTO public.paragraph",
		GET_PARAGRAPH =
			"SELECT * FROM public.paragraph where id=?",
		UPDATE_PARAGRAPH =
			"UPDATE";
		
}
