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

import java.util.Stack;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.zidsoft.zdlib.R;
import com.zidsoft.zdlib.app.ZDApplication;

/**
 * Message and/or progress bar fragment that may be used inline to show indeterminate
 * progress and an inline message. Progressbar and message are not mutually exclusive.
 * Fragment is automatically hidden when all of its visible components are hidden/empty.
 * Due to recycling of listview items, a fragment is not suitable for embedding in 
 * a listview item (will cause sporadic crashes). To use in a listview just embed the
 * fragment layout in the listview item and in getview apply the fragment state
 * to the list view item view using {@link MsgFragment#applyState(View, int)} method
 * which accepts the framelayout of the listview message view.
 * Note that since the msg fragment in that case is never attached to an activity and
 * functions merely as a store for the last message and progress state. 
 * 
 * @author faridz
 *
 */
public class MsgFragment extends SherlockDialogFragment {
	/**
	 * Flag to display message in dialog, rather than using the inline text view.
	 */
	public static final int MSGFLAG_DIALOG = 0x1;
	
	/**
	 * Show state for visible elements. Fragment is hidden when there is nothing
	 * to show and shown otherwise.
	 */
	protected class State {
		/**
		 * 0 for hide, increment/decrement for each show/hide
		 */
		int progressBar;
		MsgInfo msgInfo;
		
		State() {
			this.progressBar = 0;
			this.msgInfo = new MsgInfo();
		}
		
		State(final State state) {
			this.progressBar = state.progressBar;
			this.msgInfo = new MsgInfo(state.msgInfo);
		}
		
		boolean isShowProgress() {
			return progressBar > 0;
		}
		boolean isShow() {
			return progressBar > 0 || msgInfo.hasMsg();
		}			
	}
	protected State m_state = new State();
	
	/**
	 * Modal progressbar stack of this fragment dialogs.
	 */
	protected Stack<MsgFragment> m_modalProgressBar = new Stack<MsgFragment>();
	
	public MsgFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View view = View.inflate(getActivity(), 
				R.layout.zdlib_msg_fragment_mainview, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// apply pending state, if any
		applyProgressBarState(getView());
		if (m_state.msgInfo.hasMsg()) {
			setMessage(m_state.msgInfo.msg, m_state.msgInfo.msgType);
		} else {
			clearMessage();
		}
		showFragment();
	}
	
	protected boolean isShowEligible() { return true; }
	
	protected void showFragment(final boolean state) {
		if (state && !isShowEligible()) {
			return;
		}
		final FragmentTransaction tran = getChildFragmentManager().beginTransaction();
		if (state) {
			tran.show(this);
		} else {
			tran.hide(this);
		}
		tran.commitAllowingStateLoss()/*commit()*/;
	}
	
	/**
	 * Determine whether the msg fragment has some content, such as a message to
	 * display to the user or in progress progressbar.
	 * @return
	 */
	public boolean hasContent() {
		return m_state.isShow();
	}
	public boolean hasMsg() {
		return m_state.msgInfo.hasMsg();
	}
	
	public boolean isShowProgress() {
		return m_state.isShowProgress();
	}
	/**
	 * Show fragment if any of its elements are to be shown or hide fragment if
	 * none of its elements is eligible to be shown.
	 */
	protected void showFragment() {
		showFragment(hasContent());
	}
	
	protected View getMessageWrapper(final View view) {
		return view == null ? null : 
			view.findViewById(R.id.msg_wrapper);
	}
	
	protected TextView getMessageView(final View view) {
		return view == null ? null : 
			(TextView) view.findViewById(R.id.msg_text);
	}

	protected ProgressBar getProgressBar(final View view) {
		return view == null ? null :
			(ProgressBar) view.findViewById(R.id.progressBar1);		
	}

	public void showProgressBar(final boolean state) {
		final View view = getView();
		if (state) {
			++m_state.progressBar;
		} else {
			--m_state.progressBar;
		}
		final ProgressBar progressBar = getProgressBar(view);
		if (progressBar == null) {
			// deferred until fragment views are created
			return;
		}
		applyProgressBarState(view);		
		showFragment();
	}
	
	/**
	 * Display a modal progressbar with the given message. No overload for msg resourceId
	 * since msg fragment may not be attached to an activity yet.
	 * @param msg
	 * @param fm fragment manager for case where msg fragment is not attached to an activity.
	 */
	public void pushModalProgressBar(final String msg, final FragmentManager fm) {
		final MsgFragment msgFragment = new MsgFragment();
		msgFragment.setMessage(msg, MsgType.Information);
		msgFragment.setStyle(STYLE_NO_TITLE | STYLE_NORMAL /*modal*/,
				R.style.Theme_Sherlock_Light_Dialog);
		msgFragment.setCancelable(false);
		msgFragment.showProgressBar(true);
		msgFragment.show(fm, "dialog");
		// do it now so user can not use back button or navigate away
		fm.executePendingTransactions();
		m_modalProgressBar.push(msgFragment);
	}
	public void pushModalProgressBar(final String msg) {
		pushModalProgressBar(msg, getChildFragmentManager());
	}
	
	public void popModalProgressBar() {
		if (!m_modalProgressBar.isEmpty()) {
			final MsgFragment msgFragment = m_modalProgressBar.pop();
			msgFragment.dismiss();
		}
	}
	
	public void clearMessage() {
		final View view = getView();
		m_state.msgInfo.clear();
		if (getMessageWrapper(view) != null) {
			getMessageWrapper(view).setVisibility(View.GONE);
			showFragment();
		}
	}
	
