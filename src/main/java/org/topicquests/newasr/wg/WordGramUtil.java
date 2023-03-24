/**
 * 
 */
package org.topicquests.newasr.wg;

import java.util.ArrayList;
import java.util.List;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IDictionary;
import org.topicquests.newasr.api.ILexTypes;
import org.topicquests.support.api.IResult;

import com.google.common.base.Splitter;

/**
 * @author jackpark
 *
 */
public class WordGramUtil {
	private ASREnvironment environment;
	private IDictionary dictionary;
	private IAsrModel model;

	private  String 
		commaId = null,
		colonId = null,
		semicolonId = null,
		periodId = null,
		exclaimId = null,
		questionId = null,
		leftParenId = null,
		rightParenId = null,
		leftCurlyId = null,
		rightCurlyId = null,
		leftBrackId = null,
		rightBrackId = null,
		leftCarrotId = null,
		rightCarrotId = null,
		quoteId = null,
		tickId = null;
	/**
	 * 
	 */
	public WordGramUtil(ASREnvironment e) {
		environment = e;
		dictionary = environment.getDictionary();
		model = environment.getModel();
		bootPunctuation();
		System.out.println("PUNCT "+commaId);
	}

	/**
	 * Core sentence spliting on spaces between words
	 * @param sentence
	 * @return
	 */
	public List<String> splitSentence(String sentence) {
		List<String> result = new ArrayList<String>();
		Iterable<String> ix = Splitter.on(' ')
			       .trimResults()
			       .omitEmptyStrings()
			       .split(sentence);
		for (String w: ix)
			result.add(w);
		result = pluckPunctuation(result);
		return result;
	}
	
	/**
	 * Words which begin or end with punctuation are split into two cells
	 * @param sentence
	 * @return
	 */
	List<String> pluckPunctuation(List<String> sentence) {
		List<String> result = new ArrayList<String>();
		String theWord, w;

		boolean endsWithComma = false;
		boolean endsWithQuestionMark = false;
		boolean endsWithColon = false;
		boolean endsWithSemicolon = false;
		boolean endsWithPeriod = false;
		boolean endsWithExclaim = false;
		boolean endsWithTick = false;
		boolean endsWithQuote = false;
		boolean endsWithParen = false;
		boolean endsWithBrack = false;
		boolean endsWithCarrot = false;
		boolean endsWithCurly = false;
		boolean startsWithTick = false;
		boolean startsWithQuote = false;
		boolean startsWithParen = false;
		boolean startsWithBrack = false;
		boolean startsWithCarrot = false;
		boolean startsWithCurly = false;
		int len = sentence.size();
		for (int i=0;i<len;i++) {
			w = sentence.get(i);
			if (w != "") {
				endsWithComma = endsWithComma(w);
				endsWithQuestionMark = endsWithQuestionMark(w);
				endsWithColon = endsWithColon(w);
				endsWithSemicolon = endsWithSemicolon(w);
				endsWithTick = endsWithTick(w);
				startsWithTick = startsWithTick(w);
				endsWithPeriod = endsWithPeriod(w);
				endsWithParen = endsWithParen(w);
				startsWithParen = startsWithParen(w);
				endsWithBrack = endsWithBrack(w);
				startsWithBrack = startsWithBrack(w);
				endsWithCurly = endsWithCurly(w);
				startsWithCurly = startsWithCurly(w);
				endsWithCarrot = endsWithCarrot(w);
				startsWithCarrot = startsWithCarrot(w);
				endsWithCarrot = endsWithCarrot(w);
				startsWithCarrot = startsWithCarrot(w);
				//Deal with leading special characters
				if (startsWithTick) {
					result.add("'");
				}
				else if (startsWithQuote) {
					result.add("\"");
				}
				else if (startsWithParen) {
					result.add("(");
				}
				else if (startsWithBrack) {
					result.add("[");
				}
				else if (startsWithCurly) {
					result.add("{");
				}
				else if (startsWithCarrot) {
					result.add(w);
				}
				theWord = cleanWord(w);
				// the word itself
				result.add(theWord);
				//Deal with trailing characters
				if (endsWithComma) {
					result.add(",");
				} else if (endsWithColon) {
					result.add(":");
				} else if (endsWithSemicolon) {
					result.add(";");
				} else if (endsWithQuestionMark) {
					result.add("?");
				} else if (endsWithPeriod) {
					result.add(".");
				} else if (endsWithExclaim) {
					result.add("!");
				} else if (endsWithTick) {
					result.add("'");
				} else if (endsWithQuote) {
					result.add("\"");
				} else if (endsWithParen) {
					result.add(")");
				} else if (endsWithBrack) {
					result.add(w);
				} else if (endsWithCurly) {
					result.add("]");
				} else if (endsWithCarrot) {
					result.add("}");
				}
			}
		}

		return result;
	}
	
		
		///////////////////////////
		// utilities
		///////////////////////////
		boolean endsWithComma(String w) {
			return w.trim().endsWith(",");
		}
		boolean endsWithQuestionMark(String w) {
			return w.trim().endsWith("?");
		}

