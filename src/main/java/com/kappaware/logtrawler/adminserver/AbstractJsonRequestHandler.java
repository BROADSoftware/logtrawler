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
package com.kappaware.logtrawler.adminserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

import com.kappaware.logtrawler.Utils;
import com.kappaware.logtrawler.ipmatcher.IpMatcher;

public abstract class AbstractJsonRequestHandler implements RequestHandler {

	protected abstract Object handleJsonRequest(HttpServletRequest request, String[] groups, HttpServletResponse response) throws HttpServerException;
	
	private JSON json = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT);
	private IpMatcher ipMatcher;
	
	public AbstractJsonRequestHandler(IpMatcher ipMatcher) {
		this.ipMatcher = ipMatcher;
	}
	
	
	@Override
	public void handleRequest(HttpServletRequest request, String[] groups, HttpServletResponse response) throws IOException, HttpServerException {
		if(!this.ipMatcher.match(request.getRemoteAddr())) {
			throw new HttpServerException(HttpServletResponse.SC_FORBIDDEN, String.format("Request from %s are not allowed", request.getRemoteAddr()));
		}
		Object o = this.handleJsonRequest(request, groups, response);
		if(o != null) {
			try {
				String jsonResponse = json.asString(o);
				response.setContentType("application/json;charset=UTF-8");
				response.setStatus(HttpServletResponse.SC_OK);
				Utils.setCache(response, 0);
				PrintWriter w = response.getWriter();
				w.print(jsonResponse);
				w.flush();
				w.close();
			} catch (JSONObjectException e) {
				throw new HttpServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to generate a JSON string:" + e.getMessage());
			}
		} else {
			throw new HttpServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Null json object");
		}
		
	}

}
