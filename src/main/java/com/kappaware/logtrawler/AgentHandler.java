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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.DirWatcher.Exclusion;
import com.kappaware.logtrawler.config.Config;
import com.kappaware.logtrawler.config.ConfigurationException;
import com.kappaware.logtrawler.config.Config.Agent;
import com.kappaware.logtrawler.output.MultiOutputFlow;
import com.kappaware.logtrawler.output.OutputFlow;
import com.kappaware.logtrawler.output.OutputFlowFactory;

public class AgentHandler {
	static Log log = LogFactory.getLog(AgentHandler.class);
	private Config.Agent config;
	private FileEventHandler fileEventHandler;
	private List<DirWatcher> dirWatchers = new Vector<DirWatcher>();

	public AgentHandler(Agent config) throws IOException, ConfigurationException {
		this.config = config;
		if (this.config.getFolders().size() < 1) {
			throw new ConfigurationException(String.format("Agent %s: No folder to monitor!", config.getName()));
		}
		if(config.getOutputFlows() == null || config.getOutputFlows().size() == 0) {
			log.warn(String.format("Agent %s: No output flow configured.", config.getName()));
		}
		OutputFlow outputFlow = new MultiOutputFlow(new OutputFlowFactory(this.config.getOutputMaxBatchSize() > 0), config.getOutputFlows());
		init(outputFlow);
	}

	private void init(OutputFlow outputFlow) throws IOException {
		BlockingSetQueue<FileEvent> queue = new BlockingSetQueueImpl<FileEvent>();
		for (Config.Agent.Folder dir : config.getFolders()) {
			dirWatchers.add(new DirWatcher(dir, queue));
		}
		this.fileEventHandler = new FileEventHandler(queue, config, outputFlow);
	}

	public Config.Agent getConfig() {
		return this.config;
	}

	public Map<FileKey, MFile> getFileByKey() {
		return this.fileEventHandler.getFileByKey();
	}

	public Map<FileKey, MFile> getZombieByKey() {
		return this.fileEventHandler.getZombieByKey();
	}

	public String getName() {
		return this.config.getName();
	}

	public Set<MFile> getActiveFiles() {
		return this.fileEventHandler.getActiveFiles();
	}

	public EvictionHolder getEvictionHolder() {
		return this.fileEventHandler.getEvictionHolder();
	}

	public long getMaxLineLength() {
		return fileEventHandler.getMaxLineLength();
	}

	public long getNbrLineRead() {
		return fileEventHandler.getNbrLineRead();
	}

	public List<Exclusion> getExclusions() {
		List<Exclusion> l = new LinkedList<Exclusion>();
		for (DirWatcher dw : this.dirWatchers) {
			if (dw.getExclusions() != null) {
				l.addAll(dw.getExclusions());
			}
		}
		return l;
	}

}
