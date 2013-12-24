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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.LinearLayout;

import com.zidsoft.zdlib.R;

/**
 * View class that implements touch delegate view where the delegate forwards
 * touches to the parent view and therefore allows a small view to have a larger
 * touch area.
 * <br/><br/>Adapted from:<br/>
 * <a href="https://plus.google.com/u/0/+RomanNurik/posts/5HDnfDCFWQe">
 * Roman Nurik - Google+ - #AndroidDev #AndroidProtip #Usability Be mindful of your…
 * </a>
 * <br />
 * <a href="http://cyrilmottier.com/2012/02/16/listview-tips-tricks-5-enlarged-touchable-areas/">
 * ListView Tips & Tricks #5: Enlarged Touchable Areas - Cyril Mottier
 * </a>
 * @author faridz
 *
 */
public class LargeTouchAreaView extends LinearLayout {
	protected static final int DEBUG_COLOR = Color.argb(50, 255, 0, 0); 
	protected View m_touchDelegateView;
	protected int m_touchDelegateFlags;
	protected final int m_touchableMinCx;
	protected final int m_touchableMinCy;
	protected Rect m_curRect;
	protected boolean m_debug;
	protected int m_debugColor = DEBUG_COLOR;
	
	public LargeTouchAreaView(Context context) {
		super(context);
		m_touchableMinCx = context.getResources().getDimensionPixelSize(
				R.dimen.zdlib_min_touchable_width);
		m_touchableMinCy = context.getResources().getDimensionPixelSize(
				R.dimen.zdlib_min_touchable_height);		
		init(context);
	}

	public LargeTouchAreaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_touchableMinCx = context.getResources().getDimensionPixelSize(
				R.dimen.zdlib_min_touchable_width);
		m_touchableMinCy = context.getResources().getDimensionPixelSize(
				R.dimen.zdlib_min_touchable_height);		
		init(context);
	}

	protected void init(final Context context) {
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if (m_touchDelegateView == null) {
			return;
		}
		Rect recOrig = new Rect();
		m_touchDelegateView.getHitRect(recOrig);
		Rect recDelegate = new Rect(recOrig);
		if (recDelegate.width() < m_touchableMinCx) {
			final int extraCx = (m_touchableMinCx - recDelegate.width())/2;
			recDelegate.left  -= extraCx;
			recDelegate.right += extraCx;
		}
		if (recDelegate.height() < m_touchableMinCy) {
			final int extraCy = (m_touchableMinCy - recDelegate.height())/2;
			recDelegate.top    -= extraCy;
			recDelegate.bottom += extraCy;
		}
		// rectangle for this view relative to itself
		final Rect rec = new Rect(0, 0, r - l, b - t);

		if ((m_touchDelegateFlags & TouchDelegate.TO_RIGHT) != 0) {
			recDelegate.right = rec.right;
		}
		if ((m_touchDelegateFlags & TouchDelegate.TO_LEFT) != 0) {
			recDelegate.left = rec.left;
		}
		if ((m_touchDelegateFlags & TouchDelegate.ABOVE) != 0) {
			recDelegate.top = rec.top;
		}
		if ((m_touchDelegateFlags & TouchDelegate.BELOW) != 0) {
			recDelegate.bottom = rec.bottom;
		}
		m_curRect = recDelegate;
		setTouchDelegate(new TouchDelegate(recDelegate, m_touchDelegateView));
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (isDebug() && m_curRect != null) {
			final Paint paint = new Paint();
			paint.setStyle(Style.FILL);
			paint.setColor(m_debugColor);
			canvas.drawRect(m_curRect, paint);
		}
		super.dispatchDraw(canvas);
	}
	
	protected boolean isDebug() { return m_debug; };
	public void setTouchDelegateDebug(final boolean debug,
			final int color) {
		m_debug = debug;
		m_debugColor = color;
	}
	public void setTouchDelegateDebug(final boolean debug) {
		setTouchDelegateDebug(debug, DEBUG_COLOR);
	}
	
	/**
	 * Set the delegate touch view for this view.
	 * @param view delegate view, checkbox for example
	 * @param flags 0 for defaults or one of more of:
	 * <li>TouchDelegate.TO_RIGHT extends delegate touch area to the parent right bound</>
	 * <li>TouchDelegate.TO_LEFT extend delegate touch area to the parent left bound</>
	 * <li>TouchDelegate.ABOVE extend delegate touch area to the parent top</>
	 * <li>TouchDelegate.BELOW extend delegate touch area to the parent bottom</>
	 */
	public void setTouchDelegateView(final View view, final int flags) {
		m_touchDelegateView = view;
		m_touchDelegateFlags = flags;
	}
	public void setTouchDelegateView(final View view) {
		setTouchDelegateView(view, 0);
	}
}
