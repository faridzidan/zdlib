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
 * {@link ServiceCall} callback interface. Callback methods are guaranteed to
 * execute in UI thread.
 * 
 * @author faridz
 *
 */
public interface ServiceCallClient {

	/**
	 * Notifies client that service call finished. Client must inspect the result
	 * object to determine whether the service call finished successfully or with errors.
	 * @param call service call object
	 * @param cmd service call command, if any. Client may modify the service 
	 * command object to pass information (in case of success, for example) to 
	 * the command end handler.
	 * @param result
	 */
	void onServiceCallFinished(ServiceCall call,
			ServiceCallCommand cmd, ServiceCallResult result);
	
	/**
	 * Notifies client that service call failed. Additional information about the
	 * failure may be present in the result object.
	 * @param call service call object
	 * @param cmd service call command, if any
	 * @param result
	 */
	void onServiceCallFailed(ServiceCall call, 
			ServiceCallCommand cmd, ServiceCallResult result);
	
	/**
	 * Notifies client that service call was canceled. Result object may have
	 * additional details about the cancel.
	 * @param call service call object
	 * @param cmd service call command, if any
	 * @param result
	 */
	void onServiceCallCanceled(ServiceCall call, 
			ServiceCallCommand cmd, ServiceCallResult result);
}
