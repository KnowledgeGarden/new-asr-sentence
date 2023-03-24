/**
 * 
 */
package org.topicquests.newasr.test;

/**
 * @author jackpark
 *
 */
public class BootTest extends TestingRoot {

	/**
	 * 
	 */
	public BootTest() {
		super();
		System.out.println("A "+environment.getProperties());
		environment.shutDown();
		System.exit(0);
	}

}
