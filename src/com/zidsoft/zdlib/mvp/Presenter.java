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

import android.content.Context;


/**
 * Base presenter class. Presenter may optionally listen to model changes by
 * adding the presenter to the model change listeners, usually done, when presenter
 * creates the model.
 * @author faridz
 *
 * @param <IM> model interface
 * @param <IV> view interface
 * @param <IP> presenter interface
 */
public abstract class Presenter<
	IM extends IModel, 
	IV extends IView, 
	IP extends IPresenter<IM, IV>> implements IPresenter<IM, IV> {
	
	protected IM m_model;
	protected IV m_view;
	
	@Override
	public IM getModel() {
		return m_model;
	}

	@Override
	public IV getView() {
		return m_view;
	}
	
	@Override
	public void onViewAttached() {
		m_model.addModelChangeListener((PropertyChangeListener) this);		
	}
	
	@Override
	public void onViewDetached() {
		m_model.removeModelChangeListener((PropertyChangeListener) this);
	}
	
	protected Context getContext() {
		return getView().getContext();
	}
}
