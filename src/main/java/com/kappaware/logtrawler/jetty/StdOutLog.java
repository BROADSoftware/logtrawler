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
package com.kappaware.logtrawler.jetty;

import org.eclipse.jetty.util.log.StdErrLog;

/**
 * A bug is jetty loggin system was the message 'INFO::main: Logging initialized' is displayed, whatever log level is defined in configuration file.
 * And it was on stderr.
 * 
 * The rule for Collector is, when running as a daemon to throw out stdout, but let stderr to be displayed, to be able to look after config or other errors.
 * So this message was annoying.
 * 
 * Two solutions: 
 * - A logger which throw anything: Dummy.log
 * - This logger which display on stdout.
 *
 * 
 * @author sa
 *
 */
public class StdOutLog extends StdErrLog {
	
	public StdOutLog() {
		super();
		this.setStdErrStream(System.out);
	}

}
