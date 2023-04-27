/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.ISentence;
import org.topicquests.newasr.impl.ASRSentence;

/**
 * @author jackpark
 *
 */
public class FirstSentenceTest extends TestingRoot {
	private final String
		SX  = "Carbon dioxide is thought to cause climate change",
		SXX = "Carbon dioxide was thought to cause climate change",
		S0  = "Carbon dioxide has been thought to cause climate change",
		S1	="Greenhouse gasses have been thought to cause climate change",
		S2  = "Christian Drosten works in Germany.",
		S4	="Scientists have been thought to believe that climate change is caused by carbon dioxide",
		S4A ="Scientists believe that climate change is caused by carbon dioxide",
		S5  ="Climate change is not caused by elephants",
		S6  = "Greenhouse gasses cause climate change",
		S7  = "Greenhouse gas causes climate change",
		S8  = "Some scientists believe that other scientists believe that carbon dioxide causes climate change",
		S9  = "Elephant shit encourages flies",
		S10 = "The pandemic of obesity, type 2 diabetes mellitus (T2DM) and nonalcoholic fatty liver disease (NAFLD) has frequently been associated with dietary intake of saturated fats (1) and specifically with dietary palm oil (PO) (2).";


	private ISentence sentence;

	/**
	 * 
	 */
	public FirstSentenceTest() {
		super();
		
		sentence = new ASRSentence(); 
		sentence.setText(S4A);
		sentence.setId(System.currentTimeMillis());
		sentenceEngine.acceptNewSentence(sentence.getData());
		
		//environment.shutDown(); // cannot shut down due to thread
		//System.exit(0);
	}
	
	
}
/**
S1
{
	"data": [
		[{
			"strt": 2,
			"enx": 6,
			"txt": "have been thought to"
		}],
		[{
			"strt": 6,
			"enx": 7,
			"txt": "cause"
		}]
	],
	"dbp": [
		["climate change", "http://dbpedia.org/resource/Global_warming", "0.9999890286064247"]
	],
	"wkd": []
}
S2
{
	"data": [
		[],
		[]
	],
	"dbp": [
		["Christian", "http://dbpedia.org/resource/Christianity", "0.8546387600731989"],
		["Drosten", "http://dbpedia.org/resource/Landdrost", "0.9999991796410609"],
		["Germany", "http://dbpedia.org/resource/Germany", "0.9929449861218795"]
	],
	"wkd": [
		["Germany", "Q1206012", "LOC", ["country in Central Europe"]]
	]
} 
S4
{
	"data": [
		[{
			"strt": 1,
			"enx": 5,
			"txt": "have been thought to"
		}, {
			"strt": 9,
			"enx": 10,
			"txt": "is"
		}],
		[{
			"strt": 5,
			"enx": 6,
			"txt": "believe"
		}, {
			"strt": 10,
			"enx": 11,
			"txt": "caused"
		}, {
			"strt": 10,
			"enx": 12,
			"txt": "caused by"
		}]
	],
	"dbp": [
		["climate change", "http://dbpedia.org/resource/Global_warming", "0.9999928412462271"],
		["carbon dioxide", "http://dbpedia.org/resource/Carbon_dioxide", "0.9993317763086826"]
	],
	"wkd": []
}
 */ 
