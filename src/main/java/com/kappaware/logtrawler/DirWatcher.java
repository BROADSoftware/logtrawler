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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.FileEvent.Type;
import com.kappaware.logtrawler.config.Config;

public class DirWatcher {
	static Log log = LogFactory.getLog(DirWatcher.class);

	private boolean followLink;
	private BlockingSetQueue<FileEvent> queue;
	private final WatchService watcher;
	private final Map<WatchKey, Path> pathByKey;
	private WatcherThread watcherThread;
	private boolean running = true;
	private Set<FileVisitOption> fileVisitOptions;
	private List<Exclusion> exclusions;

	/**
	 * 
	 * @param path		The path to monitor
	 * @param queue		Where to store events
	 * @throws IOException 
	 */
	DirWatcher(Config.Agent.Folder dir, BlockingSetQueue<FileEvent> queue) throws IOException {
		Path path = Paths.get(dir.getPath());
		this.followLink = dir.getFollowLink();
		this.queue = queue;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.pathByKey = new HashMap<WatchKey, Path>();
		this.fileVisitOptions = new HashSet<FileVisitOption>();
		if (this.followLink) {
			this.fileVisitOptions.add(FileVisitOption.FOLLOW_LINKS);
		}
		if (dir.getExcludedPaths() != null && dir.getExcludedPaths().size() > 0) {
			exclusions = new Vector<Exclusion>(dir.getExcludedPaths().size());
			for (String excludedPath : dir.getExcludedPaths()) {
				exclusions.add(new Exclusion(dir.getPath(), excludedPath));
			}
		}
		log.info(String.format("Scanning %s ...", path));
		this.registerWithSub(path, FileEvent.Type.FILE_INIT);
		this.watcherThread = new WatcherThread();
		this.watcherThread.setDaemon(true);
		this.watcherThread.start();
	}

	public void kill() throws InterruptedException {
		this.running = false;
		this.watcherThread.interrupt();
		this.watcherThread.join();
	}

	private boolean check(String path) {
		if (this.exclusions != null) {
			for (Exclusion exclusion : this.exclusions) {
				if (exclusion.match(path)) {
					exclusion.count++;
					exclusion.last = path;
					return false;
				}
			}
		}
		return true;
	}

	private class MyPathVisitor extends SimpleFileVisitor<Path> {
		FileEvent.Type newFileType;

		MyPathVisitor(FileEvent.Type newFileType) {
			this.newFileType = newFileType;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
			Path ap = path.toAbsolutePath().normalize();
			if (check(ap.toString())) {
				register(ap);
				return FileVisitResult.CONTINUE;
			} else {
				log.debug(String.format("Folder %s excluded", path));
				addToQueueNoCheck(new FileEvent(Type.DIR_EXCLUDED, path));
				return FileVisitResult.SKIP_SUBTREE;
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			addToQueue(new FileEvent(newFileType, file, attrs));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			Objects.requireNonNull(file);
			logError("Error while accessing", file, exc);
			return FileVisitResult.CONTINUE;
		}

	}

	private void registerWithSub(final Path path, FileEvent.Type newFiletype) throws IOException {
		Files.walkFileTree(path, this.fileVisitOptions, Integer.MAX_VALUE, new MyPathVisitor(newFiletype));
	}

	private void addToQueue(FileEvent fevent) {
		if (check(fevent.getPath().toString())) {
			this.addToQueueNoCheck(fevent);
		} else {
			addToQueueNoCheck(new FileEvent(Type.FILE_EXCLUDED, fevent.getPath(), fevent.getAttributes()));
			log.debug(String.format("File %s excluded", fevent.getPath()));
		}
	}

	private void addToQueueNoCheck(FileEvent fevent) {
		boolean b = this.queue.add(fevent);
		if (log.isDebugEnabled()) {
			if (!b) {
				log.debug(String.format("Message %s dropped, as already in queue", fevent.toString()));
			}
		}
	}

