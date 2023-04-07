/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.pred.PredicateAssembler;
import org.topicquests.newasr.spacy.SpacyHttpClient;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class SecondPredicateTest extends TestingRoot {
	private PredicateAssembler handler;
	private SpacyHttpClient spacy;

	private final String
		S0  = "Carbon dioxide causes climate change.",
		S1	="Greenhouse gasses have been thought to cause climate change",
		S2  ="Scientists believe that climate change is caused by carbon dioxide",
		S3	="Climate change has been thought to have been caused by greenhouse gasses",
		S4	="Scientists have been thought to believe that climate change is caused by  carbon dioxide";
	/**
	 * 
	 */
	public SecondPredicateTest() {
		super();
		spacy = new SpacyHttpClient(environment);
		IResult r = spacy.processSentence(S2);
		System.out.println("A "+r.getErrorString());
		System.out.println("B\n"+r.getResultObject());
		
		environment.shutDown();
		System.exit(0);

	}

}
/*
 * simple rule
 * S0 [[6114188574318813489, 0, 1], [6114188574318813489, 1, 2], [6114188574318813489, 3, 4], [6114188574318813489, 4, 5]
 *            Carbon					dioxide						climate						climate
 * S1 [[6114188574318813489, 0, 1], [6114188574318813489, 1, 2], [6114188574318813489, 7, 8], [6114188574318813489, 8, 9]]
 * 			Greenhouse					gasses								climate				change
 * longer rule
 * S0 [[6114188574318813489, 0, 1], [6114188574318813489, 1, 2], [6114188574318813489, 3, 4], [6114188574318813489, 4, 5]]}
 *			Carbon						dioxide						climate						change
 *S2 [[0, 1], 		[3, 4], 	[4, 5], 	[9, 10], [10, 11]]
 *     scientits    climate		change
A 
B
{"data":[[{"strt":2,"enx":6,"txt":"have been thought to"}],[{"strt":6,"enx":7,"txt":"cause"}]],"dbp":[["climate change","http://dbpedia.org/resource/Global_warming","0.9999890286064247"]],"wkd":[],"trips":[]}
 
 */
