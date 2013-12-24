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
 
package com.zidsoft.zdlib.mvp;

import java.beans.PropertyChangeListener;

import com.zidsoft.zdlib.android.MsgInfo;
import com.zidsoft.zdlib.service.ServiceCallCommand;

public interface IModel {
	/** An inline command is a command that is executed directly in the model
	 * and usually in the UI thread. Contrast with {@link ServiceCallCommand}
	 * @author faridz
	 *
	 */
	public static class InlineCommand {
		public static final String STATE = InlineCommand.class.getSimpleName() 
				+ ".State";
		public enum State {
			Inactive,
			Active;
		}
	}
	
    void addModelChangeListener(final PropertyChangeListener listener);
    void removeModelChangeListener(final PropertyChangeListener listener);
    
    boolean isDelayedListenerSupported();
    void setDelayedListenerSupported(final boolean state);
    
    /**
     * Clear the last message, if any, for the given command.
     * @param key
     */
    void clearLastMessage(final Enum<?> command);
    
    /**
     * Get the last message, if any, for the given command.
     * @param key
     * @return last message for the key if it has a last message, null otherwise.
     */    
    MsgInfo getLastMessage(final Enum<?> command);
    
    /**
     * Get the last message, if any, for the given key.
     * @param key
     * @flags additional flags to add to the last message, if any, flags.
     * @return last message for the key if it has a last message, null otherwise.
     */    
    MsgInfo getLastMessage(final Enum<?> command, final int flags);
    
    
    /**
     * Set the last message for the given key to the given message.
     * @param key
     * @param msgInfo
     */
    void setLastMessage(final Enum<?> command, final MsgInfo msgInfo);
}
