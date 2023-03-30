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
public interface ILexTypes {
	public static final String
		NOUN				= "n",
		INFERRED_NOUN		= "in",
		NOUN_PHRASE			= "np",
		INFERRED_NOUNPHRASE	= "inp",
		PROPER_NOUN			= "npn",
		GERUND				= "ng",
		DETERMINER			= "det",
		VERB				= "v",
		INFERRED_VERB		= "iv",
		VERB_PHRASE			= "vp",
		INFERRED_VERBPRASE	= "ivp",
		TUPLE_TYPE			= "tup",
		ADJECTIVE			= "adj", //note: it's an "a" for wordnet/framenet
		ADVERB				= "adv",
		ADVERBIAL_PHRASE	= "advp",
		PREPOSITION			= "prep",
		PRONOUN				= "pro",
		PUNCTUATION			= "punct",
		CONJUNCTION			= "cnj",
		C_CONJUNCTION		= "ccnj",
		CONJUNCTIVE_ADVERB 	= "cadvp",
		R_CONJUNCTION		= "corj",
		QUESTION_WORD		= "qw",
		STOP_WORD			= "sw",
		NUMBER				= "num",
		PERCENT_NUMBER		= "pnum",
		DATE				= "date",
		EMAIL				= "email",
		IP_ADDRESS			= "ipA",
		TIME				= "time",
		HREF				= "href",
		GEO_LOC				= "geoL",
		META_TYPE			= "MTA";
}
