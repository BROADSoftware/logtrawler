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
package com.kappaware.logtrawler.adminserver.requesthandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kappaware.logtrawler.adminserver.AbstractJsonRequestHandler;
import com.kappaware.logtrawler.config.Config;
import com.kappaware.logtrawler.ipmatcher.IpMatcher;

public class ConfigRequestHandler extends AbstractJsonRequestHandler {
	private Config config;

	public ConfigRequestHandler(Config config, IpMatcher ipMatcher) {
		super(ipMatcher);
		this.config = config;
	}

	@Override
	protected Object handleJsonRequest(HttpServletRequest request, String[] groups, HttpServletResponse response) {
		return config;
	}

}
