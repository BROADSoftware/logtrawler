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

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;

import com.kappaware.logtrawler.AgentHandler;
import com.kappaware.logtrawler.adminserver.requesthandler.ConfigRequestHandler;
import com.kappaware.logtrawler.adminserver.requesthandler.StatsRequestHandler;
import com.kappaware.logtrawler.config.Config;
import com.kappaware.logtrawler.config.ConfigurationException;
import com.kappaware.logtrawler.ipmatcher.IpMatcherImpl;

public class AdminServer {
	static Log log = LogFactory.getLog(AdminServer.class);

	private Server server;

	public AdminServer(Config config, Map<String, AgentHandler> agentHandlerByName) throws Exception {
		InetSocketAddress bindAddr = null;
		try {
			String[] endp = config.getAdminEndpoint().split(":");
			if (endp.length != 2) {
				throw new Exception("");
			}
			int port = Integer.parseInt(endp[1]);
			bindAddr = new InetSocketAddress(endp[0], port);

		} catch (Throwable t) {
			throw new ConfigurationException(String.format("Missing or invalid admin endpoint:%s", config.getAdminEndpoint()));
		}

		this.server = new Server(bindAddr);

		IpMatcherImpl ipMatcher = new IpMatcherImpl();
		for(String segmentDef : config.getAdminAllowedNetwork()) {
			ipMatcher.addSegment(segmentDef);
		}
		AdminHandler adminHandler = new AdminHandler();
		adminHandler.register(HttpMethod.GET, "/config", new ConfigRequestHandler(config, ipMatcher));
		adminHandler.register(HttpMethod.GET, String.format("/([\\w]+)/(%s|%s|%s|%s|%s|%s)", Stats.Mode.all, Stats.Mode.stats, Stats.Mode.zombies, Stats.Mode.actives, Stats.Mode.files, Stats.Mode.evictions), new StatsRequestHandler(agentHandlerByName, ipMatcher));
		server.setHandler(adminHandler);

		server.start();
		AbstractNetworkConnector anc = (AbstractNetworkConnector) (server.getConnectors()[0]);
		log.info(String.format("Admin REST server bound at %s:%d", anc.getHost(), anc.getLocalPort()));
	}

	public void kill() throws Exception {
		if (this.server != null) {
			this.server.stop();
			this.server.join();
		}
	}

}
