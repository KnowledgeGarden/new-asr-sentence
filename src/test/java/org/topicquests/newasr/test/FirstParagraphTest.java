/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.IParagraph;
import org.topicquests.newasr.impl.ASRParagraph;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class FirstParagraphTest extends TestingRoot {
	private final String
	B = "Saccharomyces cerevisiae var. boulardii is the most significant probiotic yeast species. S. cerevisiae var. boulardii is a eukaryotic organism that has been used in scientific investigations since the time of its discovery [2]. This model organism has unique importance because of its alterable and flexible genome.",
	C="According to the latest definition of the World Health Organization, probiotics are active microbes that stimulate the growth of other probiotic bacteria in the gut and possess beneficial health effects to the host [1]. These microorganisms are able to produce anti-carcinogenic, antioxidant and anti-mutagenic agents and induce protection against different bacterial diseases including diarrhea and respiratory tract infections. Saccharomyces cerevisiae var. boulardii is the most significant probiotic yeast species. S. cerevisiae var. boulardii is a eukaryotic organism that has been used in scientific investigations since the time of its discovery [2]. This model organism has unique importance because of its alterable and flexible genome. The genome of S. cerevisiae var. boulardii was completely sequenced in 1950 and a genome size of approximately 11.3 Mb was reported. It has approximately 6000 genes and 275 additional tRNA genes. Almost 23% of the S. cerevisiae var. boulardii’s genome is homologous to the hominid genome. This specific yeast is best known for its role in treating gastrointestinal diseases [3,4]. ";
	private final long DOC_ID = 37;
	/**
	 * 
	 */
	public FirstParagraphTest() {
		super();
		IParagraph p = new ASRParagraph();
		p.setDocumentId(DOC_ID);
		p.setText(B);
		paragraphEngine.addParagraph(p.getData());
		//IResult r = paraHandler.findCoreferences(C);
		//System.out.println("A\n"+r.getResultObject());
		//System.out.println("B\n"+r.getResultObjectA());

		//environment.shutDown();
		//System.exit(0);
	}

}
