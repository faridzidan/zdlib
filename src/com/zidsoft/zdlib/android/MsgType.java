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
 
 package com.zidsoft.zdlib.android;

import com.zidsoft.zdlib.R;

/**
 * Message type enum for classifying messages displayed to the user on the screen.
 * 
 * @author faridz
 *
 */
public enum MsgType {
	Information(-1, R.color.gray),
	NoDataFound(-1, R.color.CornflowerBlue),
	SuccessWithInfo(-1, R.color.CornflowerBlue),
	Warning(R.drawable.ic_dialog_alert, R.color.gray),
	Err(R.drawable.ic_dialog_alert, R.color.gray);
	
	public final int imageResourceId;
	public final int textColorResourceId;
	MsgType(final int imageResourceId,
			final int textColorResourceId) {
		this.imageResourceId = imageResourceId;
		this.textColorResourceId = textColorResourceId;
	}
}
