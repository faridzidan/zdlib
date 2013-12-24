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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.zidsoft.zdlib.android.MsgFragment;
import com.zidsoft.zdlib.android.MsgInfo;
import com.zidsoft.zdlib.android.MsgType;
import com.zidsoft.zdlib.app.ZDApplication;
import com.zidsoft.zdlib.service.ServiceCallCommand;
import com.zidsoft.zdlib.service.ServiceCallCommand.State;
import com.zidsoft.zdlib.util.ObjectUtils;

/**
 * Base model class.
 * 
 * @author faridz
 *
 */
public class Model {
	protected static final String TAG = Model.class.getSimpleName();
	/**
	 * Whether the model supports delayed listeners. A delayed listener is 
	 * a listener that may start listening after a shared model already processed
	 * some commands before the prospective delayed listener starts listening and
	 * need to be notified of possible missed notifications that the model already
	 * sent out to existing listeners.
	 */
	private boolean m_delayedListenerSupport;
	
	protected class CommandStateNotification {
		public final ServiceCallCommand cmd;
		public final Object oldValue;
		public final Object newValue;
		
		public CommandStateNotification(final ServiceCallCommand cmd, 
				final Object oldValue, final Object newValue) {
			this.cmd = cmd;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		@Override
		public int hashCode() {
			return cmd.hashCode() | oldValue.hashCode() | 2*newValue.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof CommandStateNotification) &&
				ObjectUtils.equals(this.cmd, ((CommandStateNotification) obj).cmd) &&
				ObjectUtils.equals(this.oldValue, ((CommandStateNotification) obj).oldValue) &&
				ObjectUtils.equals(this.newValue, ((CommandStateNotification) obj).newValue);
		}
	}
	
	protected abstract class DelayedListenerCommand<E extends Enum<E>> 
		extends ServiceCallCommand<E> {

		public DelayedListenerCommand(E command) {
			super(command);
		}

		/**
		 * Whether to remove command from replay commands on receiving end
		 * notification of the command.
		 * @return
		 */
		protected abstract boolean isAutoPop();
		
		@Override
		public void onExecBegin() {
			notifyModelChangeListeners(this, STATE, State.Inactive, State.Active);
			// handle listeners that may be added in the future that may be
			// interested in receiving this notification.			
			if (isDelayedListenerSupported()) {
				m_qCmdStateNotification.add(new CommandStateNotification(this, 
						State.Inactive, State.Active));
			}			
		}
		
		@Override
		public void onExecEnd() {
			// if there are no longer any listeners and the there is an error
			// message for the exec display the error message in the application
			// context
			if (m_setModelChangeListener.isEmpty()) {
				final MsgInfo msgInfo = getLastMessage(this.command);
				if (msgInfo != null && msgInfo.hasMsg() &&
					msgInfo.msgType.equals(MsgType.Err) &&
					ZDApplication.getInstance().getCurrentActivity() != null) {
					new MsgFragment().displayDialogMessage(msgInfo, 
							ZDApplication.getInstance().getCurrentActivity()
								.getSupportFragmentManager());
				}
			} else {
				notifyModelChangeListeners(this, STATE, State.Active, State.Inactive);
			}
			// handle listeners that may be added in the future that may be
			// interested in receiving this notification.			
			if (isDelayedListenerSupported()) {
				if (isAutoPop() || isStraggler()) {
					// end notification and an auto pop command or command belongs 
					// to a entity/patient that the model is no longer set to
	
					// remove the corresponding begin notification. 
					// No need to add this end notification
					m_qCmdStateNotification.remove(new CommandStateNotification(this, 
							State.Inactive, State.Active));
				} else {
					m_qCmdStateNotification.add(new CommandStateNotification(this, 
							State.Active, State.Inactive));					
				}
			}			
		}
	};
	
	/**
	 * Queue of command state notifications that are of interest to any listeners
	 * that may be added. Descendants add to the queue any notifications that
	 * a listener, that may be added later, needs to receive but normally would not 
	 * because it was not listening when the notification was initially sent to listeners. 
	 * Descendants must manage the notification queue as needed by the particular model.
	 */
	protected Queue<CommandStateNotification> m_qCmdStateNotification = 
			new ConcurrentLinkedQueue<CommandStateNotification>();
	
	/**
	 * Set of model change listeners that are notified when the model changes (usually
	 * just the corresponding presenter).
	 */
	private Set<PropertyChangeListener> m_setModelChangeListener = 
			new HashSet<PropertyChangeListener>();
	
	/**
	 * Whether the model supports delayed listeners. A delayed listener that may be
	 * added after the model is initialized would need to be notified of any missed
	 * notifications.
	 * @return true or false
	 */
	public boolean isDelayedListenerSupported() {
		return m_delayedListenerSupport;
	}
	public void setDelayedListenerSupported(final boolean state) {
		m_delayedListenerSupport = state;
	}
	
