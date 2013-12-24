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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zidsoft.zdlib.R;
import com.zidsoft.zdlib.app.ZDApplication;

/**
 * Singleton class for displaying one instance of no network connectivity 
 * message dialog to the user.
 * 
 * @author faridz
 *
 */
public class NoConnectivityMsgFragment extends MsgFragment {

	public static String getDialogTag() {
		return NoConnectivityMsgFragment.class.getSimpleName() + ".DialogTag";
	}
	
	public static void displayMessage() {
		final ZDFragmentActivity activity = ZDApplication.getInstance()
				.getCurrentActivity();
		if (activity != null) {
			final FragmentManager fm = activity.getSupportFragmentManager();
			Fragment fragment = fm.findFragmentByTag(getDialogTag());
			// do not show the dialog if it is already showing
			if (fragment == null) {
				final MsgFragment msgFragment = new NoConnectivityMsgFragment();
				msgFragment.setMessage(ZDApplication.getInstance()
						.getString(R.string.zdlib_not_connected_to_network), MsgType.Err);
				msgFragment.setStyle(STYLE_NO_TITLE, R.style.Theme_Sherlock_Light_Dialog);
				msgFragment.show(fm, getDialogTag());
			}
		}
	}
}
