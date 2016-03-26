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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class AdminHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpMethod method = null;
		try {
			method = HttpMethod.valueOf(request.getMethod());
		} catch (Throwable t) {
			// method will be null. Will be ok for HashMap.get(null)
		}
		try {
			List<HandlerContainer> handlerContainers = this.handlerContainerListByMethod.get(method);
			if (handlerContainers != null) {
				for (HandlerContainer hc : handlerContainers) {
					if (hc.tryToHandleRequest(request, response)) {
						return;
					}
				}
			}
			throw new HttpServerException(HttpServletResponse.SC_NOT_FOUND, null);
		} catch (HttpServerException hse) {
			if(hse.getErrorCode() == HttpServletResponse.SC_NOT_FOUND && hse.getMessage() == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("Invalid path:'%s'", request.getPathInfo()));
			} else {
				response.sendError(hse.getErrorCode(), hse.getMessage());
			}
		}
		baseRequest.setHandled(true);
	}


	private Map<HttpMethod, List<HandlerContainer>> handlerContainerListByMethod = new HashMap<HttpMethod, List<HandlerContainer>>();

	public void register(HttpMethod method, String pathInfoRegex, RequestHandler requestHandler) {
		List<HandlerContainer> handlerContainers = this.handlerContainerListByMethod.get(method);
		if (handlerContainers == null) {
			handlerContainers = new ArrayList<HandlerContainer>();
			this.handlerContainerListByMethod.put(method, handlerContainers);
		}
		handlerContainers.add(new HandlerContainer(requestHandler, pathInfoRegex));
	}

	private static class HandlerContainer {
		private RequestHandler requestHandler;
		private Pattern pattern;

		HandlerContainer(RequestHandler requestHandler, String regex) {
			super();
			this.requestHandler = requestHandler;
			this.pattern = Pattern.compile(regex);
		}

		boolean tryToHandleRequest(HttpServletRequest request, HttpServletResponse response) throws HttpServerException, IOException {
			String p = request.getPathInfo();
			Matcher m = this.pattern.matcher(p);
			if (m.matches()) {
				int gc = m.groupCount();
				String[] groups = new String[gc];
				for (int i = 0; i < gc; i++) {
					groups[i] = m.group(i+1);
				}
				this.requestHandler.handleRequest(request, groups, response);
				return true;
			} else {
				return false;
			}

		}

	}
	
	
}
