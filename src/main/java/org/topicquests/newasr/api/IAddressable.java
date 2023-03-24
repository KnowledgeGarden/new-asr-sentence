/**
 * 
 */
package org.topicquests.newasr.api;

/**
 * @author jackpark
 * All features use BIGINTs as database identifiers
 */
public interface IAddressable {

	void setId(long id);
	
	long getId();
}
