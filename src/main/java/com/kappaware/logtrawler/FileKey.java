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


/**
 * This class is intended to represent a unique file identifier. Typically an inode for Unix FS.
 * 
 * A requirement is to be able to build a FileKey from a String representation, as it is the only representation which could be sent back from the server.
 * @author sa
 *
 */
public class FileKey {
	private String value;
	
	public FileKey(String s) {
		this.value = s;
	}
	public FileKey(Object o) {
		this.value = o.toString();
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else {
			return this.value.equals(((FileKey)other).value);
		}
	}

	@Override
	public String toString() {
		return this.value;
	}
}
