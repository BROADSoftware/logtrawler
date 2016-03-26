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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Used only for stats. Looking at eviction provide an indication if we need to increase number of active file
 * @author sa
 *
 */

public class EvictionHolder {
	private LinkedList<Eviction> evictions;
	private int maxSize;
	
	public EvictionHolder(int maxSize) {
		this.maxSize = maxSize;
		this.evictions = new LinkedList<Eviction>();
	}
	
	public void add(String path, long ts) {
		evictions.addLast(new Eviction(path, ts));
		while(evictions.size() > maxSize) {
			evictions.removeFirst();
		}
	}
	
	public List<Eviction> getEvictions() {
		return this.evictions;
	}
	
	static public class Eviction {
		private String path;
		private long timestamp;
		
		public Eviction(String path, long timestamp) {
			super();
			this.path = path;
			this.timestamp = timestamp;
		}

		public String getPath() {
			return path;
		}

		public String getTimestamp() {
			return (new Date(this.timestamp)).toString();
		}
	}

}
