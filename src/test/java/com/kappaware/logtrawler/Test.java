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

import java.io.IOException;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Sorry, This is not a Junit test.
 * 
 * Just a small program to test behavior of parser in case of control char in string
 * 
 * @author Serge ALEXANDRE
 *
 */
public class Test {
	private String testTab = buildTest(9);
	private String testBs = buildTest(8);
	private String testFormFeed = buildTest(12);
	private String testCr = buildTest(13);
	private String testLf = buildTest(10);
	private String testSlash = buildTest(0x2f);
	private String testBackSlash = buildTest(0x5c);
	
	private String testCtrl1 = buildTest(1);
	private String testCtrl2 = buildTest(2);
	private String testCtrl19 = buildTest(19);
	
	private static String buildTest(int codePoint) {
		StringBuffer sb = new StringBuffer();
		sb.append("ABC");
		sb.append((char)codePoint);
		sb.append("DEF");
		return sb.toString();
	}

	public String getTestTab() {
		return testTab;
	}



	public void setTestTab(String testTab) {
		this.testTab = testTab;
	}



	public String getTestBs() {
		return testBs;
	}



	public void setTestBs(String testBs) {
		this.testBs = testBs;
	}



	public String getTestFormFeed() {
		return testFormFeed;
	}



	public void setTestFormFeed(String testFormFeed) {
		this.testFormFeed = testFormFeed;
	}



	public String getTestCr() {
		return testCr;
	}



	public void setTestCr(String testCr) {
		this.testCr = testCr;
	}



	public String getTestLf() {
		return testLf;
	}



	public void setTestLf(String testLf) {
		this.testLf = testLf;
	}



	public String getTestSlash() {
		return testSlash;
	}



	public void setTestSlash(String testSlash) {
		this.testSlash = testSlash;
	}



	public String getTestBackSlash() {
		return testBackSlash;
	}



	public void setTestBackSlash(String testBackSlash) {
		this.testBackSlash = testBackSlash;
	}



	public String getTestCtrl1() {
		return testCtrl1;
	}

	public void setTestCtrl1(String testCtrl1) {
		this.testCtrl1 = testCtrl1;
	}

	public String getTestCtrl2() {
		return testCtrl2;
	}

	public void setTestCtrl2(String testCtrl2) {
		this.testCtrl2 = testCtrl2;
	}

	public String getTestCtrl19() {
		return testCtrl19;
	}

	public void setTestCtrl19(String testCtrl19) {
		this.testCtrl19 = testCtrl19;
	}

	static JSON prettyJSON = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT);
	static JSON unprettyJSON = JSON.std.without(JSON.Feature.PRETTY_PRINT_OUTPUT);

	public String toJson(boolean pretty) {
		try {
			if(pretty) {
				return prettyJSON.asString(this);
			} else {
				return unprettyJSON.asString(this);
			}
		} catch (JSONObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Test fromJson(String json) {
		try {
			return (Test) JSON.std.beanFrom(Test.class, json); 
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to parse '%s' as a valid Json!", json), e);
		}
	}

	public static void main(String[] argv) {
		Test test1 = new Test();
		String test1str = test1.toJson(false);
		System.out.println(test1str);

		Test test2 = Test.fromJson(test1str);
		String test2str = test2.toJson(true);
		System.out.println(test2str);
	}

}
