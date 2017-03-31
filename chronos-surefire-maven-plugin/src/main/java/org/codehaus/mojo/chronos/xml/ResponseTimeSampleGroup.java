/*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
 *
 * Copyright (c) to all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.0/chronos-jmeter-maven-plugin/src/main/java/org/codehaus/mojo/chronos/check/CheckMojo.java $
 * $Id: CheckMojo.java 14893 2011-10-24 12:08:52Z soelvpil $
 */
package org.codehaus.mojo.chronos.xml;

import org.apache.commons.math.stat.descriptive.rank.Min;
import org.jdom2.Element;

public class ResponseTimeSampleGroup {

	private Element root = new Element("responsetimesamplegroup");
	private int succeeded = 0;
    private final Min testStart = new Min();
	private String name;

	public ResponseTimeSampleGroup(String name) {
		this.name = name;
		root.setAttribute("name", name);
	}

    public long getTestStart() {
        return (long) testStart.getResult();
    }

	public int getSucceeded() {
		return succeeded;
	}

	public Element toXML() {
		return root;
	}

	public void addMeasurement( long timestamp, int responseTime, boolean success, String threadId ) {
		// <sample responsetime="31" timestamp="1157615684921" success="true" threadId="Performance Test Group 1-1"/>
        testStart.increment( timestamp );
		Element sample = new Element("sample");
        sample.setAttribute("timestamp", Long.toString(timestamp));
        sample.setAttribute("responsetime", Integer.toString(responseTime));
		sample.setAttribute("success", Boolean.toString(success));
		sample.setAttribute("threadId", threadId);
		root.addContent(sample);

		succeeded++;
	}
}