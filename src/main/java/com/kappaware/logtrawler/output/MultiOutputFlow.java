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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import com.kappaware.logtrawler.config.ConfigurationException;

public class MultiOutputFlow implements OutputFlow {
	private List<OutputFlow> outputFLows;
	
	
	public MultiOutputFlow(OutputFlowFactory factory, Collection<String> outList) throws IOException, ConfigurationException {
		this.outputFLows = new Vector<OutputFlow>();
		for(String s : outList) {
			this.outputFLows.add(factory.newOutputFlow(s));
		}
	}
	
	@Override
	public void output(OutputItem item) {
		for(OutputFlow oflow : this.outputFLows) {
			oflow.output(item);
		}
	}

	@Override
	public void output(List<OutputItem> items) {
		for(OutputFlow oflow : this.outputFLows) {
			oflow.output(items);
		}
	}

}