	private void register(Path path) throws IOException {
		WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (log.isDebugEnabled()) {
			Path prev = pathByKey.get(key);
			if (prev == null) {
				log.debug(String.format("register: %s", path));
			} else {
				if (!path.equals(prev)) {
					log.debug(String.format("update: %s -> %s", prev, path));
				}
			}
		}
		this.pathByKey.put(key, path);
	}

	static private Map<WatchEvent.Kind<?>, FileEvent.Type> typeFromFileEvent;
	static {
		typeFromFileEvent = new HashMap<WatchEvent.Kind<?>, FileEvent.Type>();
		typeFromFileEvent.put(ENTRY_CREATE, FileEvent.Type.FILE_CREATE);
		typeFromFileEvent.put(ENTRY_DELETE, FileEvent.Type.PATH_DELETE); // Normaly, unused
		typeFromFileEvent.put(ENTRY_MODIFY, FileEvent.Type.FILE_MODIFY);
	}

	private class WatcherThread extends Thread {

		@SuppressWarnings({ "unchecked", "unused" })
		@Override
		public void run() {
			while (running) {
				// wait for a key to be signalled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					continue;
				}
				Path path = pathByKey.get(key);
				if (path == null) {
					logError(String.format("No matching path for a watchKey!"));
				} else {
					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						if (kind == StandardWatchEventKinds.OVERFLOW) {
							logError(String.format("OVERFLOW on java.nio.file.WatchService"));
						} else {
							Path childBase = ((WatchEvent<Path>) event).context();
							Path child = path.resolve(childBase);
							try {
								log.debug(String.format("%s: %s", event.kind().name(), child));
								if (kind == ENTRY_DELETE) {
									addToQueueNoCheck(new FileEvent(FileEvent.Type.PATH_DELETE, child));
								} else if (Files.isDirectory(child, followLink ? null : NOFOLLOW_LINKS) && kind == ENTRY_CREATE) {
									// if directory is created, then register it and its sub-directories
									registerWithSub(child, FileEvent.Type.FILE_CREATE);
								} else if (Files.isRegularFile(child, followLink ? null : NOFOLLOW_LINKS)) {
									addToQueue(new FileEvent(typeFromFileEvent.get(kind), child, Files.readAttributes(child, BasicFileAttributes.class)));
								}
							} catch (IOException x) {
								logError("Error while registering", child, x);
							}
						}
					}
				}
				// reset key and remove from set if directory no longer accessible
				boolean valid = key.reset();
				if (!valid) {
					log.debug(String.format("Removing path %s", pathByKey.get(key).toString()));
					pathByKey.remove(key);
					if (pathByKey.isEmpty()) {
						// all directories are inaccessible
						running = false;
					}
				}
				if (false && log.isDebugEnabled()) {
					String sep = "";
					String r = "";
					for (Path p : pathByKey.values()) {
						r += sep + p;
						sep = ", ";
					}
					log.debug(String.format("Monitored path:'%s'", r));
				}
			}
		}
	}

	private void logError(String message) {
		this.addToQueueNoCheck(new FileEvent(Type.ERROR, null, message));
		//log.error(message);
	}
	
	private void logError(String message, Path path, Throwable t) {
		this.addToQueueNoCheck(new FileEvent(Type.ERROR, path, (message!=null)?message + ": " + Utils.toString(t): Utils.toString(t)));
		//log.error(message, t);
	}
	
	public Collection<? extends Exclusion> getExclusions() {
		return this.exclusions;
	}

	public static class Exclusion {
		private String basePath;		// Only for stats completeness
		private String regex;
		private Pattern pattern;
		private long count;
		private String last;

		Exclusion(String basePath, String excludedPath) {
			this.basePath = basePath;
			this.regex = excludedPath;
			this.pattern = Pattern.compile(excludedPath);
		}

		boolean match(String s) {
			return pattern.matcher(s).matches();
		}

		public String getRegex() {
			return regex;
		}

		public long getCount() {
			return count;
		}

		public String getLast() {
			return last;
		}

		public String getBasePath() {
			return basePath;
		}

	}
}
