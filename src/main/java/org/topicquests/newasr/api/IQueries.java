/**
 * 
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
	/*	GET_NODE =
			"SELECT public.words.id, public.words.words, public.pos.pospos, public.topicid.locator, "+
			"public.dbpedia.dbp, public.wikidata.wkd, public.active.active, public.cannon.cannon "+
			"public.synonym.synonyn, public.antonym.antonym, "+
			"public.properties._key, public.properties._val, "+
			"public.inlinks.isentenceId, public.inlinks.itargetId, public.outlinks.osentenceId public.outlinks.otargetId "+
			"FROM public.words WHERE public.words.id=? "+
			"JOIN public.pos ON public.pos.id = public.words.id"+
			"JOIN public.topicid ON public.topicid.id = public.words.id "+
			"JOIN public.dbpedia ON public.dbpedia.id = public.words.id "+
			"JOIN public.wikidata ON public.wikidata.id = public.words.id "+
			"JOIN public.active ON public.active.id = public.words.id "+
			"JOIN public.cannon ON public.cannon.id = public.words.id "+
			"JOIN public.synonym ON public.synonym.id = public.words.id "+
			"JOIN public.antonym ON public.antonym.id = public.words.id "+
			"JOIN public.properties ON public.properties.id = public.words.id "+
			"JOIN public.inlinks ON public.inlinks.id = public.words.id "+
			"JOIN public.outlinks ON public.outlinks.id = public.words.id ",
	*/
		
			
		/*PUT_WORDS =	
			"INSERT INTO public.words (id, words) VALUES (?, ?)",
		PUT_POS =	
			"INSERT INTO public.pos (id, pos) VALUES (?, ?)",
		PUT_LOCATOR =	
			"INSERT INTO public.topicid (id, locator) VALUES (?, ?)",
		PUT_DBPEDIA =	
			"INSERT INTO public.dbpedia (id, dbp) VALUES (?, ?)",
		PUT_WIKIDATA =	
			"INSERT INTO public.wikidata (id, wkd) VALUES (?, ?)",
		PUT_ACTIVE =	
			"INSERT INTO public.active (id, active) VALUES (?, ?)",
		PUT_CANNON =	
			"INSERT INTO public.cannon (id, cannon) VALUES (?, ?)",
		PUT_SYNONYM =	
			"INSERT INTO public.synonym (id, synonym) VALUES (?, ?)",
		PUT_ANTONYM =	
			"INSERT INTO public.antonym (id, antonym) VALUES (?, ?)",*/
	
	
		GET_NODE =
			"SELECT * FROM public.node where id=?",
		GET_INLINKS =
			"SELECT * FROM public.inlinks WHERE id=?",
		GET_OUTLINKS =
			"SELECT * FROM public.outlinks WHERE id=?",
 
		PUT_NODE =
			"INSERT INTO public.node (id, words, pos, topicid, dbpedia, wikidata, tense, negation, epi, active, cannon) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
		PUT_INlINK =	
			"INSERT INTO public.inlinks (id, isentenceId, itargetId) VALUES (?, ?, ?)",
		PUT_OUTlINK =	
			"INSERT INTO public.outlinks (id, osentenceId, otargetId) VALUES (?, ?, ?)",

		ADD_POS =
			"UPDATE public.node SET pos = ? WHERE i?", //note array value
		ADD_LOCATOR =
			"UPDATE public.node SET topicid = ? WHERE i?", //note array value
		ADD_DBPEDIA =
			"UPDATE public.node SET dbpedia = ? WHERE i?", //note array value
		ADD_WIKIDATA =
			"UPDATE public.node SET topicid = ? WHERE i?", //note array value
		ADD_SYNONYM =
			"UPDATE public.node SET synonym = ? WHERE i?", //note array value
		ADD_ANTONYM =
			"UPDATE public.node SET antonym = ? WHERE i?", //note array value
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
