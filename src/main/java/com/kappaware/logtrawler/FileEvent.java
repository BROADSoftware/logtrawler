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

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileEvent {

	// @formatter:off
	public enum Type {
		FILE_INIT, 
		FILE_CREATE, 
		FILE_MODIFY, 
		PATH_DELETE, // As we receive this after object deletion, we are unable to determine if it is a file or a folder
		DIR_EXCLUDED,
		FILE_EXCLUDED,
		ERROR
	}
	// @formatter:on

	private Type type;
	private Path path;
	private BasicFileAttributes attributes;
	private FileKey fileKey = null;
	private Long size;
	private Integer hashCode = null;
	private long timestamp;
	private String message;	// Just for error

	public FileEvent(Type type, Path path, BasicFileAttributes attributes) {
		this(type, path);
		this.attributes = attributes;		
		if (this.attributes != null) {
			// No attribute in case of PATH_DELETE
			this.fileKey = new FileKey(this.attributes.fileKey());
			this.size = this.attributes.size();
		}
	}

	public FileEvent(Type type, Path path, String message) {
		this(type, path);
		this.message = message;
	}

	public FileEvent(Type type, Path path) {
		super();
		this.timestamp = System.currentTimeMillis();
		this.type = type;
		if(path != null) {
			this.path = path.toAbsolutePath().normalize();
		}
	}
	
	public Type getType() {
		return type;
	}

	public Path getPath() {
		return path;
	}

	public BasicFileAttributes getAttributes() {
		return attributes;
	}

	public FileKey getFileKey() {
		return fileKey;
	}

	public Long getSize() {
		return size;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Note this optimization rely on the fact this object is not mutable (Enforced by no setter)
	 * @return
	 */
	@Override
	public int hashCode() {
		if (this.hashCode == null) {
			this.hashCode = this.type.hashCode() + ((this.fileKey!=null)?this.fileKey.hashCode():0) + ((this.path!=null)?this.path.hashCode():0) + ((this.message!=null)?this.message.hashCode():0);
		}
		return this.hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else {
			if (this.hashCode() != other.hashCode()) {
				return false;
			} else {
				FileEvent o = (FileEvent) other;
				return (this.type.equals(o.getType()) && Utils.isEquals(this.path, o.getPath()) && Utils.isEquals(this.fileKey, o.fileKey) && Utils.isEquals(this.message, o.message));
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s on %s (%s)  size:%s", this.type.toString(), this.path.toString(), (this.attributes == null) ? "NoAttr" : this.attributes.fileKey(),  (this.attributes == null) ? "NoAttr" : Long.toString(this.attributes.size()));
	}

}