	/**
	 * Map of command names and last message for the command.
	 */
	private Map<String, MsgInfo> m_mapLastMessage = new HashMap<String, MsgInfo>();
	
    public void addModelChangeListener(final PropertyChangeListener listener) {
    	if (m_setModelChangeListener.add(listener)) {
	    	// notify listener if any missed command state notifications
	    	for (CommandStateNotification notification : m_qCmdStateNotification) {
	    		listener.propertyChange(new PropertyChangeEvent(notification.cmd, 
	    				ServiceCallCommand.STATE, 
	    				notification.oldValue, 
	    				notification.newValue));
	    	}
//	    	if (getListenerCount() > getMaxListenerCount()) {
//	    		// no need to maintain notifications if all listeners are already listening
//	    		m_qCmdStateNotification.clear();
//	    	}
    	}
    }
    
    public void removeModelChangeListener(final PropertyChangeListener listener) {
    	m_setModelChangeListener.remove(listener);
    }
	
    protected void notifyModelChangeListeners(final String property, 
    		final Object oldValue, final Object newValue) {
    	for (PropertyChangeListener listener : m_setModelChangeListener) {
    		listener.propertyChange(new PropertyChangeEvent(this, 
    				property, oldValue, newValue));
    	}
    }
    protected void notifyModelChangeListeners(final String property,
    		final Object value) {
    	notifyModelChangeListeners(property, value, null);
    }
    protected void notifyModelChangeListeners(final String property) {
    	notifyModelChangeListeners(property, null, null);
    }
    
    private void notifyModelChangeListeners(final Enum<?> command, 
    		final Object oldValue, final Object newValue) {
    	for (PropertyChangeListener listener : m_setModelChangeListener) {
    		listener.propertyChange(new PropertyChangeEvent(command, 
    				IModel.InlineCommand.STATE, oldValue, newValue));
    	}
    }
    protected void notifyModelChangeListenersBegin(final Enum<?> command) {
    	notifyModelChangeListeners(command, 
    			IModel.InlineCommand.State.Inactive,
    			IModel.InlineCommand.State.Active);
    }
    protected void notifyModelChangeListenersEnd(final Enum<?> command) {
    	notifyModelChangeListeners(command, 
    			IModel.InlineCommand.State.Active,
    			IModel.InlineCommand.State.Inactive);    	
    }    
    protected void notifyModelChangeListeners(final Object source,
    		final String property, final Object oldValue, final Object newValue) {
    	for (PropertyChangeListener listener : m_setModelChangeListener) {
    		listener.propertyChange(new PropertyChangeEvent(source, 
    				property, oldValue, newValue));
    	}
    }    

	/**
	 * Remove from command state notification queue notifications that are complete
	 * (pairs of begin/end notifications) since these are no longer needed once 
	 * the model is set to a different entity. Any in-progress notifications (begin
	 * with no matching end notification) are not removed.
	 */
	protected void cleanNotificationQueue() {
//		if (getListenerCount() >= getMaxListenerCount()) {
//			m_qCmdStateNotification.clear();
//		} else {
			final ArrayList<ServiceCallCommand> deletes = new ArrayList<ServiceCallCommand>();
			// find all notifications that ended
			synchronized (m_qCmdStateNotification) {
				for (CommandStateNotification notification : m_qCmdStateNotification) {
					if (notification.oldValue.equals(State.Active) &&
						notification.newValue.equals(State.Inactive)) {
						deletes.add(notification.cmd);
					}
				}
				// remove the begin/end notification pairs
				for (final ServiceCallCommand cmd : deletes) {
					m_qCmdStateNotification.remove(new CommandStateNotification(cmd, 
							State.Inactive, State.Active));
					m_qCmdStateNotification.remove(new CommandStateNotification(cmd, 
							State.Active, State.Inactive));
				}
			}
//		}
	}
    
	protected void clearLastMessages() {
		m_mapLastMessage = new HashMap<String, MsgInfo>();
	}
	
    public void clearLastMessage(final Enum<?> command) {
    	m_mapLastMessage.remove(command.name());
    }

    public MsgInfo getLastMessage(final Enum<?> command) {
    	return m_mapLastMessage.get(command.name());
    }
    
    public MsgInfo getLastMessage(final Enum<?> command, final int flags) {
    	final MsgInfo msgInfo = getLastMessage(command);
    	if (msgInfo != null) {
    		msgInfo.flags |= flags;
    	}
    	return msgInfo;
    }
    
    public void setLastMessage(final Enum<?> command, final MsgInfo msgInfo) {
    	m_mapLastMessage.put(command.name(), msgInfo);
    }

}
