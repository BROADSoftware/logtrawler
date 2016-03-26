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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {
	static Log log = LogFactory.getLog(Utils.class);

	static public boolean isEquals(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	public static String toString(Object o) {
		return (o==null)?null:o.toString();
	}

	public static void assertNotNull(Object val, String message, Object... params) {
		if (val == null) {
			String m = String.format(message, params);
			log.fatal(m);
			System.exit(1);
		}
	}

	public static void assertNull(Object val, String message, Object... params) {
		if (val != null) {
			String m = String.format(message, params);
			log.fatal(m);
			System.exit(1);
		}
	}

	// Http definition
	private static final String HEADER_PRAGMA = "Pragma";
	private static final String HEADER_EXPIRES = "Expires";
	private static final String HEADER_CACHE_CONTROL = "Cache-Control";

	public static void setCache(HttpServletResponse response, int cacheValue) {
		if (cacheValue == 0) {
			response.setHeader(HEADER_PRAGMA, "no-cache");
			// HTTP 1.0 header
			response.setDateHeader(HEADER_EXPIRES, 1L);
			// HTTP 1.1 header: "no-cache" is the standard value,
			// "no-store" is necessary to prevent caching on FireFox.
			response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
			response.addHeader(HEADER_CACHE_CONTROL, "no-store");
			response.addHeader(HEADER_CACHE_CONTROL, "must-revalidate");
			response.addHeader(HEADER_CACHE_CONTROL, "post-check=0");
			response.addHeader(HEADER_CACHE_CONTROL, "pre-check=0");

		} else {
			response.setHeader(HEADER_CACHE_CONTROL, "public");
			long now = (new Date()).getTime();
			response.setDateHeader("Date", now);
			response.setDateHeader(HEADER_EXPIRES, now + (cacheValue * 1000L));
			// HTTP 1.1 header
			String headerValue = "max-age=" + Long.toString(cacheValue);
			response.setHeader(HEADER_CACHE_CONTROL, headerValue);
		}
	}

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error("Unable to find our hostname!");
			return "UNKNOW";
		}
	}
	/*
	private static String isoDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static String printIsoDateTime(Long ts) {
		if (ts != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(isoDateTimeFormat.toString());
			return sdf.format(ts);
		} else {
			return null;
		}
	}
	*/
	public static String printIsoDateTime(Long ts) {
		if (ts != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(ts);
			return DatatypeConverter.printDateTime(c);
		} else {
			return null;
		}
	}
	
	

	public static String toString(Throwable e) {
		StringBuffer sb = new StringBuffer();
		sb.append(e.toString());
		while((e = e.getCause()) != null) {
			sb.append("\nCaused by:");
			sb.append(e.toString());
		};
		return sb.toString();
	}
}
