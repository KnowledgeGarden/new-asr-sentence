/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr;

/**
 * @author jackpark
 *
 */
public class Main {
	private ASREnvironment environment;
	/**
	 * 
	 */
	public Main() {
		environment = new ASREnvironment();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}

}
