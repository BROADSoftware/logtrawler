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
package com.kappaware.logtrawler.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.kappaware.logtrawler.Json;
import com.kappaware.logtrawler.Utils;

public class Config {
	private String hostname;
	private String rhostname;
	private Set<Agent> agents;
	private String adminEndpoint;
	private List<String> adminAllowedNetwork; // TODO
	private Boolean displayDot;

	public void setDefault() {
		if(this.agents != null) {
			for(Agent agent : agents) {
				agent.setDefault(this);
			}
		}
		if(this.hostname == null) {
			this.setHostname(Utils.getHostname());
		}
		if(this.displayDot == null) {
			this.displayDot = false;
		}
		if(this.adminAllowedNetwork == null) {
			this.addAdminAllowedNetwork("127.0.0.1/32");
		}
	}

	public void addAgent(Agent agent) throws ConfigurationException {
		if (this.agents == null) {
			this.agents = new LinkedHashSet<Agent>();
		}
		if(this.agents.contains(agent)) {
			throw new ConfigurationException(String.format("Two agent with same name:'%s'", agent.getName()));
		} else {
			this.agents.add(agent);
		}
	}
	
	public void addAdminAllowedNetwork(String n) {
		if (this.adminAllowedNetwork == null) {
			this.adminAllowedNetwork = new Vector<String>();
		}
		this.adminAllowedNetwork.add(n);
	}

	public Collection<Agent> getAgents() {
		return agents;
	}

	public void setAgents(Collection<Agent> agents) throws ConfigurationException {
		this.agents = new LinkedHashSet<Agent>();
		for(Agent agent : agents) {
			if(this.agents.contains(agent)) {
				throw new ConfigurationException(String.format("Two agent with same name:'%s'", agent.getName()));
			} else {
				this.agents.add(agent);
			}
		}
	}

	public String getAdminEndpoint() {
		return adminEndpoint;
	}

	public void setAdminEndpoint(String adminEndpoint) {
		this.adminEndpoint = adminEndpoint;
	}

	public List<String> getAdminAllowedNetwork() {
		return adminAllowedNetwork;
	}

	public void setAdminAllowedNetwork(List<String> adminAllowedNetwork) {
		this.adminAllowedNetwork = adminAllowedNetwork;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		String[] h = hostname.split("\\.");
		StringBuffer sb = new StringBuffer();
		String sep = "";
		for(int i = h.length - 1; i >= 0; i--) {
			sb.append(sep);
			sb.append(h[i]);
			sep = ".";
		}
		this.rhostname = sb.toString();
	}
	

	public void setRhostname(String rhostname) {
		// DO NOTHING. See setHostname()
	}

	public String getRhostname() {
		return rhostname;
	}

	public Boolean getDisplayDot() {
		return displayDot;
	}

	public void setDisplayDot(Boolean displayDot) {
		this.displayDot = displayDot;
	}
	
	// -----------------------------------------------------

	public static class Agent {
		private Config config;
		private String name;
		// The folders to watch
		private Set<Folder> folders;
		// Valid log mime types
		private Set<String> logMimeTypes;
		private Long zombieTtl;
		private Integer maxActiveFiles;
		private Long activeFileTimeout;
		private Boolean pushInitialContent;
		private Integer maxLastEvictions;
		private Integer maxLineLength;	// If a line exceed this length, then the file is flagged as non-log
		private Integer maxControlCharPercent;		// If the ratio of control char exceed this value (in percent of all character), this the file is flagged as non-log
		private Set<String> outputFlows;
		private Integer outputMaxBatchSize;		// 0: No batch (One simple item per post)

		public Agent() {
			this.name = "default";
		}
		
		public Agent(String name) {
			this.name = name;
		}
		
		public void setDefault(Config config) {
			this.config = config;
			if (this.name == null) {
				this.setName("default");
			}
			if (this.logMimeTypes == null) {
				this.addLogMimeType("text/plain");
				this.addLogMimeType("text/x-log");
				this.addLogMimeType("application/octet-stream");
			}
			if (this.folders == null) {
				this.folders = new LinkedHashSet<Folder>();
			}
			if (this.zombieTtl == null) {
				this.zombieTtl = 5L; 
			}
			if (this.maxActiveFiles == null) {
				this.maxActiveFiles = 16;	
			}
			if (this.activeFileTimeout == null) {
				this.activeFileTimeout = 600L; // 10 mn 
			}
			if (this.pushInitialContent == null) {
				this.pushInitialContent = false; 
			}
			if(this.maxLastEvictions == null) {
				this.maxLastEvictions = 16;
			}
			if(this.maxLineLength == null) {
				this.maxLineLength = Integer.MAX_VALUE;
			}
			if(this.maxControlCharPercent == null) {
				this.maxControlCharPercent = 10;
			}
			if(this.outputFlows == null) {
				this.outputFlows = new LinkedHashSet<String>(); // Empty list
			}
			if(this.outputMaxBatchSize == null) {
				this.outputMaxBatchSize = 0;
			}
		}
		