		boolean endsWithColon(String w) {
			return w.trim().endsWith(":");
		}
		boolean endsWithSemicolon(String w) {
			return w.trim().endsWith(";");
		}
		
		boolean endsWithPeriod(String w) {
			return w.trim().endsWith(".");
		}

		boolean endsWithExclaim(String w) {
			return w.trim().endsWith("!");
		}
		
		boolean endsWithCurly(String w) {
			return w.trim().endsWith("}");
		}
		
		boolean endsWithBrack(String w) {
			return w.trim().endsWith("]");
		}
		
		boolean endsWithParen(String w) {
			return w.trim().endsWith(")");
		}
		
		boolean endsWithCarrot(String w) {
			return w.trim().endsWith(">");
		}
		boolean endsWithQuote(String w) {
			return w.trim().endsWith("\"");
		}
		boolean endsWithTick(String w) {
			return w.trim().endsWith("'");
		}

		boolean startsWithPeriod(String w) {
			return w.trim().startsWith(".");
		}
		boolean startsWithCarrot(String w) {
			return w.trim().startsWith("<");
		}
		boolean startsWithParen(String w) {
			return w.trim().startsWith("(");
		}
		boolean startsWithBrack(String w) {
			return w.trim().startsWith("[");
		}
		boolean startsWithQuote(String w) {
			return w.trim().startsWith("\"");
		}
		boolean startsWithTick(String w) {
			return w.trim().startsWith("'");
		}
		boolean startsWithCurly(String w) {
			return w.trim().startsWith("{");
		}
		
	/**
	 * Strip of leading and trailing special characters
	 * @param w
	 * @return
	 */
	String cleanWord(String w) {
		String result = w.trim();
		if (result.endsWith(",") ||
			result.endsWith(".") ||
			result.endsWith(";") ||
			result.endsWith("?") ||
			result.endsWith("\"") ||
			result.endsWith("'") ||
			result.endsWith("}") ||
			result.endsWith("]") ||
			result.endsWith(">") ||
			result.endsWith(")") ||
			result.endsWith("!")) {
			result = result.substring(0, (result.length()-1));
		}
		if (result.startsWith("\"") ||
			result.startsWith("<") ||
			result.startsWith("{") ||
			result.startsWith("(") ||
			result.startsWith("[")) {
			result = result.substring(1);
		}
		return result;
	}
	/**
	 * Called locally
	 */
	void bootPunctuation() {
		quoteId = getPunctuationId("\"", ILexTypes.STOP_WORD); //needs another lextype for what it signifes to quote
		commaId = getPunctuationId(",", ILexTypes.C_CONJUNCTION); // also a stopword
		colonId = getPunctuationId(":", ILexTypes.C_CONJUNCTION); // also a stopword
		semicolonId = getPunctuationId(";", ILexTypes.C_CONJUNCTION); //also a stopword
		periodId = getPunctuationId(".", ILexTypes.STOP_WORD); //needs another lextype
		exclaimId = getPunctuationId("!", ILexTypes.STOP_WORD); //needs another lextype
		questionId = getPunctuationId("?", ILexTypes.QUESTION_WORD);//also a stopword
		leftParenId = getPunctuationId("(", ILexTypes.STOP_WORD);//needs another lextype
		rightParenId = getPunctuationId(")", ILexTypes.STOP_WORD);//needs another lextype
		leftCurlyId = getPunctuationId("{", ILexTypes.STOP_WORD); //needs another lextype
		rightCurlyId = getPunctuationId("}", ILexTypes.STOP_WORD); //needs another lextype
		leftBrackId = getPunctuationId("[", ILexTypes.STOP_WORD);//needs another lextype
		rightBrackId = getPunctuationId("]", ILexTypes.STOP_WORD);//needs another lextype
		leftCarrotId = getPunctuationId("<", ILexTypes.STOP_WORD);//needs another lextype
		rightCarrotId = getPunctuationId(">", ILexTypes.STOP_WORD);//needs another lextype
		tickId = getPunctuationId("'", ILexTypes.STOP_WORD);//needs another lextype
	}
	
	/**
	 * 
	 * @param punct
	 * @param lexType  same as POS
	 * @return
	 */
	String getPunctuationId(String punct, String lexType) {
		System.out.println("ProcesPunct: "+punct);
		IResult r = model.processTerm(punct, lexType);
		return (String)r.getResultObject();
	}
}
