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
 * Miscellaneous string utility methods.
 * 
 * @author faridz
 *
 */
public class ZDStringUtils {

	private ZDStringUtils() {}

	/**
	 * Determine whether the given string value is equivalent to true. Behavior
	 * is that to check for values equivalent to false and consider all other values
	 * true.
	 * @param val
	 * @return
	 */
	public static boolean isTrue(final String val) {
		if (val == null) {
			return false;
		} else if (
			val.compareToIgnoreCase("N")     == 0 ||
			val.compareToIgnoreCase("NO") 	 == 0 ||
			val.compareToIgnoreCase("F") 	 == 0 ||
			val.compareToIgnoreCase("FALSE") == 0 ||
			val.compareToIgnoreCase("0") 	 == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Convert string value to an equivalent Boolean value. 
	 * @param val string value
	 * @return null if val is null otherwise true or false is returned
	 */
	public static Boolean toBoolean(final String val) {
		return val == null ? null : isTrue(val);
	}
	
	public static String toYesNo(final Boolean val) {
		return val == null ? "" :
			val ? "Yes" : "No";
	}
	public static String toYesNoNegated(final Boolean val) {
		return val == null ? "" :
			!val ? "Yes" : "No";
	}
	
	/**
	 * Create a parameter list for using with jdbc. Example output:
	 * (?, ?, ?) or (?), etc
	 * @param size number of parameters
	 * @return parameter list string
	 */
	public static String createParamList(final int size) {
		StringBuilder sb = new StringBuilder(3*size + 2);
		sb.append("(");
		for (int i = 0; i < size; ++i) {
			sb.append("?, ");
		}
		sb.replace(sb.length() - 2, sb.length(), ")");
		return sb.toString();
	}
}
