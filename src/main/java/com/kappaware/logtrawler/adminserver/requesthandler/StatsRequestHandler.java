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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.AgentHandler;
import com.kappaware.logtrawler.adminserver.AbstractJsonRequestHandler;
import com.kappaware.logtrawler.adminserver.HttpServerException;
import com.kappaware.logtrawler.adminserver.Stats;
import com.kappaware.logtrawler.ipmatcher.IpMatcher;

public class StatsRequestHandler extends AbstractJsonRequestHandler {
	Log log = LogFactory.getLog(StatsRequestHandler.class);

	private Map<String, AgentHandler> agentHandlerByName;

	public StatsRequestHandler(Map<String, AgentHandler> agentHandlerByName, IpMatcher ipMatcher) {
		super(ipMatcher);
		this.agentHandlerByName = agentHandlerByName;
	}

	@Override
	protected Object handleJsonRequest(HttpServletRequest request, String[] groups, HttpServletResponse response) throws HttpServerException {
		if (groups.length == 2) {
			Stats.Mode mode;
			try {
				mode = Stats.Mode.valueOf(groups[1]);
			} catch (Exception e) {
				throw new HttpServerException(HttpServletResponse.SC_NOT_FOUND, String.format("Unknow mode '%s'", groups[1]));
			}
			Stats stats = new Stats();
			if ("*".equals(groups[0])) {
				for(AgentHandler ah : this.agentHandlerByName.values()) {
					stats.getAgents().add(new Stats.Agent(ah, mode));
				}
			} else {
				AgentHandler ah = this.agentHandlerByName.get(groups[0]);
				if (ah != null) {
					stats.getAgents().add(new Stats.Agent(ah, mode));
				} else {
					throw new HttpServerException(HttpServletResponse.SC_NOT_FOUND, String.format("Unknow agent '%s'", groups[0]));
				}
			}
			return stats;
		} else {
			log.error(String.format("groups[].length = %d", groups.length));
			throw new HttpServerException(HttpServletResponse.SC_NOT_FOUND, null);
		}
	}
	
}
