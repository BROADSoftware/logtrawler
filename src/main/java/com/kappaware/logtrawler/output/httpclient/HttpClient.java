/*
 * Copyright (C) 2016 BROADSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kappaware.logtrawler.output.httpclient;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClient {
	static Log log = LogFactory.getLog(HttpClient.class);

	public enum SslHandling {
		NONE,			// SSL will not be supported
		RELAX_SSL,		// Ensure encryption, but allow invalid server certificate
		STRICT_SSL
	}

	public enum On404 {
		ERROR,				// Throw an exception
		NULL_ON_GET			// Return null on GET. Exception for other method
	}
	
	private CloseableHttpClient httpclient;
	private On404 on404;
	
	
	public HttpClient(SslHandling sslHandling, On404 on404) {
		this.on404 = on404;
		if(sslHandling == SslHandling.NONE) {
			httpclient = HttpClients.custom().build();
		} else {
		try {
			SSLConnectionSocketFactory sslConnectionSocketFactory;
			SSLContext sslContext = SSLContext.getInstance("TLS");
			if (sslHandling == SslHandling.STRICT_SSL) {
				sslContext.init(null, null, null);
				sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new BrowserCompatHostnameVerifier());
			} else {
				sslContext.init(null, new TrustManager[] { new PassthroughTrustManager() }, null);
				sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());
			}
			httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
		} catch (Exception e) {
			throw new RuntimeException("Exception in SSL configuration", e);
		}
		}
	}

	private static class PassthroughTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	public String exec(HttpRequestBase request, ContentType contentType, String requestData, List<Header> headers) throws HttpClientException {
		if (headers != null) {
			for (Header h : headers) {
				request.setHeader(h);
			}
		}
		if(requestData != null) {
			((HttpEntityEnclosingRequestBase)request).setEntity(new StringEntity(requestData, contentType));
		}
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 404 && this.on404 == On404.NULL_ON_GET && request.getMethod().equals("GET")) {
				return null;
			}
			if (statusCode >= 200 && statusCode < 300) {
				return getEntity(response);
			} else {
				request.getURI();
				throw new HttpClientException("Unexpected HTTP Response code", request.getMethod(), request.getURI(), requestData, 
						response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), getEntity(response));
			}
		} catch (Exception e) {
			if(e instanceof HttpClientException) {
				throw (HttpClientException)e;
			} else {
				throw new HttpClientException("Exception in HTTP Client", request.getMethod(), request.getURI(), requestData, e);
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
	}


	private static String getEntity(CloseableHttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			return EntityUtils.toString(entity);
		} else {
			return null;
		}
	}
	
	
	public String post(String endpoint, ContentType contentType, String requestData, List<Header> headers) throws HttpClientException {
		HttpPost httpPost = new HttpPost(endpoint);
		return this.exec(httpPost, contentType, requestData, headers);
	}

	public String put(String endpoint, ContentType contentType, String requestData, List<Header> headers) throws HttpClientException {
		HttpPut httpPut = new HttpPut(endpoint);
		return this.exec(httpPut, contentType, requestData, headers);
	}
	
	public String get(String endpoint, List<Header> headers) throws HttpClientException {
		HttpGet httpGet = new HttpGet(endpoint);
		return this.exec(httpGet, null, null, headers);
	}

	public void delete(String endpoint, List<Header> headers) throws HttpClientException {
		HttpDelete httpDelete = new HttpDelete(endpoint);
		this.exec(httpDelete, null, null, headers);
	}

}
