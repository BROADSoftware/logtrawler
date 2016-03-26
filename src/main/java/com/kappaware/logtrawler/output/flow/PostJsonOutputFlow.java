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
package com.kappaware.logtrawler.output.flow;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;

import com.kappaware.logtrawler.Json;
import com.kappaware.logtrawler.output.httpclient.HttpClient;
import com.kappaware.logtrawler.output.httpclient.HttpClient.On404;
import com.kappaware.logtrawler.output.httpclient.HttpClient.SslHandling;
import com.kappaware.logtrawler.output.httpclient.HttpClientException;

/**
 * WARNING: This class will lock the output process in case of error. 
 * 
 * Temporary solution, relying on the behavior of collector
 * 
 * @author sa
 *
 */

public class PostJsonOutputFlow extends AbstractJsonOutputFlow {
	static Log log = LogFactory.getLog(PostJsonOutputFlow.class);

	private String endpoint;
	private ContentType contentType = ContentType.APPLICATION_JSON;
	private HttpClient httpClient = null;

	public PostJsonOutputFlow(String endpoint, boolean outputArray) {
		super(outputArray);
		this.endpoint = endpoint;
		log.info(String.format("Events will be POSTed to '%s'", endpoint));
	}

	@Override
	protected void doOutput(Object obj) {
		if (this.httpClient == null) {
			this.httpClient = new HttpClient(SslHandling.NONE, On404.ERROR);
		}
		boolean success;
		do {
			try {
				String r = this.httpClient.post(this.endpoint, this.contentType, Json.toJson(obj, false), null);
				log.debug(String.format("Return from '%s' : '%s'", this.endpoint, r));
				success = true;
			} catch (HttpClientException e) {
				if (e.getStatusCode() == null || e.getStatusCode() == HttpServletResponse.SC_SERVICE_UNAVAILABLE) {
					// Server does not respond, or is temporary Unavailable	
					log.error(String.format("Unable to connect to '%s'. Will retry", this.endpoint), e);
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
					}
					success = false;
				} else {
					log.error(String.format("Error connecting to '%s'. Message dropped", this.endpoint), e);
					success = true;
				}
			}
		} while (!success);
	}



}
