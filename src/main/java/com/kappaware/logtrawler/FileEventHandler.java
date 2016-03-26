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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.config.Config.Agent;
import com.kappaware.logtrawler.output.OutputFlow;
import com.kappaware.logtrawler.output.OutputItem;
import com.kappaware.logtrawler.output.OutputItem.Type;

public class FileEventHandler {
	static Log log = LogFactory.getLog(FileEventHandler.class);

	private BlockingSetQueue<FileEvent> queue;
	private Agent config;
	private HandlerThread handlerThread;
	private boolean running = true;
	private OutputFlow outputFlow;

	public FileEventHandler(BlockingSetQueue<FileEvent> queue, Agent config, OutputFlow outputFlow) {
		this.queue = queue;
		this.config = config;
		this.outputFlow = outputFlow;
		this.handlerThread = new HandlerThread();
		this.handlerThread.setDaemon(false); // This is NOT a daemon thread
		this.handlerThread.start();
	}

	public void kill() throws InterruptedException {
		this.running = false;
		this.handlerThread.interrupt();
		this.handlerThread.join();
	}

	private class HandlerThread extends Thread {
		private Map<FileKey, MFile> fileByKey = new HashMap<FileKey, MFile>();
		private Map<FileKey, MFile> zombieFileByKey = new HashMap<FileKey, MFile>();
		private Set<MFile> activeFiles = new HashSet<MFile>();
		private EvictionHolder evictionHolder = new EvictionHolder(config.getMaxLastEvictions());
		private long maxLineLength = 0;
		private long nbrLineRead = 0;

