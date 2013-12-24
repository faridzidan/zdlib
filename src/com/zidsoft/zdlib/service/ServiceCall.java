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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Looper;

import com.zidsoft.zdlib.android.NoConnectivityMsgFragment;
import com.zidsoft.zdlib.common.NotConnectedToNetworkException;

/**
 * Generic service call object. Allows executing a task/service call in 
 * background thread, as necessary, using a thread pool.
 * Based on http://code.google.com/p/shelves/source/browse/trunk/Shelves/src/org/curiouscreature/android/shelves/util/UserTask.java
 * @author faridz
 *
 */
public abstract class ServiceCall {
    private static final String TAG = ServiceCall.class.getSimpleName();	
	private static final String MSG_ALREADY_EXECUTING = 
			"Cannot execute task: the task is already running.";
	private static final String MSG_ALREADY_EXECUTED = 
			"Cannot execute task: the task has already been executed " +
			"(a task can be executed only once)";
	/*
    * Indicates the current status of the service call. Each status will be set only once
    * during the lifetime of a service call.
    */
	public enum Status {
		/**
		* Indicates that the service call has not been executed yet.
		*/
		PENDING,
		/**
		* Indicates that the service call is running.
		*/
		RUNNING,
	   /**
		* Indicates that {@link UserTask#onPostExecute(Object)} has finished.
		*/
		FINISHED;
	}

    private volatile Status m_status = Status.PENDING;
    private ServiceCallResult m_result = null;
    private ServiceCallClient m_client = null;
    final private ServiceCallCommand m_cmd;
    private ServiceCallResultMapper m_mapper = null;
    
    private static final int CORE_POOL_SIZE;
    static {
    	final int SCALEFACTOR = 2;
    	final int cores = Runtime.getRuntime().availableProcessors();
    	int maxThreads = cores * SCALEFACTOR;
    	CORE_POOL_SIZE = (maxThreads > 0 ? maxThreads : 1);
    }
    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE;
    private static final int KEEP_ALIVE = 10;    
    
    /**
     * Unbounded work queue allows unlimited number of pending service calls.
     */
    private static final LinkedBlockingQueue<Runnable> m_workQueue =
            new LinkedBlockingQueue<Runnable>();
    
    private static final ThreadPoolExecutor m_executor = new ThreadPoolExecutor(
		CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, 
			m_workQueue, new RejectedExecutionHandler() {
				
				@Override
				public void rejectedExecution(Runnable r, 
						ThreadPoolExecutor executor) {
					// todo: notify client that service call is canceled
				}
			});
    
    private final FutureTask<ServiceCallResult> m_task;
    private static final Handler m_handler = new Handler();
    
    /**
     * Information about current active service calls and possible listeners
     * for end of the last active service call for a class.
     * @author faridz
     *
     */
    private static class ActiveInfo {
    	Map<String, Set<ServiceCall>> commandCalls;
    	/**
    	 * Listeners to be notified when the last active service call for class
    	 * key ends and the class key will no longer have any active service calls.
    	 */
    	Set<PropertyChangeListener> emptyActiveListeners;
    }
    
	/**
	 * Map of active info per command class key, command key.
	 */
	private static final Map<String, ActiveInfo> m_mapActive = 
			new HashMap<String, ActiveInfo>();
    
