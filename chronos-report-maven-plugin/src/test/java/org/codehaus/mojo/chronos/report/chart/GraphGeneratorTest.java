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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/test/java/org/codehaus/mojo/chronos/report/chart/GraphGeneratorTest.java $
  * $Id: GraphGeneratorTest.java 17309 2012-08-14 12:21:00Z soelvpil $
  */
package org.codehaus.mojo.chronos.report.chart;

import junit.framework.TestCase;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSampleGroup;
import org.codehaus.mojo.chronos.report.ReportConfigStub;
import org.codehaus.mojo.chronos.report.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Testclass for {@link org.codehaus.mojo.chronos.report.chart.GraphGenerator}
 * 
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public class GraphGeneratorTest extends TestCase {

    ResourceBundle bundle;
    private ChartRendererStub renderer;

    protected void setUp() throws Exception {
        bundle = Utils.getBundle(Locale.getDefault());
        renderer = new ChartRendererStub();
    }

    /**
     * Tests if charts can be generated without errors.
     * 
     * @throws Exception
     * @throws MavenReportException
     */
    public void testDoGenerateReport() throws Exception {
        File file = new File("src/test/resources/test2-junitsamples.xml");
        GroupedResponsetimeSamples jmeterSamples = new GroupedResponsetimeSamples( file );

        Map<String, List<ChartSource>> detailsChartSources = new LinkedHashMap<String, List<ChartSource>>();
        for ( ResponsetimeSampleGroup sampleGroup : jmeterSamples.getSampleGroups() )
        {
            List<ChartSource> sources = new ArrayList<ChartSource>(  );
            DetailsHistogramChartSource detailsHistogramChartSource =
                new DetailsHistogramChartSource( sampleGroup );
            sources.add( detailsHistogramChartSource );
            detailsChartSources.put( sampleGroup.getName(), sources );
        }


        GraphGenerator gen = new GraphGenerator( new ArrayList<ChartSource>(  ), detailsChartSources, new SystemStreamLog());
        gen.generateGraphs(renderer, bundle, new ReportConfigStub());
        assertEquals(6, renderer.charts.size());
    }
}
