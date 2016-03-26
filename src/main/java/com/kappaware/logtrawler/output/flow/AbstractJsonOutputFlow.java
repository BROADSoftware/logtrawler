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
import java.util.Vector;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kappaware.logtrawler.output.OutputFlow;
import com.kappaware.logtrawler.output.OutputItem;

/**
 * WARNING: This class will lock the output process in case of error. 
 * 
 * Temporary solution, relying on the behavior of collector
 * 
 * @author sa
 *
 */

public abstract class AbstractJsonOutputFlow implements OutputFlow {
	static Log log = LogFactory.getLog(AbstractJsonOutputFlow.class);

	boolean outputArray;

	public AbstractJsonOutputFlow(boolean outputArray) {
		this.outputArray = outputArray;
	}

	abstract void doOutput(Object obj);

	@Override
	public void output(List<OutputItem> items) {
		if(this.outputArray) {
			this.doOutput(items);
		} else {
			for(OutputItem item : items) {
				this.doOutput(item);
			}
		}
	}

	@Override
	public void output(OutputItem item) {
		if(this.outputArray) {
			List<OutputItem> l = new Vector<OutputItem>();
			l.add(item);
			this.doOutput(l);
		} else {
			this.doOutput(item);
		}
	}

}
