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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/test/java/org/codehaus/mojo/chronos/report/ReportMojoTest.java $
  * $Id: ReportMojoTest.java 17313 2012-08-15 10:04:20Z soelvpil $
  */
package org.codehaus.mojo.chronos.report;

import junit.framework.TestCase;

public class ReportMojoTest extends TestCase {

    public void testSimple() throws Exception {
        TestHelper.performReport("src/test/resources/test1-junitsamples.xml", "src/test/resources/test1-gc.xml",
                "test1");
    }

    public void testJtl22Combined2() throws Exception {
        TestHelper.performReport("src/test/resources/combinedtest-jtl22-summaryreport.xml", null, "test5");
    }

    public void testOutputName() {
        ReportMojo mojo = new ReportMojo();
        mojo.reportid = "out";
        assertEquals("out", mojo.getOutputName());
    }

    public void testGetGc() {
        ReportMojo mojo = new ReportMojo();
        mojo.project = TestHelper.newMavenProject();
        assertTrue(mojo.getConfig().isShowgc());
    }

    public void testGetId() {
        ReportMojo mojo = new ReportMojo();
        mojo.reportid = "xx";
        assertEquals("xx", mojo.reportid != null ? mojo.reportid : mojo.dataid );
        mojo.reportid = "yy";
        assertEquals("yy", mojo.reportid != null ? mojo.reportid : mojo.dataid );
        mojo.reportid = null;
        mojo.dataid = "zz";
        assertEquals("zz", mojo.reportid != null ? mojo.reportid : mojo.dataid );
    }
}