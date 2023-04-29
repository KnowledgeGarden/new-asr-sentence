/**
 * 
 */
package org.topicquests.newasr.para;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.ie.util.RelationTriple;

import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
/**
 * @author jackpark
 *
 */
public class ParagraphHandler {
	private ASREnvironment environment;
	private StanfordCoreNLP pipeline;
	public static final String
		CHAIN_ID_KEY		= "chainId",
		CARGO_KEY			= "cargo",
		CHAIN_KEY			= "chain",
		SENTENCE_ID_KEY		= "sentence";
	/**
	 * 
	 */
	public ParagraphHandler(ASREnvironment env) {
		environment =env;
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,pos,lemma,ner,parse,coref, kbp");
	    props.setProperty("coref.algorithm", "neural");
	    pipeline = new StanfordCoreNLP(props, false); // false because of some errors
	}

	public IResult findCoreferences(String paragraph) {
		IResult result = new ResultPojo();
		Annotation document = new Annotation(paragraph);
		pipeline.annotate(document);
		JsonArray corefs = new JsonArray();
		result.setResultObject(corefs);
		// these take the form
		// CHAIN11-["a eukaryotic organism that has been used in scientific investigations since the time of its discovery [ 2 ]" in sentence 2,
		//          "This model organism" in sentence 3]
		JsonObject jo, jc;
		// coreferences
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			//jo = new JsonObject();
			environment.logDebug("CCCC "+cc);
			jc =parseChain(cc.toString());
			corefs.add(jc);
		}
		//mentions
		JsonArray mentions = new JsonArray();
		result.setResultObjectA(mentions);
		JsonArray ms;
		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
		      ms = new JsonArray();
		      mentions.add(ms);
		      for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
		        ms.add(m.toString());
		      }
		}
		return result;
	}
	
	// CHAIN11-["a eukaryotic organism that has been used in scientific investigations since the time of its discovery [ 2 ]" in sentence 2,
	//          "This model organism" in sentence 3]
	JsonObject parseChain(String chain) {
		environment.logDebug("PARSING "+chain);
		JsonObject result = new JsonObject();
		JsonObject jo = new JsonObject();
		int where = chain.indexOf("-");
		String chainId = chain.substring(0, where);
		result.addProperty(CHAIN_ID_KEY, chainId);
		JsonArray cargo = new JsonArray();
		result.add(CARGO_KEY, cargo);
		where = where+1;
		String array = chain.substring(where);
		String [] chunks = array.split(",");
		String x,ch, sent;
		int len = chunks.length;
		int ptx;
		// coreferences
		for (int i=0;i<len;i++) {
			jo = new JsonObject();
			x = chunks[i].trim();
			ptx = x.indexOf("in sentence");
			ch = x.substring(0, ptx).trim();
			if (ch.startsWith("["))
				ch = ch.substring(1);
			ptx += "in sentence".length();
			sent = x.substring(ptx).trim();
			jo.addProperty(CHAIN_KEY, ch);
			jo.addProperty(SENTENCE_ID_KEY, sent);
			cargo.add(jo);
		}
		return result;
	}
}
