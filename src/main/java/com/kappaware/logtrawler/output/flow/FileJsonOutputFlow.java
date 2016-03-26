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
package com.kappaware.logtrawler.output.flow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.Json;

public class FileJsonOutputFlow extends AbstractJsonOutputFlow {
	Log log = LogFactory.getLog(FileJsonOutputFlow.class);
	private PrintWriter out;

	public FileJsonOutputFlow(String fileName, boolean outputArray) throws IOException {
		super(outputArray);
		out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
		log.info(String.format("Json events will be stored in to '%s'", fileName));
	}

	@Override
	void doOutput(Object obj) {
		out.println(Json.toJson(obj, true));
		out.flush();
	}

}