   private void onServiceCallFailed(final ServiceCallResult result) {
	   removeActive(m_cmd, this);
	   if (m_client != null) {
		   m_handler.post(new Runnable() {
	
			@Override
			public void run() {
				if (isStraggler()) {
					m_result.setCanceled();
					m_client.onServiceCallCanceled(ServiceCall.this, m_cmd, result);
				} else {
					m_client.onServiceCallFailed(ServiceCall.this, m_cmd, result);
					if (result.exception instanceof NotConnectedToNetworkException) {
						NoConnectivityMsgFragment.displayMessage();
					}
				}
				if (m_cmd != null) {
					m_cmd.onExecEnd();
				}
			}
		   });
	   } else if (m_cmd != null) {
		   m_handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (result.exception instanceof NotConnectedToNetworkException) {
					NoConnectivityMsgFragment.displayMessage();
				}				
				m_cmd.onExecEnd();				
			}
		});
	   } else {
			if (result.exception instanceof NotConnectedToNetworkException) {
				m_handler.post(new Runnable() {
					
					@Override
					public void run() {
						NoConnectivityMsgFragment.displayMessage();						
					}
				});
			}		   
	   }
   }
   
   private void onServiceCallCanceled(final ServiceCallResult result) {
	   removeActive(m_cmd, this);
	   if (m_client != null) {
		   m_handler.post(new Runnable() {
	
			@Override
			public void run() {
				m_client.onServiceCallCanceled(ServiceCall.this, m_cmd, result);
				if (m_cmd != null) {
					m_cmd.onExecEnd();
				}
			}
		   });
	   } else if (m_cmd != null) {
		   m_handler.post(new Runnable() {
			
			@Override
			public void run() {
				m_cmd.onExecEnd();				
			}
		});
	   }	   
   }
   
   public ServiceCall(final ServiceCallCommand cmd) {
	   m_cmd = cmd;
       m_task = new FutureTask<ServiceCallResult>(new Callable<ServiceCallResult>() {

			@Override
			public ServiceCallResult call() throws Exception {
				if (isStraggler()) {
					m_task.cancel(true);
				}
				//Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);				
				return doInBackground();
			}}) {
        	
            @Override
            protected void done() {
            	m_status = Status.FINISHED;
                m_result = null;
                try {
                    m_result = get();
                    if (m_result.exception != null) {
                    	android.util.Log.e(TAG, "Result exception", m_result.exception);
                    	onDone(m_result);
            			onServiceCallFailed(m_result);
                        return;
                    }
                } catch (CancellationException e) {
                	m_result.setCanceled();
                	onDone(m_result);
                	onServiceCallCanceled(m_result);
                    return;
                } catch (InterruptedException e) {
                	android.util.Log.w(TAG, e);
                    m_result.setResult(e);
                    onDone(m_result);
                    onServiceCallFailed(m_result);
                    return;
                } catch (ExecutionException e) {
                	android.util.Log.e(TAG, "ExecutionException", e.getCause());
                	m_result.setResult(e);
                	onDone(m_result);
                	onServiceCallFailed(m_result);
                	return;
                } catch (Throwable t) {
                	android.util.Log.e(TAG, "Throwable", t.getCause());
                	m_result.setResult(t);
                	onDone(m_result);
                	onServiceCallFailed(m_result);
                	return;
                }

                if (m_mapper != null) {
                	try {
                		m_result = m_mapper.map(m_result);
                	} catch (Throwable t) {
                		t.printStackTrace();
                		m_result.setResult(t);
                		onDone(m_result);
                		onServiceCallFailed(m_result);
                    	return;
                	}
                }
                onDone(m_result);
				removeActive(m_cmd, ServiceCall.this);                
                if (m_client != null) {
	                m_handler.post(new Runnable() {
	
						@Override
						public void run() {
							final boolean straggler = isStraggler();
							if (!straggler) {
								ServiceCall.this.onPostExecute(m_result);
							}
							if (straggler) {
								m_result.setCanceled();
								m_client.onServiceCallCanceled(ServiceCall.this, 
										m_cmd, m_result);
							} else {
								m_client.onServiceCallFinished(ServiceCall.this,
										m_cmd, m_result);
							}
							if (m_cmd != null) {
								m_cmd.onExecEnd();
							}							
						}
	                });
                } else if (m_cmd != null) {
         		   m_handler.post(new Runnable() {
         				
         				@Override
         				public void run() {
         					m_cmd.onExecEnd();				
         				}
         			});
                }
            }
        };
	}

   public ServiceCall() {
	   this((ServiceCallCommand) null);
   }
   
   /**
    * Returns the current status of this task.
    *
    * @return The current status.
    */
   public final Status getStatus() {
       return m_status;
   }
   
   public void setMapper(final ServiceCallResultMapper mapper) {
	   m_mapper = mapper;
   }
   public ServiceCallResultMapper getMapper() { return m_mapper; };
   
   /**
    * Returns <tt>true</tt> if this task was cancelled before it completed
    * normally.
    *
    * @return <tt>true</tt> if task was cancelled before it completed
    *
    * @see #cancel(boolean)
    */
   public final boolean isCancelled() {
       return m_task.isCancelled();
   }

   /**
    * Attempts to cancel execution of this task.  This attempt will
    * fail if the task has already completed, already been cancelled,
    * or could not be cancelled for some other reason. If successful,
    * and this task has not started when <tt>cancel</tt> is called,
    * this task should never run.  If the task has already started,
    * then the <tt>mayInterruptIfRunning</tt> parameter determines
    * whether the thread executing this task should be interrupted in
    * an attempt to stop the task.
    *
    * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
    *        task should be interrupted; otherwise, in-progress tasks are allowed
    *        to complete.
    *
    * @return <tt>false</tt> if the task could not be cancelled,
    *         typically because it has already completed normally;
    *         <tt>true</tt> otherwise
    *
    * @see #isCancelled()
    * @see #onCancelled() 
    */
   public final boolean cancel(boolean mayInterruptIfRunning) {
       return m_task.cancel(mayInterruptIfRunning);
   }

    /**
     * Override this method to perform a computation on a background thread. 
     *
     * This method can call {@link #publishProgress(Object[])} to publish updates
     * on the UI thread.
     *
     * @return A result, defined by the subclass of this task.
     *
     * @see #onPreExecute()
     * @see #onPostExecute(Object)
     */
    protected abstract ServiceCallResult doInBackground();
	
    /**
     * Final processing to do any additional clean up. Always called whether
     * task succeed, fails or is canceled. Desendants my override this method
     * to implement final clean up in the background thread. Should not throw
     * any exceptions.
     * @param result
     */
    protected void onDone(ServiceCallResult result) {};
    /**
     * Runs on the UI thread before {@link #doInBackground(Object[])}.
     *
     * @see #onPostExecute(Object)
     * @see #doInBackground(Object[])
     */
    protected void onPreExecute() {
    }

    /**
     * Runs on the UI thread after {@link #doInBackground(Object[])}. The
     * specified result is the value returned by {@link #doInBackground(Object[])}
     * or null if the task was cancelled or an exception occured.
     *
     * @param result The result of the operation computed by {@link #doInBackground(Object[])}.
     *
     * @see #onPreExecute()
     * @see #doInBackground(Object[])
     */
    protected void onPostExecute(ServiceCallResult result) {
    }
	
    /**
     * Submit service call for execution. Usually called from main thread. 
     * @param client
     * @param lock a lock to acquire before service call is actually eligible
     * for execution.
     */
    public final void exec(final ReentrantLock lock,
    		final ServiceCallClient client) {
        if (m_status != Status.PENDING) {
            switch (m_status) {
                case RUNNING:
                    throw new IllegalStateException(MSG_ALREADY_EXECUTING);
                case FINISHED:
                    throw new IllegalStateException(MSG_ALREADY_EXECUTED);
			default:
				break;
            }
        }
        m_client = client;
        m_status = Status.RUNNING;
       	addActive(m_cmd, this);
        onPreExecute();
        if (lock != null) {
        	Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
		        	lock.lock();
		        	m_executor.execute(m_task);
				}
			});
        	thread.start();
        } else {
        	m_executor.execute(m_task);
        }
    }
    public final void exec(final ServiceCallClient client) {
    	exec(null, client);
    }
    
    public final ServiceCallResult execImmediate() {
        if (m_status != Status.PENDING) {
            switch (m_status) {
                case RUNNING:
                    throw new IllegalStateException(MSG_ALREADY_EXECUTING);
                case FINISHED:
                    throw new IllegalStateException(MSG_ALREADY_EXECUTED);
			default:
				break;
            }
        }
        m_client = null;
        m_status = Status.RUNNING;
       	addActive(m_cmd, this);

        onPreExecute();
        m_task.run();
        if (!isStraggler()) {
        	onPostExecute(m_result);
        }
        
        return m_result;
    }
    
    private boolean isStraggler() {
    	return m_cmd != null && m_cmd.isStraggler();
    }
    
	private static void addActive(final ServiceCallCommand cmd, 
								  final ServiceCall call) {
		if (cmd != null) {
			final String classKey = cmd.getClassKey();
			final String key = cmd.getKey();			
			synchronized(m_mapActive) {
				ActiveInfo activeInfo = m_mapActive.get(classKey);
				if (activeInfo == null) {
					activeInfo = new ActiveInfo();
					activeInfo.commandCalls = new HashMap<String, Set<ServiceCall>>();
					m_mapActive.put(classKey, activeInfo);					
				}
				Set<ServiceCall> calls = activeInfo.commandCalls.get(key);
				if (calls == null) {
					calls = new HashSet<ServiceCall>();
					activeInfo.commandCalls.put(key, calls);
				}
				calls.add(call);
			}
			if (isMainThread()) {
				cmd.onExecBegin();
			} else {
				m_handler.post(new Runnable() {
					
					@Override
					public void run() {
						cmd.onExecBegin();						
					}
				});
			}
		}
	}
    
	private static void removeActive(ServiceCallCommand cmd, 
			final ServiceCall call) {
		if (cmd != null) {
			final String classKey = cmd.getClassKey();
			final String key = cmd.getKey();
			synchronized(m_mapActive) {
				final ActiveInfo activeInfo = m_mapActive.get(classKey);
				if (activeInfo != null) {
					final Set<ServiceCall> calls = activeInfo.commandCalls.get(key);		
					if (calls != null) {
						calls.remove(call);
						if (calls.isEmpty()) {
							activeInfo.commandCalls.remove(key);
						}
					}
					if (activeInfo.commandCalls.isEmpty()) {
						// remove the last active command
						if (activeInfo.emptyActiveListeners != null) {
							// make a copy (don't want to hold lock for runnable)
							final Set<PropertyChangeListener> emptyActiveListeners =
								new HashSet<PropertyChangeListener>(
										activeInfo.emptyActiveListeners);
							m_handler.post(new Runnable() {
								
							@Override
							public void run() {
								for (final PropertyChangeListener listener :
									emptyActiveListeners) {
									listener.propertyChange(
										new PropertyChangeEvent(ServiceCall.class.getSimpleName(), 
					    				ServiceCallCommand.Notification.onEndActive.name(), 
					    					classKey, null));
								}
							}
							});
						}
						m_mapActive.remove(classKey);
					}
				}
			}
		}
	}

	/**
	 * Add a listener to listen for when the given command class key has its last
	 * remaining active call removed and no longer has any active calls. The
	 * listener is automatically removed from the command class registered 
	 * listeners when the last active command for the class finishes and after 
	 * the listener is notified. Usually called from UI thread.
	 * @param classKey command class key
	 * @param listener listener to be notified when the condition is met
	 * @return true if the given command class key has at least one active command
	 * and the given listener is successfully added to be notified of the 
	 * condition, false otherwise.
	 */
	public static boolean addEndActiveListener(final String classKey,
			final PropertyChangeListener listener) {
		synchronized(m_mapActive) {
			final ActiveInfo activeInfo = m_mapActive.get(classKey);
			if (activeInfo == null || activeInfo.commandCalls == null ||
				activeInfo.commandCalls.isEmpty()) {
				return false;
			}
			if (activeInfo.emptyActiveListeners == null) {
				activeInfo.emptyActiveListeners = new HashSet<PropertyChangeListener>();
			}
			activeInfo.emptyActiveListeners.add(listener);
		}
		listener.propertyChange(
				new PropertyChangeEvent(ServiceCall.class.getSimpleName(), 
				ServiceCallCommand.Notification.onEndActiveListenBegin.name(), 
					classKey,null));
		
		return true;
	}
	
	/**
	 * Determine whether the given command class and command class key is active.
	 * @param classKey command class key
	 * @param key command key
	 * @return true if active, false otherwise.
	 */
	public static boolean isActiveCommand(String classKey, String key) {
		final ActiveInfo activeInfo = m_mapActive.get(classKey);
		final Set<ServiceCall> calls = activeInfo == null ? null :
			activeInfo.commandCalls.get(key);
		return calls != null && !calls.isEmpty();
	}
	/**
	 * Determine whether the given command class key has at least one active
	 * command.
	 * @param classKey command class key
	 * @return true if the given command class key has at least one active command.
	 */
	public static boolean hasActiveCommand(String classKey) {
		final ActiveInfo activeInfo = m_mapActive.get(classKey);
		return activeInfo != null && !activeInfo.commandCalls.isEmpty();
	}

	/**
	 * Determine if this thread is the main application thread
	 * (UI thread).
	 * @return
	 */
	public static boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
	
}
