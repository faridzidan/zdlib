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

package com.zidsoft.zdlib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class ZDDateUtils {
	protected static String DEBUG_TAG = ZDDateUtils.class.getSimpleName(); 
	public static final String DATE_LONG_SIMPLEDATEFORMAT = "yyyyMMdd";
	public static final String DATETIME_LONG_SIMPLEDATEFORMAT = "yyyyMMddHHmmss";
	/**
	 * Format as a date.
	 */
	public static final int DATE = 0x0001;
	/**
	 * Format as a datetime.
	 */
	public static final int DATETIME = 0x0002;
	/**
	 * Format date as time only.
	 */
	public static final int TIME = 0x0004;
	/**
	 * Replace a null date with its display string (such as --/--/----)
	 */
	public static final int NULL_REPLACEMENT = 0x0008;
	/**
	 * Do not include year in the formatted string if year is the same as
	 * current year.
	 */
	public static final int SPARSE_YEAR = 0x0010;

	// /**
	// * Format the year as 4 digits rather than two.
	// */
	// public static final int FULL_YEAR = 0x0020;

	private ZDDateUtils() {
	}

	public static String formatDate(final Date date, final int flags) {
		if (date == null) {
			return (flags & NULL_REPLACEMENT) == 0 ? "" : "--/--/----";
		} else {
			// M/d/yy
			// M/d/yyyy
			// h:mm a
			// M/d/yyyy h:mm a
			// E M/d/yyyy h:mm a
			final boolean omitYear = (flags & SPARSE_YEAR) != 0
					&& date.getYear() == new Date().getYear();
			final boolean fullYear = true; // (flags & FULL_YEAR) != 0;
			final String format = (flags & DATE) != 0 ? omitYear ? "M/d"
					: fullYear ? "M/d/yyyy" : "M/d/yy"
					: (flags & DATETIME) != 0 ? omitYear ? "M/d h:mm a"
							: fullYear ? "M/d/yyyy h:mm a" : "M/d/yy h:mm a"
							: (flags & TIME) != 0 ? "h:mm a"
									: "M/d/yyyy h:mm a";

			return new SimpleDateFormat(format).format(date);
		}
	}

	/**
	 * Returns new Date object with time removed
	 * 
	 * @param date
	 *            Date object with time
	 * @return Date object with 0 hours, minutes, seconds, and milliseconds
	 */
	public static Date getDateWithoutTime(Date date) {
		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * @see #getDateTimeFromLong(Long)
	 */
	public static Date getDateTimeFromLongSafe(final String longDateTime) {
		Date date = null;
		try {
			date = getDateTimeFromLong(longDateTime);
		} catch (ParseException e) {
			Log.e(DEBUG_TAG, "Failuring to parse long date/time String: "
					+ longDateTime + " with exception: "
					+ e.getLocalizedMessage());
		}
		return date;
	}
	
	/**
	 * @see #getDateFromLong(Long)
	 */
	public static Date getDateFromLongSafe(final String longDate) {
		Date date = null;
		try {
			date = getDateFromLong(longDate);
		} catch (ParseException e) {
			Log.e(DEBUG_TAG, "Failuring to parse long date String: "
					+ longDate + " with exception: "
					+ e.getLocalizedMessage());
		}
		return date;		
	}
	/**
	 * Parses date/time string encoded long into a Date object.
	 * Example input: 20120608135700
	 * 
	 * @param longDateTime
	 *            String containing date/time in long format
	 * @return Date object representing the date/time in the long string
	 * @throws ParseException
	 *             if invalid date/time long string provided
	 */
	public static Date getDateTimeFromLong(final String longDateTime)
			throws ParseException {
		// example: 20120608135700
		return longDateTime == null ? null : new SimpleDateFormat(
				DATETIME_LONG_SIMPLEDATEFORMAT).parse(longDateTime);
	}
	/**
	 * @see #getDateFromLong(String)
	 */
	public static Date getDateTimeFromLong(final Long longDateTime)
			throws ParseException {
		// example: 20120608135700
		return longDateTime == null ? null :
				getDateTimeFromLong(String.valueOf(longDateTime));
	}
	
	/**
	 * Create a date object from the given long encoded date.
	 * Example input: 20120608
	 * @param longDate
	 * @return Date object
	 * @throws ParseException
	 */
	public static Date getDateFromLong(final Long longDate)
			throws ParseException {
		// example: 20120608
		return longDate == null ? null :
			getDateFromLong(String.valueOf(longDate));
	}
	
	/**
	 * @see #getDateFromLong(Long)
	 */
	public static Date getDateFromLong(final String longDate)
			throws ParseException {
		// example: 20120608
		return longDate == null ? null : new SimpleDateFormat(
				DATE_LONG_SIMPLEDATEFORMAT).parse(longDate);
	}	
}
