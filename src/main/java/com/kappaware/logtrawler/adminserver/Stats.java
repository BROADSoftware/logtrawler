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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.AgentHandler;
import com.kappaware.logtrawler.DirWatcher.Exclusion;
import com.kappaware.logtrawler.EvictionHolder;
import com.kappaware.logtrawler.MFile;
import com.kappaware.logtrawler.Utils;
import com.kappaware.logtrawler.config.Config;

public class Stats {
	static Log log = LogFactory.getLog(Stats.class);

	private List<Agent> agents = new Vector<Agent>();

	public List<Agent> getAgents() {
		return agents;
	}

	public String getNow() {
		return (new Date(System.currentTimeMillis())).toString();
	}

	public enum Mode {
		stats, files, zombies, actives, evictions, all
	}

	static public class Agent {
		private Config.Agent config;
		private int fileCount;
		private int zombieCount;
		private int activeFileCount;
		private int lastEvictionCount;
		private List<MFileView> files;
		private List<MFileView> zombies;
		private List<MFileView> activeFiles;
		private List<EvictionHolder.Eviction> lastEvictions;
		private long maxLineLength;
		private long nbrLineRead;
		private List<Exclusion> exclusions;
		

		public Agent(AgentHandler ah, Mode mode) {
			super();
			this.config = ah.getConfig();
			this.fileCount = ah.getFileByKey().size();
			this.zombieCount = ah.getZombieByKey().size();
			this.activeFileCount = ah.getActiveFiles().size();
			this.lastEvictionCount = ah.getEvictionHolder().getEvictions().size();
			this.exclusions = ah.getExclusions();
			
			if (mode == Mode.files || mode == Mode.all) {
				this.files = new ArrayList<MFileView>(fileCount);
				for (MFile file : ah.getFileByKey().values()) {
					this.files.add(file.getView());
				}
				Collections.sort(this.files, new FilePathComparator());
			}
			if (mode == Mode.zombies || mode == Mode.all) {
				this.zombies = new ArrayList<MFileView>(zombieCount);
				for (MFile file : ah.getZombieByKey().values()) {
					this.zombies.add(file.getView());
				}
				Collections.sort(this.zombies, new FilePathComparator());
			}
			if (mode == Mode.actives || mode == Mode.all) {
				this.activeFiles = new ArrayList<MFileView>(activeFileCount);
				for (MFile file : ah.getActiveFiles()) {
					this.activeFiles.add(file.getView());
				}
				Collections.sort(this.activeFiles, new FileLruComparator());
			}
			if (mode == Mode.evictions || mode == Mode.all) {
				this.lastEvictions = ah.getEvictionHolder().getEvictions();
			}
			this.maxLineLength = ah.getMaxLineLength();
			this.nbrLineRead = ah.getNbrLineRead();
		}

		public Config.Agent getConfig() {
			return config;
		}

		public int getFileCount() {
			return fileCount;
		}

		public int getZombieCount() {
			return zombieCount;
		}

		public int getActiveFileCount() {
			return activeFileCount;
		}

		public int getLastEvictionCount() {
			return lastEvictionCount;
		}

		public List<MFileView> getFiles() {
			return files;
		}

		public List<MFileView> getZombies() {
			return zombies;
		}

		public List<MFileView> getActiveFiles() {
			return this.activeFiles;
		}

		public List<EvictionHolder.Eviction> getLastEvictions() {
			return lastEvictions;
		}

		public long getMaxLineLength() {
			return maxLineLength;
		}

		public long getNbrLineRead() {
			return nbrLineRead;
		}
		
		public List<Exclusion> getExclusions() {
			return exclusions;
		}


	}

	static public class MFileView {
		private MFile file;

		public MFileView(MFile file) {
			super();
			this.file = file;
		}

		public String getPath() {
			return file.getPath().toString();
		}

		public String getKey() {
			return file.getKey().toString();
		}

		public boolean isLog() {
			return file.isLog();
		}

		public String getDeletionTime() {
			return Utils.printIsoDateTime(file.getDeletionTime());
		}

		public String getMimeType() {
			return file.getMimeType();
		}

		public long getPosition() {
			return file.getPosition();
		}

		public String getLastAccessTime() {
			return Utils.printIsoDateTime(file.getLastAccessTime());
		}

		public String getLastFetchTime() {
			return Utils.printIsoDateTime(file.getLastFetchTime());
		}

		public boolean isActive() {
			return this.file.isActive();
		}

		public String getLine() {
			return this.file.getLine();
		}

		public long getNbrCharRead() {
			return file.getNbrCharRead();
		}

		public long getNbrControlCharRead() {
			return file.getNbrControlCharRead();
		}

		public String getComment() {
			return file.getComment();
		}

		public long getMaxLineLength() {
			return file.getMaxLineLength();
		}

		public String getLastLine() {
			return file.getLastLine();
		}

		public long getNbrLineRead() {
			return file.getNbrLineRead();
		}

	}

	public static class FilePathComparator implements Comparator<MFileView> {
		@Override
		public int compare(MFileView o1, MFileView o2) {
			return o1.getPath().compareTo(o2.getPath());
		}
	}

	public static class FileLruComparator implements Comparator<MFileView> {
		@Override
		public int compare(MFileView o1, MFileView o2) {
			long x = o1.file.getLastAccessTime() - o2.file.getLastAccessTime();
			return (x == 0) ? 0 : (x > 0 ? -1 : 1);
		}
	}
}
