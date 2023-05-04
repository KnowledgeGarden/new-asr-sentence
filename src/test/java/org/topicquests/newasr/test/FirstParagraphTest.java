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
	C="According to the latest definition of the World Health Organization, probiotics are active microbes that stimulate the growth of other probiotic bacteria in the gut and possess beneficial health effects to the host [1]. These microorganisms are able to produce anti-carcinogenic, antioxidant and anti-mutagenic agents and induce protection against different bacterial diseases including diarrhea and respiratory tract infections. Saccharomyces cerevisiae var. boulardii is the most significant probiotic yeast species. S. cerevisiae var. boulardii is a eukaryotic organism that has been used in scientific investigations since the time of its discovery [2]. This model organism has unique importance because of its alterable and flexible genome. The genome of S. cerevisiae var. boulardii was completely sequenced in 1950 and a genome size of approximately 11.3 Mb was reported. It has approximately 6000 genes and 275 additional tRNA genes. Almost 23% of the S. cerevisiae var. boulardii’s genome is homologous to the hominid genome. This specific yeast is best known for its role in treating gastrointestinal diseases [3,4]. ",
	D ="The oval to round cell shape of S. cerevisiae var. boulardii is composed of approx. 3 µm thickness and 2.5–10.5 µm length. This yeast is able to reproduce sexually and asexually by budding and unification [8]. The cell wall of S. cerevisiae var. boulardii is composed of a rigid inner polysaccharide layer with a 1,3-β-glucan branched structure while the outer layer is made up of mannoproteins. The total mass of S. cerevisiae var. boulardii in terms of dry weight is almost 30% and the estimated total polysaccharide and protein contents are 85% and 15%, respectively. Biochemical characterization of S. cerevisiae var. boulardii confirmed the presence of glucose, mannose and N-acetylglucosamine up to 90%, 20% and 2%, respectively. Glucose to glucose interaction is associated with β-1,3 and β-1,6 linkages. β-1,3 glucan is responsible for the elasticity and strength of the yeast cell wall. The lateral cell wall of S. cerevisiae var. boulardii is composed of straight chitin chains of 1–2% of total dry weight [9]. ",
	E = "The role of microbiota in health and diseases is being highlighted by numerous studies since its discovery. Depending on the localized regions, microbiota can be classified into gut, oral, respiratory, and skin microbiota. The microbial communities are in symbiosis with the host, contributing to homeostasis and regulating immune function. However, microbiota dysbiosis can lead to dysregulation of bodily functions and diseases including cardiovascular diseases (CVDs), cancers, respiratory diseases, etc. In this review, we discuss the current knowledge of how microbiota links to host health or pathogenesis. We first summarize the research of microbiota in healthy conditions, including the gut-brain axis, colonization resistance and immune modulation. Then, we highlight the pathogenesis of microbiota dysbiosis in disease development and progression, primarily associated with dysregulation of community composition, modulation of host immune response, and induction of chronic inflammation. Finally, we introduce the clinical approaches that utilize microbiota for disease treatment, such as microbiota modulation and fecal microbial transplantation.",
	F = "The origin of “microbiota” can be dated back to early 1900s. It was found that a vast number of microorganisms, including bacteria, yeasts, and viruses, coexist in various sites of the human body (gut, skin, lung, oral cavity).1 In addition, the human microbiota, also known as “the hidden organ,” contribute over 150 times more genetic information than that of the entire human genome.2 Although “microbiota” and “microbiome” are often interchangeable, there are certain differences between the two terms. Microbiota describes the living microorganisms found in a defined environment, such as oral and gut microbiota. Microbiome refers to the collection of genomes from all the microorganisms in the environment, which includes not only the community of the microorganisms, but also the microbial structural elements, metabolites, and the environmental conditions.3 In this regard, microbiome encompasses a broader spectrum than that of microbiota. In the current review, we mainly focus on the function of microbiota in human health and diseases.",
	G="";
	
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
		new ASRParagraph();
		p.setDocumentId(DOC_ID);
		p.setText(C);
		paragraphEngine.addParagraph(p.getData());
		new ASRParagraph();
		p.setDocumentId(DOC_ID);
		p.setText(D);
		paragraphEngine.addParagraph(p.getData());
		new ASRParagraph();
		p.setDocumentId(DOC_ID);
		p.setText(E);
		paragraphEngine.addParagraph(p.getData());
		new ASRParagraph();
		p.setDocumentId(DOC_ID);
		p.setText(F);
		paragraphEngine.addParagraph(p.getData());
		
		//environment.shutDown();
		//System.exit(0);
	}

}
