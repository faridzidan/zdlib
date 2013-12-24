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

package com.zidsoft.zdlib.service;

/**
 * Service call command. Generalized interface allows notification of command
 * start and command end and tracking of active commands. 
 * 
 * @author faridz
 *
 */
public abstract class ServiceCallCommand<E extends Enum<E>> {
	public static final String STATE = ServiceCallCommand.class.getSimpleName() 
			+ ".State";
	public enum Notification {
		onEndActiveListenBegin,
		onEndActive;
	}
	public enum State {
		Inactive,
		Active;
	}
	
	public final E command;
	
	public ServiceCallCommand(final E command) {
		this.command = command;
	}
	
	/**
	 * Get key for the command for this class of commands.
	 * @return
	 */
	public String getKey() {
		return command.name();
	}
	
	/**
	 * Get the class key for this command. Commands are tracked per class key.
	 * @return
	 */
	public abstract String getClassKey();
	
	/**
	 * Determine whether this command call is a straggler call. A straggler call is
	 * call that was started but when finished its results are no longer needed (due
	 * to a change in the model that supersedes the call). When a call is identified
	 * as a straggler call, the client onServiceCallCanceled method is called instead
	 * and the call is effectively or actually canceled.
	 * @return
	 */
	public abstract boolean isStraggler();
		
	protected abstract void onExecBegin();
	protected abstract void onExecEnd();
}
