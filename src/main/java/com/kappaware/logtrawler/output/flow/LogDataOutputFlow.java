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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.output.OutputFlow;
import com.kappaware.logtrawler.output.OutputItem;
import com.kappaware.logtrawler.output.OutputItem.Type;

public class LogDataOutputFlow implements OutputFlow {
	Log log = LogFactory.getLog(LogDataOutputFlow.class);

	@Override
	public void output(OutputItem item) {
		if (item.getType() == Type.DATA_LINE) {
			log.info(String.format("%s: %s", item.getFile(), item.getData()));
		}
	}
	
	@Override
	public void output(List<OutputItem> items) {
		for(OutputItem item : items) {
			this.output(item);
		}
	}

}
