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

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.zidsoft.zdlib.android.ZDFragmentActivity;

public abstract class ZDApplication extends Application {
	protected static ZDApplication m_application;
	protected ZDFragmentActivity m_currentActivity;
	
	@Override
	public void onCreate() {
		super.onCreate();
		m_application = this;
	}
	
	public static ZDApplication getInstance() {
		return m_application;
	}
	
	
	public final ZDFragmentActivity getCurrentActivity() {
		return m_currentActivity;
	}
	
	public final void setCurrentActivity(final ZDFragmentActivity activity) {
		m_currentActivity = activity;
	}
	
	public abstract IZDApplicationModel getModel();
	
	/**
	 * Get the server base url for the application.
	 * @return server base url. Example: http://google.com/
	 */
	public abstract String getServerBaseUrl();
	
	public boolean isConnectedToNetwork() {
		boolean state = false;
		try {
			final int[] networkTypes = {
					ConnectivityManager.TYPE_WIFI,
					ConnectivityManager.TYPE_MOBILE,
					ConnectivityManager.TYPE_WIMAX,
					ConnectivityManager.TYPE_ETHERNET};
			final ConnectivityManager cm = (ConnectivityManager)  
					getSystemService(Context.CONNECTIVITY_SERVICE);
			for (final int networkType : networkTypes) {
				final NetworkInfo networkInfo = cm.getNetworkInfo(networkType);
				if (networkInfo != null &&
					networkInfo.getState() == NetworkInfo.State.CONNECTED) {
					state = true;
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			state = false;
		}
		return state;
	}
	
	/**
	 * Determine whether device api supports SearchView.
	 * @return true if device supports SearchView
	 */
	static public boolean isHoneycombOrAbove() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}	
}
