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

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import com.zidsoft.zdlib.service.ServiceCallHttpResult.ResponseCode;

/**
 * Base class for {@link ServiceCall} result.
 * 
 * @author faridz
 *
 */
public class ServiceCallResult {

	/**
	 * Helper class that captures the number of rows affected by a delete or an update
	 * and where success is determined by having exactly 1 row affected.
	 * @author faridz
	 *
	 */
	public static class CountAffectedExpectOne {
		public final int count;
		public CountAffectedExpectOne(final int count) {
			this.count = count;
		}
	}

	public static class CountAffectedExpectExactly {
		public final int countAffected;
		public final int countExpected;
		public CountAffectedExpectExactly(final int countAffected,
				final int countExpected) {
			this.countAffected = countAffected;
			this.countExpected = countExpected;
		}
		
		public Exception makeDeleteException() {
			return new Exception("Expected to delete " + countExpected + 
					" row(s). Number of rows affected " + countAffected);
		}
	}
	
	/**
	 * Helper class that captures the number of rows affected by a delete or an update
	 * and where success is determined by having at least one or more rows affected.
	 * @author faridz
	 *
	 */	
	public static class CountAffectedExpectOneOrMore {
		public final int count;
		public CountAffectedExpectOneOrMore(final int count) {
			this.count = count;
		}
	}
	
	public class ReturnInfo extends Throwable {
		public static final int SQL_NO_DATA_FOUND = 100;
		public static final int CANCELED = -99;
		public int returnCode;
		public String message;
		
		public ReturnInfo(final int returnCode) {
			this.returnCode = returnCode;
		}
		
		public ReturnInfo(final int returnCode, final String message) {
			this.returnCode = returnCode;
			this.message = message;
		}
		
		public ReturnInfo(final CountAffectedExpectOne affected) {
			switch(affected.count) {
			case 1:
				// success
				this.returnCode = 0;
				this.message = null;
				break;
				
			case 0:
				// no rows were affected
				this.returnCode = SQL_NO_DATA_FOUND;
				this.message = null;
				break;
				
			default:
				this.returnCode = -1;
				this.message = MessageFormat.format(
						"Number of rows affected {0, number} is more tha one row",
						new Object[]{Integer.valueOf(affected.count)});
				break;
			}
		}

		public ReturnInfo(final CountAffectedExpectExactly info) {
			if (info.countAffected == info.countExpected) {
				// success
				this.returnCode = 0;
				this.message = null;
			} else if (info.countAffected == 0) {
				// no rows were affected
				this.returnCode = SQL_NO_DATA_FOUND;
				this.message = null;
			} else {
				this.returnCode = -1;
				this.message = MessageFormat.format(
						"Expected {1, number} rows to be affected; number of" +
				" rows affected was {0, number}",
						new Object[]{Integer.valueOf(info.countAffected),
									 Integer.valueOf(info.countExpected)});
			}
		}
		
		public ReturnInfo(final CountAffectedExpectOneOrMore affected) {
			if (affected.count >= 1) {
				// success
				this.returnCode = 0;
				this.message = null;
			} else if (affected.count == 0) {
				// no rows were affected
				this.returnCode = SQL_NO_DATA_FOUND;
				this.message = null;
			} else {
				this.returnCode = -1;
				this.message = MessageFormat.format(
						"Number of rows affected {0, number} is not one or more rows",
						new Object[]{Integer.valueOf(affected.count)});
			}
		}
		
		public ReturnInfo(final ResponseCode responseCode, 
				final String responseMessage) {
			if (responseCode.value == 200) {
				this.returnCode = 0;
				this.message = null;
			} else {
				this.returnCode = -1;
				this.message = MessageFormat.format(
						"HTTP responseCode: {0, number}, {1}", 
						new Object[]{Integer.valueOf(responseCode.value),
								responseMessage});
			}
		}
		
		public boolean isCanceled() {
			return returnCode == CANCELED;
		}
		
		public void setCanceled() {
			this.returnCode = CANCELED;
			this.message = "Canceled";
		}
	}
	
	public Exception exception = null;
	public ReturnInfo returnInfo;
	
	public ServiceCallResult() {
		this.exception = null;
		this.returnInfo = null;
	}
	
	public ServiceCallResult(final int returnCode) {
		this.returnInfo = new ReturnInfo(returnCode);
	}
	
	public ServiceCallResult(final int returnCode,
			final String message) {
		this.returnInfo = new ReturnInfo(returnCode, message);
	}
	
	public ServiceCallResult(InterruptedException e) {
	     exception = e;
	     returnInfo = new ReturnInfo(-1, e.toString());
	}
	
	public ServiceCallResult(ExecutionException e) {
		this.exception = e;
		returnInfo = new ReturnInfo(-1, e.toString());
	} 
	
	public ServiceCallResult(final Exception e) {
		this.exception = e;
		returnInfo = new ReturnInfo(-1, this.exception.toString());
	}
	
	public ServiceCallResult(Throwable t) {
		setResult(t);
	}

	public boolean isCanceled() {
		return returnInfo != null && returnInfo.returnCode == ReturnInfo.CANCELED;
	}
	
	public void setCanceled() {
		if (returnInfo == null) {
			this.returnInfo = new ReturnInfo(ReturnInfo.CANCELED, "Canceled");
		} else {
			this.returnInfo.setCanceled();			
		}
	}
	public void setResult(final ServiceCallResult res) {
		this.exception = res.exception;
		this.returnInfo = res.returnInfo;		
	}

	public void setResult(final ReturnInfo returnInfo) {
		this.exception = null;
		this.returnInfo = returnInfo;		
	}
	
	public void setResult(Exception e) {
		this.exception = e;
		returnInfo = new ReturnInfo(-1, this.exception.toString());
	}
	
	public void setResult(Throwable t) {
		this.exception = new Exception(t);
		returnInfo = new ReturnInfo(-1, this.exception.toString());
	}
	
	/**
	 * Get message for the result object.
	 * @param defaultMsg default message to return in case object has no message.
	 * @return result message, if any, or the default given message if result object
	 * does not have a message to return.
	 */
	public String getMessage(final String defaultMsg) {
		
		return returnInfo.message == null || returnInfo.message.trim().length() == 0 ?
				defaultMsg : returnInfo.message;
	}
}
