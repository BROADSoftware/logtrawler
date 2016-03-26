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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.Json;

public class LogJsonOutputFlow extends AbstractJsonOutputFlow {
	Log log = LogFactory.getLog(LogJsonOutputFlow.class);

	
	public LogJsonOutputFlow(boolean outputArray) {
		super(outputArray);
	}
	

	@Override
	void doOutput(Object obj) {
		log.info(Json.toJson(obj, true));
	}

}
