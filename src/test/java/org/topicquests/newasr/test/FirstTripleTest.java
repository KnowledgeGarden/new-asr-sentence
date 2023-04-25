/**
 * 
 */
package org.topicquests.newasr.test;

import org.topicquests.newasr.api.ISimpleTriple;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.newasr.impl.ASRSimpleTriple;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class FirstTripleTest extends TestingRoot {

	/**
	 * 
	 */
	public FirstTripleTest() {
		super();
		ISimpleTriple foo = _makeTriple(34, "Something", 99, "cause", 99, "Something else");
		IResult r = tripleModel.putTuple(foo);
		System.out.println("A "+r.getErrorString());
		System.out.println("B "+r.getResultObject());
		if(r.getResultObject() != null) {
			long fooId = ((Long)r.getResultObject()).longValue();
			r = tripleModel.getTupleById(fooId);
			System.out.println("C "+r.getErrorString());
			System.out.println("D "+r.getResultObject());
			if(r.getResultObject() != null) {
				ISimpleTriple bar = (ISimpleTriple)r.getResultObject();
				System.out.println("E\n"+bar.getData());
			}
		}
		r =tripleModel.getThisTuple(foo);
		System.out.println("F "+r.getErrorString());
		System.out.println("G "+r.getResultObject());
		if(r.getResultObject() != null) {
			ISimpleTriple bar = (ISimpleTriple)r.getResultObject();
			System.out.println("H\n"+bar.getData());
		}
		
		environment.shutDown();
		System.exit(0);
	}

	ISimpleTriple _makeTriple(long sId, String stx, long pId, String ptx,  long oId, String otx) {
		ISimpleTriple result = new ASRSimpleTriple();
		result.setSubjectId(sId, ISimpleTriple.WORDGRAM_TYPE);
		result.setSubjectText(stx);
		result.setPredicateId(pId);
		result.setPredicateText(ptx);
		result.setObjectId(oId, ISimpleTriple.WORDGRAM_TYPE);
		result.setObjectText(otx);
		return result;
	}
/**
E
{
	"norm": -1,
	"id": 6,
	"subj": 34,
	"subjT": "wgt",
	"subjTX": "Something",
	"pred": 99,
	"predTX": "cause",
	"obj": 99,
	"objT": "wgt",
	"OBJTX": "Something else"
}
H
{
	"norm": -1,
	"subj": 34,
	"subjT": "wgt",
	"subjTX": "Something",
	"pred": 99,
	"predTX": "cause",
	"obj": 99,
	"objT": "wgt",
	"OBJTX": "Something else",
	"id": 1
}
 */
 
}
