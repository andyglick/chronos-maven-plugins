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
 * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/ReportMojo.java $
 * $Id: ReportMojo.java 17317 2012-08-15 11:32:32Z soelvpil $
 */
package org.codehaus.mojo.chronos.report;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;

import java.util.Locale;

/**
 * Creates a report of the currently executed performancetest in html format.
 * 
 * @author ksr@lakeside.dk
 * @goal report
 */
// Line merged from Atlession
// Removed the @execution phase=verify descriptor to prevent double lifecycle execution
// * @execute phase=verify
public class ReportMojo extends AbstractReportMojo {

	private static final int DEFAULT_DURATION = 20000;

	/**
	 * Location (directory) where generated html will be created.
	 * 
	 * @parameter expression="${project.build.directory}/site "
	 */
	protected String outputDirectory;

	/**
	 * Doxia Site Renderer.
	 * 
	 * @component role="org.codehaus.doxia.site.renderer.SiteRenderer"
	 * @required
	 * @readonly
	 */
	protected SiteRenderer siteRenderer;

	/**
	 * Maven Project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	public MavenProject project;

	/**
	 * The id of the report and the name of the generated html-file. If no id is defined, the dataid is used
	 * 
	 * @parameter
	 */
	protected String reportid;

	/**
	 * The id of the data, to create a report from.
	 * 
	 * @parameter default-value = "performancetest"
	 */
	protected String dataid;

	/**
	 * The title of the generated report.
	 * 
	 * @parameter
	 */
	protected String title;

	/**
	 * The description of the generated report.
	 * 
	 * @parameter
	 */
	protected String description;

	/**
	 * The timeinterval (in millis) to base moving average calculations on.
	 * 
	 * @parameter default-value = 20000
	 */
	protected int averageduration = DEFAULT_DURATION; // 20 seconds

	/**
	 * Should a summary of the tests taken together be shown?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showsummary = true;

	/**
	 * Should details of each individual test be shown?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showdetails = true;

	/**
	 * Should a histogram be shown?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showhistogram = true;

	/**
	 * Should a graph of throughput be shown?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showthroughput = true;

	/**
	 * Will graphs of responsetimes and histogram show 95 percentiles?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showpercentile95 = true;

	/**
	 * Will graphs of responsetimes and histogram show 95 percentiles?
	 * 
	 * @parameter default-value=false
	 */
	protected boolean showpercentile99 = false;

	/**
	 * Will graphs of responsetimes and histogram show the average?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showaverage = true;

	/**
	 * Will garbage collections be shown?
	 * 
	 * @parameter default-value=true
	 */
	protected boolean showgc = true;

	/**
	 * Points to a simple text file containing meta data about the build.<br />
	 * The information will be added to the reports under <i>Additional build info</i>.<br />
	 * The file is read line for line and added the report.<br />
	 * The readed expects the <code>tab</code> character to seperate keys and values:
	 * <p/>
	 * 
	 * <pre>
	 * Build no.&lt;tab&gt;567
	 * Svn tag&lt;tab&gt;Test
	 * </pre>
	 * 
	 * @parameter default-value=null
	 */
	protected String metadata;

	/**
	 * @param locale
	 * @throws MavenReportException
	 * @see AbstractMavenReport#executeReport(Locale)
	 */
	public void executeReport(Locale locale) throws MavenReportException {
		generateReport(locale);
	}

	/**
	 * @param locale
	 * @see MavenReport#getName(Locale)
	 */
	public String getName(Locale locale) {
		return getOutputName();
	}

	/**
	 * @param locale
	 * @see MavenReport#getDescription(Locale)
	 */
	public String getDescription(Locale locale) {
		return description;
	}

	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
	 */
	protected SiteRenderer getSiteRenderer() {
		return siteRenderer;
	}

	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
	 */
	protected MavenProject getProject() {
		return project;
	}

	/**
	 * @see org.apache.maven.reporting.MavenReport#getReportOutputDirectory()
	 */
	protected String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
	 */
	public boolean canGenerateReport() {
		return true;
	}

    @Override
	protected String getDataId() {
		return dataid;
	}

    @Override
	protected TestDataDirectory getTestDataDirectory() {
		final ProjectBaseDir projectBaseDir = new ProjectBaseDir(getProject().getBasedir() );
		return projectBaseDir.getDataDirectory(getDataId());
	}

    protected ReportConfig getConfig() {
        return new ReportConfig() {
            public int getAverageduration() {
                return averageduration;
            }

            public String getDescription() {
                return description;
            }

            public String getId() {
                return reportid != null ? reportid : dataid;
            }

            public String getMetadata() {
                return metadata;
            }

            public String getTitle() {
                return title;
            }

            public boolean isShowsummary() {
                return showsummary;
            }

            public boolean isShowdetails() {
                return showdetails;
            }

            public boolean isShowaverage() {
                return showaverage;
            }

            public boolean isShowpercentile95() {
                return showpercentile95;
            }

            public boolean isShowpercentile99() {
                return showpercentile99;
            }

            public boolean isShowhistogram() {
                return showhistogram;
            }

            public boolean isShowthroughput() {
                return showthroughput;
            }

            public boolean isShowgc() {
                return showgc;
            }

        };
    }
}