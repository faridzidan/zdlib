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

/**
 * Presenter is an intermediary between a model and view and in this context
 * typically listens to model notifications and pass the relevant notifications
 * to the view. Usually when the view is not present, the presenter is not listening.
 * 
 * @author faridz
 *
 * @param <IM>
 * @param <IV>
 */
public interface IPresenter<IM, IV> extends PropertyChangeListener {

	IM getModel();
	IV getView();
	
	public void onViewAttached();
	public void onViewDetached();
}
