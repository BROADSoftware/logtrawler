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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.adminserver.Stats.MFileView;
import com.kappaware.logtrawler.config.Config;
import com.kappaware.logtrawler.config.Config.Agent;

/**
 * Represent a file in the monitored space.
 * 
 * 
 * @author sa
 *
 */
public class MFile {
	static Log log = LogFactory.getLog(MFile.class);

	private Config.Agent config;
	private Path path;
	private FileKey key;
	private String mimeType;
	private long position;

	private boolean isLog;
	private Long deletionTime; // When a file is deleted.
	// Null when not active
	private BufferedReader reader;
	private FileChannel channel;
	private StringBuilder line;
	// OL will be understood as 'very very old'
	private Long lastAccessTime;
	private Long lastFetchTime; 
	private long nbrCharRead = 0L;
	private long nbrControlCharRead = 0L;
	private String comment;
	private long maxLineLength = 0L;
	private String lastLine = null;
	private long nbrLineRead = 0;

	MFile(FileEvent event, Config.Agent config) {
		this.path = event.getPath();
		this.key = event.getFileKey();
		this.position = event.getAttributes().size();
		this.config = config;
		this.line = new StringBuilder(256);	// To avoid reallocation in most cases
	}

	/**
	 * 
	 * @return	false is the file can't be opened, whatever is the reason
	 * @throws IOException
	 */
	void open() throws IOException {
		try {
			Utils.assertNull(this.channel, "Channel must be null on open on %s", this.path);
			Utils.assertNull(this.reader, "Reader must be null on open on %s", this.path);
			FileInputStream fis = new FileInputStream(this.path.toFile());
			this.channel = fis.getChannel();
			this.channel.position(this.position);
			this.reader = new BufferedReader(new InputStreamReader(fis));
			// Need this, otherwise will be the oldest, so removed before used.
			this.lastAccessTime = System.currentTimeMillis();
			return;
		} catch (IOException e) {
			//log.error(String.format("Unable to open file '%s'", this.path), e);
			if (this.channel != null) {
				try {
					this.channel.close();
				} catch (IOException e1) {
				}
			}
			throw e;
		}
	}
	


	/**
	 * Can be safely called either active or not
	 * @throws IOException
	 */
	void close() throws IOException {
		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				//log.error(String.format("Error while closing '%s'", this.path), e);
				this.reader = null;
				this.channel = null;
				throw e;
			}
			this.reader = null;
			this.channel = null;
		}
	}

	/**
	 * Note we use the timestamp of the triggering event.
	 * @param timestamp
	 * @return
	 * @throws IOException
	 */
	
	String readLine(long timestamp) throws IOException {
		//try {
			this.lastAccessTime = timestamp;
			while(true) {
				int c = this.reader.read();
				switch(c) {
					case '\n' :
						this.nbrCharRead++;
						this.lastFetchTime = this.lastAccessTime;
						this.position = this.channel.position();
						String r = this.line.toString();
						this.line.delete(0, this.line.length());
						if(r.length() > this.maxLineLength) {
							this.maxLineLength = r.length();
						}
						this.lastLine = r;
						this.nbrLineRead++;
						return r;
					//break;
					case -1:
						this.position = this.channel.position();
						return null;
					case '\r':
					case '\t':
						// handle specially to skip isIsoControl()
						this.nbrCharRead++;
						this.line.append((char)c);
					break;
					default:
						this.nbrCharRead++;
						if(Character.isISOControl(c)) {
							this.nbrControlCharRead++;
						}
						this.line.append((char)c);
					break;
				}
				// Check every 50 characters
				if(this.nbrCharRead % 50 == 0) {
					if(this.line.length() > config.getMaxLineLength()) {
						this.isLog = false;
						log.info(String.format("File '%s' is flagged as non-log, due to oversized line (> %d)", this.path, config.getMaxLineLength()));
						this.comment = String.format("File is flagged as non-log, due to oversized line (> %d)", config.getMaxLineLength());
						// Delete line, as it can introduce parsing error on stats
						this.line.delete(0, this.line.length());
						return null;
					}
					if(((this.nbrControlCharRead * 100) / this.nbrCharRead) > config.getMaxControlCharPercent()) {
						this.isLog = false;
						log.info(String.format("File '%s' is flagged as non-log, due to too many control char (%d for %d exceed %d percent)", this.path, this.nbrControlCharRead, this.nbrCharRead, config.getMaxControlCharPercent()));
						this.comment = String.format("File is flagged as non-log, due to too many control char (%d for %d exceed %d percent)", this.nbrControlCharRead, this.nbrCharRead, config.getMaxControlCharPercent());
						// Delete line, as it can introduce parsing error on stats
						this.line.delete(0, this.line.length());
						return null;
					}
				}
			}
		//} catch (IOException e) {
			//log.error(String.format("Error on reading '%s'", this.path), e);
			//return null;
		//}
	}
	

	public Long getEffectiveSize() throws IOException {
		//try {
			return this.channel.size();
		//} catch (IOException e) {
		//	log.error(String.format("Unable to get effective size on '%s'", this.path), e);
		//	return null;
		//}
	}



	public boolean isActive() {
		return this.reader != null;
	}

	void qualify(Agent config) throws IOException {
		try {
			this.mimeType = Files.probeContentType(this.path);
		} catch (IOException e) {
			//log.error(String.format("Unable to probe content of %s", this.path.toString()), e);
			this.isLog = false;
			throw e;
		}
		if (config.getLogMimeTypes().contains(this.mimeType)) {
			this.isLog = true;
		} else {
			this.isLog = false;
		}

	}

	public void setDeletionTime(Long deletionTime) {
		this.deletionTime = deletionTime;
	}

	public void setPath(Path path) {
		this.path = path.toAbsolutePath().normalize();
	}

	public void setPosition(long position) throws IOException {
		this.position = position;
		if(this.channel != null) {
			//try {
				this.channel.position(this.position);
			//} catch (IOException e) {
			//	log.error(String.format("Unable to set position=%d on file '%s'", this.position, this.path));
			//}
		}
	}
	
	public Path getPath() {
		return path;
	}

	public FileKey getKey() {
		return key;
	}

	public boolean isLog() {
		return isLog;
	}

	public Long getDeletionTime() {
		return deletionTime;
	}

	public String getMimeType() {
		return mimeType;
	}

	public long getPosition() {
		return this.position;
	}

	public Long getLastAccessTime() {
		return lastAccessTime;
	}

	public Long getLastFetchTime() {
		return lastFetchTime;
	}

	public String getLine() {
		return this.line.toString();
	}

	public long getNbrCharRead() {
		return nbrCharRead;
	}

	public long getNbrControlCharRead() {
		return nbrControlCharRead;
	}

	public String getComment() {
		return comment;
	}

	public long getMaxLineLength() {
		return maxLineLength;
	}
	
	public String getLastLine() {
		return lastLine;
	}

	public long getNbrLineRead() {
		return nbrLineRead;
	}

	@Override
	public String toString() {
		return String.format("File: Path='%s' key='%s' mimeType='%s' isLog:%s", this.path, this.key, this.mimeType, this.isLog ? "YES" : "no");
	}

	// Minimize object creation by caching them
	private MFileView view;

	public MFileView getView() {
		if (this.view == null) {
			this.view = new MFileView(this);
		}
		return this.view;
	}

	// Needed has we will use this in a Set
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else {
			return this.key.equals(((MFile)other).key);
		}
	}
	
	
}