		public void addFolder(Folder folder) throws ConfigurationException {
			if(this.folders == null) {
				this.folders = new LinkedHashSet<Folder>();
			}
			if(this.folders.contains(folder)) {
				throw new ConfigurationException(String.format("Two folder defined with same path: '%s'", folder.getPath()));
			} else {
				this.folders.add(folder);
			}
		}
		
		public void addLogMimeType(String mimeType) {
			if(this.logMimeTypes == null) {
				this.logMimeTypes = new LinkedHashSet<String>();
			}
			this.logMimeTypes.add(mimeType);
		}
		
		public void addOuputFlow(String outf) {
			if(this.outputFlows == null) {
				this.outputFlows = new LinkedHashSet<String>();
			}
			this.outputFlows.add(outf);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Collection<Folder> getFolders() {
			return folders;
		}

		public void setFolders(Collection<Folder> folders) throws ConfigurationException {
			this.folders = new LinkedHashSet<Folder>();
			for(Folder f : folders) {
				if(this.folders.contains(f)) {
					throw new ConfigurationException(String.format("Two folder defined with same path: '%s'", f.getPath()));
				} else {
					this.folders.add(f);
				}
			} 
		}

		public Collection<String> getLogMimeTypes() {
			return logMimeTypes;
		}

		public void setLogMimeTypes(Collection<String> logMimeTypes) {
			this.logMimeTypes = new LinkedHashSet<String>(logMimeTypes);
		}

		public Long getZombieTtl() {
			return zombieTtl;
		}

		public void setZombieTtl(Long zombieTtl) {
			this.zombieTtl = zombieTtl;
		}

		public Integer getMaxActiveFiles() {
			return maxActiveFiles;
		}

		public void setMaxActiveFiles(Integer maxActiveFiles) {
			this.maxActiveFiles = maxActiveFiles;
		}

		public Long getActiveFileTimeout() {
			return activeFileTimeout;
		}

		public void setActiveFileTimeout(Long activeFileTimeout) {
			this.activeFileTimeout = activeFileTimeout;
		}

		public Boolean getPushInitialContent() {
			return pushInitialContent;
		}

		public void setPushInitialContent(Boolean pushInitialContent) {
			this.pushInitialContent = pushInitialContent;
		}
		


		public Integer getMaxLastEvictions() {
			return maxLastEvictions;
		}

		public void setMaxLastEvictions(Integer maxEvictions) {
			this.maxLastEvictions = maxEvictions;
		}

		public Integer getMaxLineLength() {
			return maxLineLength;
		}

		public void setMaxLineLength(Integer maxLineLength) {
			this.maxLineLength = maxLineLength;
		}

		public Integer getMaxControlCharPercent() {
			return maxControlCharPercent;
		}

		public void setMaxControlCharPercent(Integer maxControlCharPercent) {
			this.maxControlCharPercent = maxControlCharPercent;
		}


		public Collection<String> getOutputFlows() {
			return outputFlows;
		}

		public void setOutputFlows(Collection<String> outputFlows) {
			this.outputFlows = new LinkedHashSet<String>(outputFlows);
		}

		
		public Integer getOutputMaxBatchSize() {
			return outputMaxBatchSize;
		}

		public void setOutputMaxBatchSize(Integer outputMaxBatchSize) {
			this.outputMaxBatchSize = outputMaxBatchSize;
		}

		public String fetchHostname() {
			return this.config.hostname;
		}

		public String fetchRhostname() {
			return this.config.rhostname;
		}
		
		public boolean fetchDisplayDot() {
			return this.config.getDisplayDot();
		}


		@Override
		public boolean equals(Object other) {
			return this.name.equals(((Agent)other).name);
		}
		
		public int hashCode() {
			return this.name.hashCode();
		}
		
		public static class Folder {
			private String path;
			private boolean followLink;
			private Set<String> excludedPaths;			// Regex to exclude file. applied on absolute path

			public Folder() {
				this.path = "/dev/null";	// Should never be used.
			}

			public void addExcludedPath(String excludedPath) {
				if(this.excludedPaths == null) {
					this.excludedPaths = new LinkedHashSet<String>();
				}
				this.excludedPaths.add(excludedPath);
			}
			
			public Folder(String path, boolean followLink) {
				this.path = path;
				this.followLink = followLink;
			}

			public void setPath(String path) {
				this.path = path;
			}
			public String getPath() {
				return path;
			}


			public void setFollowLink(boolean followLink) {
				this.followLink = followLink;
			}

			public boolean getFollowLink() {
				return followLink;
			}

			public Collection<String> getExcludedPaths() {
				return excludedPaths;
			}

			public void setExcludedPaths(Collection<String> excludedPaths) {
				this.excludedPaths = new LinkedHashSet<String>(excludedPaths);
			}

			
			@Override
			public boolean equals(Object other) {
				return this.path.equals(((Folder)other).path);
			}
			
			@Override
			public int hashCode() {
				return this.path.hashCode();
			}
		
		}



	}

	// ------------------------------------------------------------------------------

	// To have a template
	public static void main(String[] argv) throws ConfigurationException {
		Config config = new Config();
		config.addAgent(new Agent("default"));
		config.getAgents().iterator().next().addFolder(new Config.Agent.Folder("/var/log", false));
		config.setDefault();

		System.out.println(Json.toJson(config, true));

	}

}
