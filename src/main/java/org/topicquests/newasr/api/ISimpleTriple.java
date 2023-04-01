/**
 * 
 */
package org.topicquests.newasr.api;

/**
 * @author jackpark
 *
 */
public interface ISimpleTriple extends IAddressable {
	public static final String
		WORDGRAM_TYPE		= "wgt",
		TRIPLE_TYPE			= "tplt";
	
	void setSubjectId(long id);
	long getSubjectId();
	void setSubjectType(String type);
	String getSubjectType9()
;	
	long setPredicateId(long id);
	long getPredicateId();
	
;	void setObjectId(long id);
	long getObjectId();
	void setObjectType(String type);
	String getObjectType();

}
