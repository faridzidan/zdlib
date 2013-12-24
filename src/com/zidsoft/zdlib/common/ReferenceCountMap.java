/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Zidsoft LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 
package com.zidsoft.zdlib.common;

import java.util.WeakHashMap;

/**
 * Weak reference count map. Counts the references to an object without holding
 * a hard reference to the object. When the last reference to the object is
 * removed, the object's map entry is deleted.
 * 
 * @author faridz
 *
 * @param <K>
 */
public abstract class ReferenceCountMap<K> extends WeakHashMap<K, Integer> {
	
	public ReferenceCountMap() {
		new WeakHashMap<K, Integer>();
	}
	/**
	 * This method is called the first time a reference is added for an object.
	 * @param key
	 */
	protected abstract void onInitialAdd(final K key);
	
	/**
	 * Add another reference to the object. If this is the first reference being
	 * added, {@link #onInitialAdd(Object)} is called.
	 * @param key
	 */
	public void addReference(final K key) {
    	boolean initialReg;
    	synchronized(this) {
        	Integer count = get(key);
        	initialReg = count == null;
			// hey there. Java Integer is immutable
       		put(key, initialReg ? 1 : ++count);
    	}
    	if (initialReg) {
    		onInitialAdd(key);
    	}
    }

	/**
	 * Remove reference to the object. If the removed reference is the last
	 * reference to the object, the object's map entry is deleted.
	 * @param key
	 */
	public void removeReference(final K key) {
    	synchronized(this) {
    		Integer count = get(key);
    		if (count != null) {
    			--count;
    			if (count.intValue() == 0) {
    				remove(key);
    			} else {
    				put(key, count);
    			}
    		}
    	}		
	}
}
