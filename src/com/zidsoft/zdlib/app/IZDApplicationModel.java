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
 
 package com.zidsoft.zdlib.app;

import com.zidsoft.zdlib.mvp.IModel;

/**
 * Application model interface.
 * 
 * @author faridz
 *
 */
public interface IZDApplicationModel extends IModel {
	/**
	 * Get user-specific key for the given key and current user. Returned
	 * key will be the same across systems but specific to the current user.
	 * @param key
	 * @return
	 * @see #getSystemUserKey(String)
	 */
	String getUserKey(final String key);
	
	/**
	 * Get system and user specific key for the given key. The returned key
	 * is specific to the current system and current user and is different
	 * for different users and systems.
	 * @param key
	 * @return
	 * @see #getUserKey(String)
	 */
	String getSystemUserKey(final String key);
}
