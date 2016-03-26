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

import com.kappaware.logtrawler.config.ConfigurationException;
import com.kappaware.logtrawler.output.flow.FileDataOutputFlow;
import com.kappaware.logtrawler.output.flow.FileJsonOutputFlow;
import com.kappaware.logtrawler.output.flow.LogDataOutputFlow;
import com.kappaware.logtrawler.output.flow.LogJsonOutputFlow;
import com.kappaware.logtrawler.output.flow.PostJsonOutputFlow;
import com.kappaware.logtrawler.output.flow.StdoutDataOutputFlow;
import com.kappaware.logtrawler.output.flow.StdoutJsonOutputFlow;

public class OutputFlowFactory {

	final static String HTTP = "http://";
	final static String HTTPS = "https://";

	final static String FILE = "file://";
	final static String DFILE = "dfile://";
	final static String JFILE = "jfile://";
	
	final static String LOG = "log";
	final static String DLOG = "dlog";
	final static String JLOG = "jlog";
	
	final static String STDOUT = "stdout";
	final static String JSTDOUT = "jstdout";
	final static String DSTDOUT = "dstdout";
	
	private boolean outputArray;
	
	public OutputFlowFactory(boolean outputArray) {
		this.outputArray = outputArray;
	}




	public OutputFlow newOutputFlow(String url) throws IOException, ConfigurationException {
		if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
			return new PostJsonOutputFlow(url, this.outputArray);
		} else if (url.startsWith(FILE)) {
			return new FileJsonOutputFlow(url.substring(FILE.length()), this.outputArray);
		} else if (url.startsWith(JFILE)) {
			return new FileJsonOutputFlow(url.substring(JFILE.length()), this.outputArray);
		} else if (url.startsWith(DFILE)) {
			return new FileDataOutputFlow(url.substring(DFILE.length()));
		} else if (url.startsWith(DLOG)) {
			return new LogDataOutputFlow();
		} else if (url.startsWith(JLOG) || url.startsWith(LOG)) {
			return new LogJsonOutputFlow(this.outputArray);
		} else if (url.startsWith(JSTDOUT) || url.startsWith(STDOUT)) {
			return new StdoutJsonOutputFlow(this.outputArray);
		} else if (url.startsWith(DSTDOUT)) {
			return new StdoutDataOutputFlow();
		} else {
			throw new ConfigurationException(String.format("Unable to interpret outputflow definition: %s", url));
		}

	}

}
