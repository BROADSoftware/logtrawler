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


import com.fasterxml.jackson.jr.ob.JSON;

import com.kappaware.logtrawler.config.Config;

public class Json {

	static JSON prettyJSON = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT);
	static JSON unprettyJSON = JSON.std.without(JSON.Feature.PRETTY_PRINT_OUTPUT);
	static JSON strictJSON = JSON.std.with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

	public static String toJson(Object obj, boolean pretty) {
		try {
			if(pretty) {
				return prettyJSON.asString(obj);
			} else {
				return unprettyJSON.asString(obj);
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to parse generate a Json string from an object of class '%s'", obj.getClass().getName()), e);
		}
	}

	public static Config fromJson(Class<?> clazz, Object json) {
		try {
			return (Config) strictJSON.beanFrom(clazz, json); 
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to parse '%s' as a valid Json!", json), e);
		}
	}

	
}
