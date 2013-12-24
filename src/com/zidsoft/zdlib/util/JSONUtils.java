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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Miscellaneous json utility methods mainly for getting the value of a json
 * object property and returning null when the property does not exist or is null
 * without throwing an exception.
 * 
 * @author faridz
 *
 */
public class JSONUtils {

	private JSONUtils() {}

	public static Object safeGetObject(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.get(name);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static String safeGetString(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.getString(name);
		} catch (JSONException e) {
			return null;
		}
	}

	public static Integer safeGetInteger(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.getInt(name);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static Long safeGetLong(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.getLong(name);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static Boolean safeGetBoolean(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.getBoolean(name);
		} catch (JSONException e) {
			return null;
		}
	}
	public static boolean safeGetBoolean(final JSONObject obj, final String name,
			final boolean defaultVal) {
		try {
			return obj.isNull(name) ? defaultVal : obj.getBoolean(name);
		} catch (JSONException e) {
			return defaultVal;
		}
	}
	
	public static JSONArray safeGetJOSONArray(final JSONObject obj, final String name) {
		try {
			return obj.isNull(name) ? null : obj.getJSONArray(name);
		} catch (JSONException e) {
			return null;
		}		
	}
}
