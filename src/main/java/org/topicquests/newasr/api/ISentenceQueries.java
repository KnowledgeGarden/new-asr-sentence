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
		"iNSERT INTO public.sentence",
	GET_SENTENCE =
		"SELECT * FROM public.sentence where id=?",
	UPDATE_SENTENCE =
		"UPDATE";

}
