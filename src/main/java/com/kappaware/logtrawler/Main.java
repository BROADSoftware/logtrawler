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


package com.kappaware.logtrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.adminserver.AdminServer;
import com.kappaware.logtrawler.config.Config;
import com.kappaware.logtrawler.config.Config.Agent;
import com.kappaware.logtrawler.config.Config.Agent.Folder;
import com.kappaware.logtrawler.config.ConfigurationException;

public class Main {
	static Log log = LogFactory.getLog(Main.class);

	@SuppressWarnings("static-access")
	static public void main(String[] argv) throws Throwable {

		Config config;

		Options options = new Options();

		options.addOption(OptionBuilder.hasArg().withArgName("configFile").withLongOpt("config-file").withDescription("JSON configuration file").create("c"));
		options.addOption(OptionBuilder.hasArg().withArgName("folder").withLongOpt("folder").withDescription("Folder to monitor").create("f"));
		options.addOption(OptionBuilder.hasArg().withArgName("exclusion").withLongOpt("exclusion").withDescription("Exclusion regex").create("x"));
		options.addOption(OptionBuilder.hasArg().withArgName("adminEndpoint").withLongOpt("admin-endpoint").withDescription("Endpoint for admin REST").create("e"));
		options.addOption(OptionBuilder.hasArg().withArgName("outputFlow").withLongOpt("output-flow").withDescription("Target to post result on").create("o"));
		options.addOption(OptionBuilder.hasArg().withArgName("hostname").withLongOpt("hostname").withDescription("This hostname").create("h"));
		options.addOption(OptionBuilder.withLongOpt("displayDot").withDescription("Display Dot").create("d"));
		options.addOption(OptionBuilder.hasArg().withArgName("mimeType").withLongOpt("mime-type").withDescription("Valid MIME type").create("m"));
		options.addOption(OptionBuilder.hasArg().withArgName("allowedAdmin").withLongOpt("allowedAdmin").withDescription("Allowed admin network").create("a"));
		options.addOption(OptionBuilder.hasArg().withArgName("configFile").withLongOpt("gen-config-file").withDescription("Generate JSON configuration file").create("g"));
		options.addOption(OptionBuilder.hasArg().withArgName("maxBatchSize").withLongOpt("max-batch-size").withDescription("Max JSON batch (array) size").create("b"));

		CommandLineParser clParser = new BasicParser();
		CommandLine line;
		String configFile = null;
		try {
			// parse the command line argument
			line = clParser.parse(options, argv);
			if (line.hasOption("c")) {
				configFile = line.getOptionValue("c");
				config = Json.fromJson(Config.class, new BufferedReader(new InputStreamReader(new FileInputStream(configFile))));
			} else {
				config = new Config();
			}
			if (line.hasOption("f")) {
				String[] fs = line.getOptionValues("f");
				// Get the first agent (Create it if needed)
				if (config.getAgents() == null || config.getAgents().size() == 0) {
					Config.Agent agent = new Config.Agent("default");
					config.addAgent(agent);
				}
				Config.Agent agent = config.getAgents().iterator().next();
				for (String f : fs) {
					agent.addFolder(new Config.Agent.Folder(f, false));
				}
			}
			if (line.hasOption("e")) {
				String e = line.getOptionValue("e");
				config.setAdminEndpoint(e);
			}
			if (line.hasOption("o")) {
				String[] es = line.getOptionValues("o");
				if (config.getAgents() != null) {
					for (Agent agent : config.getAgents()) {
						for (String s : es) {
							agent.addOuputFlow(s);
						}
					}
				}
			}
			if (line.hasOption("h")) {
				String e = line.getOptionValue("h");
				config.setHostname(e);
			}
			if (line.hasOption("x")) {
				if (config.getAgents() != null) {
					for (Agent agent : config.getAgents()) {
						if (agent.getFolders() != null) {
							for (Folder folder : agent.getFolders()) {
								String[] exs = line.getOptionValues("x");
								for (String ex : exs) {
									folder.addExcludedPath(ex);
								}
							}
						}
					}
				}
			}
			if (line.hasOption("m")) {
				if (config.getAgents() != null) {
					for (Agent agent : config.getAgents()) {
						String[] exs = line.getOptionValues("m");
						for (String ex : exs) {
							agent.addLogMimeType(ex);
						}
					}
				}
			}
			if (line.hasOption("a")) {
				String[] exs = line.getOptionValues("a");
				for (String ex : exs) {
					config.addAdminAllowedNetwork(ex);
				}
			}
			if (line.hasOption("d")) {
				config.setDisplayDot(true);
			}
			if (line.hasOption("b")) {
				Integer i = getIntegerParameter(line, "b");
				if (config.getAgents() != null) {
					for (Agent agent : config.getAgents()) {
						agent.setOutputMaxBatchSize(i);
					}
				}
			}
			config.setDefault();
			if (line.hasOption("g")) {
				String fileName = line.getOptionValue("g");
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
				out.println(Json.toJson(config, true));
				out.flush();
				out.close();
				System.exit(0);
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			usage(options, exp.getMessage());
			return;
		}

		try {
			// Check config
			if (config.getAgents() == null || config.getAgents().size() < 1) {
				throw new ConfigurationException("At least one folder to monitor must be provided!");
			}
			Map<String, AgentHandler> agentHandlerByName = new HashMap<String, AgentHandler>();
			for (Config.Agent agent : config.getAgents()) {
				agentHandlerByName.put(agent.getName(), new AgentHandler(agent));
			}
			if(!Utils.isNullOrEmpty(config.getAdminEndpoint())) {
				new AdminServer(config, agentHandlerByName);
			}
		} catch (ConfigurationException e) {
			log.error(e.toString());
			System.exit(1);
		} catch (Throwable t) {
			log.error("Error in main", t);
			System.exit(2);
		}
	}

	private static Integer getIntegerParameter(CommandLine line, String opt) throws ParseException {
		String s = line.getOptionValue(opt);
		Integer i;
		try {
			i = new Integer(s);
		} catch (Throwable t) {
			throw new ParseException(String.format("Unable to convert '%s' to a numeric value for option -%s!", s, opt));
		}
		return i;
	}

	private static void usage(Options options, String err) {
		if (err != null) {
			System.out.print(String.format("\nERROR: %s\n\n", err));
		}
		HelpFormatter f = new HelpFormatter();
		f.printHelp("logtrawler options...\n\nOptions:\n\n", options);
		System.exit(1);
	}

}