		@Override
		public void run() {
			while (running) {
				try {
					FileEvent event = null;
					try {
						event = queue.poll(1L, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						log.debug("Interrupted");
						break;
					}
					if (event == null) {
						// Time out
						this.cleanZombies();
						this.cleanActiveFile();
					} else {
						log.debug(String.format("Event:%s", event.toString()));
						switch (event.getType()) {
							case FILE_INIT:
								onInitEvent(event);
							break;
							case FILE_CREATE:
								onCreateEvent(event);
							break;
							case FILE_MODIFY:
								onModifyEvent(event);
							break;
							case PATH_DELETE:
								onDeleteEvent(event);
							break;
							case DIR_EXCLUDED:
								this.output(new OutputItem(config, Type.DIR_EXCLUSION, event.getPath().toString(), null, event.getTimestamp()));
							break;
							case FILE_EXCLUDED:
								MFile file = new MFile(event, config);
								this.output(new OutputItem(config, Type.FILE_EXCLUSION, file, null, event.getTimestamp()));
							break;
							case ERROR:
								this.output(new OutputItem(config, Type.ERROR, Utils.toString(event.getPath()), event.getMessage(), event.getTimestamp()));
							break;
							default:
								assert false : "Unknow event type: " + event.getType().toString();
							break;

						}
					}
				} catch (Throwable t) {
					logError(null, String.format("Error in main thread"), t);
					// Arbitrary wait, to avoid using all cpu in case of repetitive error
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		private void onInitEvent(FileEvent event) {
			MFile file = new MFile(event, config);
			this.fileByKey.put(file.getKey(), file);
			log.debug(String.format("Init file %s", file.toString()));
			try {
				file.qualify(config);
			} catch (IOException e) {
				logError(file.getPath(), "Error on read", e);
			}
			if (config.getPushInitialContent()) {
				try {
					file.setPosition(0L);
				} catch (IOException e) {
					logError(file.getPath(), "Error on read", e);
				}
				this.readFile(file, event.getTimestamp());
			}
			this.output(new OutputItem(config, Type.FILE_INIT, file, null, event.getTimestamp()));
		}

		private void onCreateEvent(FileEvent event) {
			if (event.getFileKey() != null && this.zombieFileByKey.containsKey(event.getFileKey())) {
				// It is a resurrection, due to a name change.
				MFile file = this.zombieFileByKey.remove(event.getFileKey());
				file.setDeletionTime(null);
				String oldName = file.getPath().toString();
				file.setPath(event.getPath());
				this.fileByKey.put(file.getKey(), file);
				this.output(new OutputItem(config, Type.FILE_RENAME, file, oldName, event.getTimestamp()));
				log.info(String.format("Resurect file %s", file.toString()));
			} else {
				MFile file = new MFile(event, config);
				this.fileByKey.put(file.getKey(), file);
				try {
					file.qualify(config);
					file.setPosition(0L); // As it is a new one
				} catch (IOException e) {
					logError(file.getPath(), null, e);
				}
				this.output(new OutputItem(config, Type.FILE_CREATE, file, null, event.getTimestamp()));
				log.info(String.format("Create file %s", file.toString()));
				this.readFile(file, event.getTimestamp());
			}
		}

		private void onModifyEvent(FileEvent event) {
			MFile file = this.fileByKey.get(event.getFileKey());
			if (file == null) {
				logError(String.format("Unable to find a file for key:%s (path:%s)", event.getFileKey(), event.getPath()));
			} else {
				log.debug(String.format("Modify file %s", file.toString()));
				if (event.getSize() != null && event.getSize() < file.getPosition()) {
					// Seems the file has been truncated. But may be the size hosted by event is old. Get current size
					if (file.isLog() && this.activateFile(file)) {
						Long size;
						try {
							size = file.getEffectiveSize();
							if (size < file.getPosition()) {
								this.output(new OutputItem(config, Type.FILE_TRUNCATE, file, null, event.getTimestamp()));
								log.debug(String.format("File '%s' has been truncated (new size:%d  recorded position:%d)", file.getPath(), event.getSize(), file.getPosition()));
								file.setPosition(0L);
							}
						} catch (IOException e) {
							logError(file.getPath(), null, e);
						}
					}
				}
				this.readFile(file, event.getTimestamp());
			}
		}

		private void readFile(MFile file, long timestamp) {
			if (file.isLog() && this.activateFile(file)) {
				String s;
				// We test again isLog(), as it may be updated by readLine()
				try {
					List<OutputItem> items = new Vector<OutputItem>();
					while (file.isLog() && (s = file.readLine(timestamp)) != null) {
						if (s.length() > this.maxLineLength) {
							this.maxLineLength = s.length();
						}
						this.nbrLineRead++;
						items.add(new OutputItem(config, OutputItem.Type.DATA_LINE, file, s, timestamp));
						if(items.size() >= config.getOutputMaxBatchSize()) {
							this.output(items);
							items = new Vector<OutputItem>();
						}
					}
					if(items.size() > 0) {
						// Send out remaining ones
						this.output(items);
						items = new Vector<OutputItem>();
					}
					if(!file.isLog()) {
						this.output(new OutputItem(config, OutputItem.Type.FILE_REJECT, file, file.getComment(), timestamp));
					}
				} catch (IOException e) {
					logError(file.getPath(), null, e);
				}
			}
			this.capActiveSize();
		}

		private void output(OutputItem item) {
			outputFlow.output(item);
			if (config.fetchDisplayDot()) {
				System.out.print(".");
			}
		}

		private void output(List<OutputItem> items) {
			outputFlow.output(items);
			if (config.fetchDisplayDot()) {
				System.out.print(":");
			}
		}

		private void onDeleteEvent(FileEvent event) {
			// As we have fileKey is null in case of delete, we must fetch by path.
			// If it is a directory. Must delete all childs
			List<MFile> toDelete = new ArrayList<MFile>();
			Path removed = event.getPath().toAbsolutePath().normalize();
			for (MFile existing : this.fileByKey.values()) {
				if (existing.getPath().startsWith(removed)) {
					toDelete.add(existing);
				}
			}
			for (MFile f : toDelete) {
				deleteFile(f, event.getTimestamp());
			}
		}

		private void deleteFile(MFile file, long timestamp) {
			log.info(String.format("Delete file %s", file.toString()));
			this.output(new OutputItem(config, Type.FILE_DELETE, file, null, timestamp));
			this.fileByKey.remove(file.getKey());
			FileKey key = file.getKey();
			// Set as zombie
			this.zombieFileByKey.put(key, file);
			file.setDeletionTime(System.currentTimeMillis());
		}

		/**
		 * 
		 * @param file
		 * @return	false if the file can't be activated
		 */
		private boolean activateFile(MFile file) {
			if (!file.isActive()) {
				try {
					file.open();
					this.activeFiles.add(file);
					return true;
				} catch (IOException e) {
					logError(file.getPath(), "Unable to open file", e);
					return false;
				}
			} else {
				return true;
			}
		}

		private void logError(Path path, String message, Throwable e) {
			//log.error(message, e);
			this.output(new OutputItem(config, Type.ERROR, (path!=null)?path.toString():null, (message!=null)?message + ": " + Utils.toString(e):Utils.toString(e), System.currentTimeMillis()));
		}

		private void logError(String message) {
			//log.error(message);
			this.output(new OutputItem(config, Type.ERROR, (String) null, message, System.currentTimeMillis()));
		}

		private void capActiveSize() {
			if (this.activeFiles.size() > config.getMaxActiveFiles()) {
				long oldestLat = Long.MAX_VALUE;
				MFile oldest = null;
				for (MFile file : this.activeFiles) {
					if (file.getLastAccessTime() < oldestLat) {
						oldestLat = file.getLastAccessTime();
						oldest = file;
					}
				}
				if (oldest != null) {
					try {
						oldest.close();
					} catch (IOException e) {
						logError(oldest.getPath(), "Error while closing", e);
					}
					this.activeFiles.remove(oldest);
					this.evictionHolder.add(oldest.getPath().toAbsolutePath().normalize().toString(), System.currentTimeMillis());
				}
			}
		}

		private void cleanZombies() {
			List<MFile> toDelete = new ArrayList<MFile>();
			long eol = System.currentTimeMillis() - (config.getZombieTtl() * 1000);
			for (MFile file : this.zombieFileByKey.values()) {
				if (file.getDeletionTime() < eol) {
					toDelete.add(file);
				}
			}
			if (toDelete.size() > 0) {
				for (MFile file : toDelete) {
					this.zombieFileByKey.remove(file.getKey());
					// Following does nothing if the file was not in active state.
					try {
						file.close();
					} catch (IOException e) {
						logError(file.getPath(), "Error while closing", e);
					}
					this.activeFiles.remove(file);
				}
				log.debug(String.format("%d zombies files has been cleaned", toDelete.size()));
			}
		}

		private void cleanActiveFile() {
			List<MFile> toClose = new ArrayList<MFile>();
			long eol = System.currentTimeMillis() - (config.getActiveFileTimeout() * 1000);
			for (MFile file : this.activeFiles) {
				if (file.getLastAccessTime() < eol) {
					toClose.add(file);
				}
			}
			if (toClose.size() > 0) {
				for (MFile file : toClose) {
					try {
						file.close();
					} catch (IOException e) {
						logError(file.getPath(), "Error while closing", e);
					}
					this.activeFiles.remove(file);
				}
				log.debug(String.format("%d active files has been closed", toClose.size()));
			}
		}
	}

	public Map<FileKey, MFile> getFileByKey() {
		return this.handlerThread.fileByKey;
	}

	public Map<FileKey, MFile> getZombieByKey() {
		return this.handlerThread.zombieFileByKey;
	}

	public Set<MFile> getActiveFiles() {
		return this.handlerThread.activeFiles;
	}

	public EvictionHolder getEvictionHolder() {
		return this.handlerThread.evictionHolder;
	}

	public long getMaxLineLength() {
		return this.handlerThread.maxLineLength;
	}

	public long getNbrLineRead() {
		return this.handlerThread.nbrLineRead;
	}
}