	public static String getDialogTag() {
		return MsgFragment.class.getSimpleName() + ".DialogTag";
	}
	
	/**
	 * Display a dialog message using the given fragment manager. Allows for displaying
	 * dialog message for a msg fragment that is not attached to an activity.
	 * @param msg
	 * @param msgType
	 * @param fm
	 */
	public void displayDialogMessage(final String msg, final MsgType msgType,
			final FragmentManager fm) {
		// display the message in its own message dialog
		final MsgFragment msgFragment = new MsgFragment();
		msgFragment.setMessage(msg, msgType);
		msgFragment.setStyle(STYLE_NO_TITLE, R.style.Theme_Sherlock_Light_Dialog);
		msgFragment.show(fm, getDialogTag());
	}
	public void displayDialogMessage(final int msgResourceId, 
			final MsgType msgType, final FragmentManager fm) {
		displayDialogMessage(ZDApplication.getInstance().getString(msgResourceId),
				msgType, fm);
	}	
	public void displayDialogMessage(final MsgInfo msgInfo, final FragmentManager fm) {
		if (msgInfo != null && msgInfo.hasMsg()) {
			displayDialogMessage(msgInfo.msg, msgInfo.msgType, fm);
		}
	}
	public void displayDialogMessage(final Exception e, final FragmentManager fm) {
		displayDialogMessage(e.getMessage(), MsgType.Err, fm);
	}
	
	/**
	 * Set message to the given message text and msg type. No overload for msg resourceId
	 * since msg fragment may not be attached to an activit yet and getActivity will
	 * return null in that case.
	 * @param msg Message text.
	 * @param msgId optional msgId to help identify current message
	 * @param msgType
	 * @param flags
	 */
	public void setMessage(final String msg, final Integer msgId, 
			final MsgType msgType, final int flags) {
		if ((flags & MSGFLAG_DIALOG) != 0) {
			// display the message in its own message dialog
			displayDialogMessage(msg, msgType, getChildFragmentManager());
			return;
		}
		m_state.msgInfo = new MsgInfo(msg, msgId, msgType, flags);
		if (getMessageView(getView()) != null) {
			applyMessageState(getView());
			showFragment();
		}
	}
	public void setMessage(final String msg, final MsgType msgType, 
			final int flags) {
		setMessage(msg, null, msgType, flags);
	}
	public void setMessage(final String msg, final Integer msgId, 
			final MsgType msgType) {
		setMessage(msg, msgId, msgType, 0);
	}	
	public void setMessage(final String msg, final MsgType msgType) {
		setMessage(msg, null, msgType, 0);
	}
	public void setMessage(final MsgInfo msgInfo) {
		if (msgInfo == null || !msgInfo.hasMsg()) {
			clearMessage();
		} else {
			setMessage(msgInfo.msg, msgInfo.msgId, msgInfo.msgType, msgInfo.flags);
		}
	}
	public void setMessage(final MsgInfo msgInfo, final int flags) {
		if (msgInfo != null) {
			msgInfo.flags |= flags;
			setMessage(msgInfo);
		}
	}
	
	public MsgType getMsgType() {
		return m_state.msgInfo.msgType == null ? null :
			m_state.msgInfo.msgType;
	}
	public String getMsg() { return m_state.msgInfo.msg; }
	public Integer getMsgId() { return m_state.msgInfo.msgId; }
	
	protected void applyProgressBarState(final View view) {
		getProgressBar(view).setVisibility(m_state.isShowProgress() ? 
											View.VISIBLE : View.GONE);
	}
	
	protected void applyMessageState(final View view) {
		final boolean hasMsg = m_state.msgInfo.hasMsg(); 
		if (hasMsg) {
			final String msg 	  = m_state.msgInfo.msg;
			final MsgType msgType = m_state.msgInfo.msgType;
			final TextView msgView = getMessageView(view);
			msgView.setText(msg == null ? "" : msg);
			msgView.setTextColor(ZDApplication.getInstance()
					.getResources().getColor(msgType.textColorResourceId));
			final ImageView imageView = (ImageView) view.findViewById(R.id.msg_image);
			final boolean showImage = msgType.imageResourceId != -1; 
			if (showImage) {
				imageView.setImageResource(msgType.imageResourceId);
			}
			imageView.setVisibility(showImage ? View.VISIBLE : View.GONE);
		}
		getMessageWrapper(view).setVisibility(hasMsg ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * Copy the state of the message fragment to the given framelayout id. The given 
	 * framelayout must contain view whose layout is mobilizer_msg_fragment_mainview.xml
	 * @param root root view to search for containerId
	 * @param containerId view id that contains a child mobilizer_msg_fragment_mainview.xml
	 * and that will receive the state of the msg fragment.
	 */
	public void applyState(View root, final int containerId) {
		final View view = root.findViewById(containerId);
		applyProgressBarState(view);
		applyMessageState(view);
		view.setVisibility(m_state.isShow() ? View.VISIBLE : View.GONE);
		view.setMinimumHeight(m_state.isShow() ? (int) ZDApplication.getInstance()
			.getResources().getDimension(
					R.dimen.zdlib_msg_fragment_min_height) : 0);
	}
	public void applyState(View root) {
		applyState(root, R.id.msg_fragment_container);
	}
	
	public void copyState(final MsgFragment src) {
		m_state = new State(src.m_state);
	}
}
