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

import java.net.URI;

@SuppressWarnings("serial")
public class HttpClientException extends Exception {
	private String method;
	private URI uri;
	private String requestBody;
	private Integer statusCode;
	private String reasonPhrase;
	private String responseBody;
	
	public HttpClientException(String message, String method, URI uri, String requestBody, Exception e) {
		super(message, e);
		this.method = method;
		this.uri = uri;
		this.requestBody = requestBody;
	}

	public HttpClientException(String message, String method, URI uri, String requestBody, Integer statusCode, String reasonPhrase, String responseBody) {
		super(message);
		this.method = method;
		this.uri = uri;
		this.requestBody = requestBody;
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
		this.responseBody = responseBody;
	}
	
	@Override 
	public String toString() {
		String s = String.format("%s:\n%s on %s\nREQ BODY:%s", super.getMessage(), this.method, this.uri.toString(), this.requestBody );
		s += String.format("\nResponse code:%s (%s)\nRESP BODY:%s", this.statusCode == null ? "null" : this.statusCode.toString(), this.reasonPhrase, this.responseBody);
		return s;
	}

	
	
	public Integer getStatusCode() {
		return statusCode;
	}
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	public String getMethod() {
		return method;
	}

	public URI getUri() {
		return uri;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public String getResponseBody() {
		return responseBody;
	}

}
