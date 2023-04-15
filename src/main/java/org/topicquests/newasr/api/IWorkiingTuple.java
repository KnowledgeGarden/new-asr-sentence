/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.api;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public interface IWorkiingTuple extends ITuple {

	/**
	 * <p>A Working tuple may not be a final ITuple
	 * because it may have terms which are not canonical
	 * or it may have passive predicates which need processing</p>
	 * @return
	 */
	IResult processData();

}
