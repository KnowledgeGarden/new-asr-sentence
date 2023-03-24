/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.newasr.wg;

import java.util.*;

import org.topicquests.newasr.ASREnvironment;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IWordGram;
import org.topicquests.support.api.IResult;



/**
 * @author park
 * <p>The ASR platform works from this WordGramCache in order to reduce
 * roundtrips to the graph database. That's because words being read are frequently 
 * read again; keeping their WordGrams in memory is the purpose of this cache</p>
 * <p>It is worth noting that concurrent reading implementations may provoke
 * version collisions; there are workarounds for that, which include the use
 * of aggregators which perform merges prior to visiting this cache.</p>
 * <p>Save an in-memory instance of an {@link IWordGram} and
 * watch for <em>Optimistic Lock Exceptions</em> when newer versions
 * of the same object are added.</p>
 * <p>Anytime an object is flushed from the cache, it is saved to the database</p>
 * <p>NOTE: it is a requirement that, when a reading session has been ended, this
 * cache <em>must be flushed</em> with <code>flushAll</code> in order to ensure
 * that all unsaved WordGrams are saved.</p>
 */
public class WordGramCache {
	private ASREnvironment environment;
	private IAsrModel model;


	private Map<String,IWordGram> cache;
	private int MAX_SIZE = 100;

	/**
	 * @param env
	 * @param maxSize
	 */
	public WordGramCache(ASREnvironment env, IAsrModel m, int maxSize) {
		environment = env;
		model = m;
		MAX_SIZE = maxSize;
	    cache = new LinkedHashMap<String,IWordGram>(maxSize+1, .75F, true) {
	    	private static final long serialVersionUID = 1;
	    		//@override
	    		protected boolean removeEldestEntry(Map.Entry eldest) {
	    			boolean result = size() > MAX_SIZE;
	    			if (result) {
	    				//remove eldest
	    				IWordGram x = (IWordGram)eldest.getValue();
	    				this.remove(x.getId());
	    				environment.logDebug("WordGramGraph.stashing "+x.getId());
	    			}
	    			return result;
	    		}
	    	};
	}
		
	/**
	 * Saves every entry to the database
	 */
	public void flushAll() {
		//nothing to do
	}
	
	/**
	 * <p>Add <code>value</code> to the cache.</p>
	 * <p>Will text <em>versions</em> if there is an entry for <code>key</code><br/>
	 *  Will return <code>false</code> if <code>value</code> is successfully added.
	 *  Otherwise, will return <code>true</code> and <em>not</em> store <code>value</code>
	 *  if the version of <code>value</code> is not at least greater than that of the
	 *  existing {@link IWordGram}</p>
	 *  <p>Don't do versions anymore</p>
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, IWordGram value) {
		synchronized(cache) {
			cache.put(key, value);
		    return true;
		}
	}

	/**
	 * Returns an {@link IWordGram}. If the object has an <em>RedirectIdProperty</em>
	 * it will return the object identified by that.
	 * @param key
	 * @return can return <code>null</code>
	 */
	public IWordGram get(String key) {
		synchronized(cache) {
			IWordGram result = cache.get(key);
			if (result == null) {
				IResult r = model.getThisTermById(key);
				result = (IWordGram)r.getResultObject();
				if (result != null) {
					environment.logDebug("WordGramCache.get "+key+" "+result);
					long redirect = result.getCannonTerm();
					if (redirect != -1) {
						return getThis(redirect);
					}
					cache.put(key, result);
				}
			}
		    return result;
		}
	}
	
	/**
	 * Returns the specific {@link IWordGram} identified by <code>key</code>
	 * @param key
	 * @return can return <code>null</code>
	 */
	public IWordGram getThis(long key) {
		environment.logDebug("WordGramCache.getThis- "+key);
		String id = Long.valueOf(key).toString();
		synchronized(cache) {
			IWordGram result = cache.get(key);
			environment.logDebug("WordGramCache.getThis-1 "+key+" "+result);
			if (result == null) {
				IResult r = model.getTermById(id);
				result = (IWordGram)r.getResultObject();
				if (result != null)
					cache.put(id, result);
			}
			environment.logDebug("WordGramCache.getThis+ "+key+" "+result);
		    return result;
		}
	}

	public void remove(String key, boolean doSave) {
		synchronized(cache) {
			if (cache.containsKey(key)) {
		        cache.remove(key);
		    }
		}
	}

	/**
	 * This clear does not save anything.
	 */
	public void clear() {
	  synchronized(cache) {
		  cache.clear();
	  }
	}
	public int size() {
	    synchronized(cache) {
	      return cache.size();
	    }
	}
}
