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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.InflaterInputStream;

import android.database.Cursor;

import com.zidsoft.zdlib.util.ZDDateUtils;
import com.zidsoft.zdlib.util.ZDStringUtils;


/**
 * Service call that access database with methods for retrieving a resultset
 * columns values, for example.
 * 
 * @author faridz
 *
 */
public abstract class ServiceCallDb extends ServiceCall {

	public static class ColAttr {
		public static final int COMPRESSED = 0x0001;
	}
	
	public ServiceCallDb(ServiceCallCommand command) {
		super(command);
	}

	public ServiceCallDb() {
	}

	protected String deflateBlob(final byte[] blob) throws IOException {
		final InflaterInputStream in = new InflaterInputStream(
				new ByteArrayInputStream(blob));
		final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		int oneByte;
		while ((oneByte = in.read()) > 0) {
			out.write(oneByte);
		}
		final String text = out.toString();
		out.close();
		in.close();
		return text;		
	}
	protected byte[] getBlob(final Cursor cur, final int index) {
		return cur.isNull(index) ? null : cur.getBlob(index);
	}
	protected byte[] getBlob(final Cursor cur, final IResultsetCol col) {
		return cur.getBlob(col.getIndex());
	}
	
	protected String getString(final Cursor cur, final IResultsetCol col) {
		return cur.isNull(col.getIndex()) ? null : cur.getString(col.getIndex());
	}
	
	protected String getLongString(final Cursor cur, final IResultsetCol col,
			final int flags) throws IOException {
		final byte[] blob = getBlob(cur, col);
		return blob == null ? null :
			(flags & ColAttr.COMPRESSED) != 0 ? deflateBlob(blob) :
				new String(blob);
	}
	protected String getLongString(final Cursor cur, final IResultsetCol col)
			throws IOException {
		return getLongString(cur, col, 0);
	}
	
	protected Integer getInteger(final Cursor cur, final IResultsetCol col) {
		return cur.isNull(col.getIndex()) ? null :
			cur.getInt(col.getIndex());
	}
	
	protected Long getLong(final Cursor cur, final int index) {
		return cur.isNull(index) ? null : cur.getLong(index);
	}
	protected Long getLong(final Cursor cur, final IResultsetCol col) {
		return getLong(cur, col.getIndex());
	}
	
	protected Boolean getBoolean(final Cursor cur, final IResultsetCol col) {
		return cur.isNull(col.getIndex()) ? null :
			ZDStringUtils.isTrue(cur.getString(col.getIndex()));
	}
	
	protected Date getDateFromLong(final Cursor cur,
			final IResultsetCol col) throws Exception {
		// example: 19830224
		return ZDDateUtils.getDateFromLong(getLong(cur, col));
	}
	
	protected Date getDateTimeFromLong(final Cursor cur,
			final IResultsetCol col) throws Exception {
		// example: 20120608135700
		return ZDDateUtils.getDateTimeFromLong(getLong(cur, col));
	}
	
}
