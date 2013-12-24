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

import com.zidsoft.zdlib.app.ZDApplication;


/**
 * Message info class. Msg is string instead of a resourceId because http service 
 * calls may return a string error message.
 *  
 * @author faridz
 * 
 */
public class MsgInfo {
	public String msg;
	/**
	 * Optional id to help identify the current msg, if any.
	 */
	public Integer msgId;
	public MsgType msgType;
	public int flags;
	
	public MsgInfo() {
		msg = null;
		msgId = null;
		msgType = null;
		flags = 0;
	}

	public MsgInfo(final MsgInfo msgInfo) {
		this.msg 	 = msgInfo.msg;
		this.msgId 	 = msgInfo.msgId;
		this.msgType = msgInfo.msgType;
		this.flags   = msgInfo.flags;
	}

	public MsgInfo(final String msg, final Integer msgId, final MsgType msgType, 
			final int flags) {
		this.msg = msg;
		this.msgId = msgId;
		this.msgType = msgType;
		this.flags = flags;		
	}
	
	public MsgInfo(final String msg) {
		this(msg, null, MsgType.Information, 0);
	}
	
	public MsgInfo(final String msg, final MsgType msgType) {
		this(msg, null, msgType, 0);
	}
	public MsgInfo(final String msg, final Integer msgId, final MsgType msgType) {
		this(msg, msgId, msgType, 0);
	}

	public MsgInfo(final String msg, final MsgType msgType, final int flags) {
		this(msg, null, msgType, flags);
	}
	
	public MsgInfo(final int msgResourceId, final MsgType msgType) {
		this(ZDApplication.getInstance().getResources().getString(msgResourceId),
				msgResourceId, msgType, 0);
	}
	
	public MsgInfo(final int msgResourceId) {
		this(ZDApplication.getInstance().getResources().getString(msgResourceId),
				msgResourceId, MsgType.Information, 0);
	}
	
	void clear() {
		msg = null;
		msgId = null;
		msgType = null;
		flags = 0;
	}
	
	public boolean hasMsg() {
		return msg != null && msgType != null;
	}
}