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
package com.kappaware.logtrawler.output;

import com.kappaware.logtrawler.MFile;
import com.kappaware.logtrawler.Utils;
import com.kappaware.logtrawler.config.Config;

public class OutputItem {

	// @formatter:off
	public enum Type {
		FILE_INIT, 
		FILE_CREATE, 
		FILE_DELETE, 
		FILE_RENAME, 
		FILE_TRUNCATE, 
		FILE_REJECT,
		DIR_EXCLUSION,
		FILE_EXCLUSION,
		DATA_LINE, 
		ERROR
	}
	// @formatter:on

	private Type type;
	private String hostname;
	private String rhostname;
	private String agent;
	private String file;
	private String filekey;
	private Long position;
	private Long otimestamp;
	private String data;
	private Boolean islog;
	private String mimetype;

	public OutputItem(Config.Agent agent, Type type, MFile file, String data, long timestamp) {
		this.hostname = agent.fetchHostname();
		this.rhostname = agent.fetchRhostname();
		this.agent = agent.getName();
		this.type = type;
		this.file = file.getPath().toString();
		this.filekey = file.getKey().toString();
		this.position = file.getPosition();
		this.data = data;
		if (type != Type.DATA_LINE) {
			this.islog = file.isLog();
			this.mimetype = file.getMimeType();
		}
		this.otimestamp = timestamp;
	}

	public OutputItem(Config.Agent agent, Type type, String filePath, String data, long timestamp) {
		this.hostname = agent.fetchHostname();
		this.agent = agent.getName();
		this.type = type;
		this.file = filePath;
		this.data = data;
		this.otimestamp = timestamp;
	}

	public String getHostname() {
		return hostname;
	}

	public String getRhostname() {
		return rhostname;
	}

	public String getAgent() {
		return agent;
	}

	public Type getType() {
		return type;
	}

	public String getFile() {
		return file;
	}

	public String getFilekey() {
		return filekey;
	}

	public Long getPosition() {
		return position;
	}

	public String getOtimestamp() {
		return Utils.printIsoDateTime(this.otimestamp);
	}

	public Boolean getIslog() {
		return islog;
	}

	public String getMimetype() {
		return mimetype;
	}

	public String getData() {
		return data;
	}

}
