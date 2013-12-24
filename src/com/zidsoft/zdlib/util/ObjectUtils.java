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

package com.zidsoft.zdlib.util;

/**
 * Object utility class. Modeled after org.apache.commons.lang3.ObjectUtils 
 * 
 * @author faridz
 *
 */
public class ObjectUtils {

	private ObjectUtils() {}
	

	/**
	 * Determine whether two objects are equals. Either object may be null.
	 * @param object1
	 * @param object2
	 * @return true if the two objects are equal.
	 */
	public static boolean equals(Object object1, Object object2) {
		if (object1 == null || object2 == null) {
			return object1 == object2;
		}
		return object1.equals(object2);
	}
	
	/**
	 * Compares two objects for inequality, where either one or both objects may be null.
	 * @param object1
	 * @param object2
	 * @return true if two objects are not equals
	 */
	public static boolean notEqual(Object object1, Object object2) {
		return !equals(object1, object2);
	}

	
	/**
	 * Null safe comparison
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static <T extends Comparable<? super T>> int compare(T c1, T c2) { 
		if (c1 == null || c2 == null) {
			return c1 == null && c2 == null ? 0 :
				c1 == null ? -1 : 1;
		}
		return c1.compareTo(c2);
	}
}

