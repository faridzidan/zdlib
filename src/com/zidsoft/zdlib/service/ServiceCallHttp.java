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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.zidsoft.zdlib.app.ZDApplication;
import com.zidsoft.zdlib.common.NotConnectedToNetworkException;


/**
 * Service call that uses an http client to access a web service resource.
 * 
 * @author faridz
 *
 */
public abstract class ServiceCallHttp extends ServiceCall {
	protected static final String TAG = ServiceCallHttp.class.getSimpleName(); 
	protected static final String UTF8 = "utf-8";
	/**
	 * Service path preceding service name.
	 */
	protected final String servicePath;
	/**
	 * Service name for the web service. It is included as the last part of the url.
	 */
	protected final String serviceName;
	
	protected static int CONNECT_TIMEOUT_DEFAULT = 60*1000;	// 60 seconds
	protected static int READ_TIMEOUT_DEFAULT = 60*1000;		// 60 seconds
	protected int m_connectTimeout = CONNECT_TIMEOUT_DEFAULT;
	protected int m_readTimeout = READ_TIMEOUT_DEFAULT;
	
	protected class Param {
		final String name;
		final String value;
		public Param(final String name, final String value) {
			this.name = name;
			this.value = value;
		}
	}

	public ServiceCallHttp(final String servicePath, final String serviceName) {
		this.servicePath = servicePath;
		this.serviceName = serviceName;
	}
	
	public ServiceCallHttp(final ServiceCallCommand cmd,
			final String servicePath, final String serviceName) {
		super(cmd);
		this.servicePath = servicePath;
		this.serviceName = serviceName;
	}

	/**
	 * Get the server base url for the service object.
	 * @return server base url. Example: http://google.com/
	 */	
	protected String getServerBaseUrl() {
		return ZDApplication.getInstance().getServerBaseUrl();
	}
	
	/**
	 * Add/set request properties. Implement in descendants as applicable to 
	 * set and add custom headers or properties for the request.
	 * @param urlConnection
	 */
	protected void configureRequestProperties(
			final HttpURLConnection urlConnection) {}
	
	/**
	 * Set the connect timeout. 
	 * @see HttpURLConnection#setConnectTimeout(int)
	 * @param timeoutMillis
	 */
	protected void setConnectTimeout(int timeoutMillis) {
		m_connectTimeout = timeoutMillis;
	}
	
	/**
	 * Set the read timeout for the service call connection. Default is 30 seconds.
	 * <p>Web server may happily accept your connection, but it might be slow 
	 * in actually responding to the request.</p>
	 * @see HttpURLConnection#setReadTimeout(int)
	 * @param timeoutMillis
	 */
	protected void setReadTimeout(int timeoutMillis) {
		m_readTimeout = timeoutMillis;
	}
	
	public boolean isPost() { return false; };
	
	/**
	 * Get payload for post method. Default implementation returns query parameters.
	 * Override in descendants that use POST method, as necessary.
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getPostPayload() throws Exception { 
		return getQuery();
	}
	
	/**
	 * Get the list of parameters for the request.
	 * @return
	 */
	protected abstract List<Param> getParameters() throws Exception;
	protected abstract ServiceCallHttpResult createResult();
	
	/**
	 * Get the get query for the url.
	 * @return null if there are no query parameters.
	 * @throws UnsupportedEncodingException 
	 */
	protected String getQuery() throws Exception {
		final List<Param> params = getParameters();
		if (params == null || params.isEmpty()) {
			return null;
		}
		final ArrayList<String> pieces = new ArrayList<String>(params.size());
		for (final Param param : params) {
			final String value = param.value == null ? "" : param.value;
			final String piece = URLEncoder.encode(param.name, UTF8)
					+ "=" + URLEncoder.encode(value, UTF8);
			pieces.add(piece);
		}
		return TextUtils.join("&", pieces);
	}
	
	protected URL getURL() throws Exception {
		final String query = isPost() ? null : getQuery();
		final String url =  getServerBaseUrl() +
				servicePath + "/" + serviceName +
				(query == null ? "" : "?" + query);
		return new URL(url);
	}
	
	/**
	 * Default implementation of reading input stream reads in stream into
	 * a string. You may override this method in you service object to read
	 * a different contents.
	 * @param urlConnection Not used here, but provided to allow descendants
	 * that override this method access to request headers, etc. You should
	 * handle this reference as read only and attempt to alter the connection.
	 * @param result result object
	 * @throws IOException 
	 */
	protected void readStream(final HttpURLConnection urlConnection,
			final ServiceCallHttpResult result) throws Exception {
		if (result.in != null) {
			final StringBuilder builder = new StringBuilder();
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(result.in, UTF8));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			result.responseString = builder.toString();
		}
	}
	
	@Override
	protected ServiceCallResult doInBackground() {
		final String LOG_TAG = ServiceCallHttp.class.getSimpleName()
				+ ".doInBackground()";
		HttpURLConnection urlConnection = null;
		ServiceCallHttpResult result = createResult();
		try {
			// check for network connectivity
			if (!ZDApplication.getInstance().isConnectedToNetwork()) {
				throw new NotConnectedToNetworkException();
			}
			urlConnection = (HttpURLConnection) getURL().openConnection();
			urlConnection.setConnectTimeout(m_connectTimeout);
			urlConnection.setReadTimeout(m_readTimeout);
			configureRequestProperties(urlConnection);
			Log.i(LOG_TAG, urlConnection.toString());
			if (isPost()) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("POST");
				final String payload = getPostPayload();
				Log.i(LOG_TAG + " Post Payload", payload);
//				urlConnection.setFixedLengthStreamingMode(payload.getBytes().length);
				final OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
				out.write(payload);
				out.close();
			} else {
				urlConnection.connect();
			}
			result.responseCode = urlConnection.getResponseCode();
			result.returnInfo = result.new ReturnInfo(
					new ServiceCallHttpResult.ResponseCode(result.responseCode),
					urlConnection.getResponseMessage());			
			if (result.responseCode == 200) {
				// success
				result.in = new BufferedInputStream(urlConnection.getInputStream());				
				try {
					readStream(urlConnection, result);
				} catch (IOException e) {
					result.setResult(e);
					return result;
				} finally {
					result.in.close();
				}
			} else {
				Log.e(TAG, result.getMessage("http request failed. Response Code: "
						+ result.responseCode));
			}
		} catch (IOException e) {
			e.printStackTrace();
			result.setResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return result;
	}

}
