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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/chart/GraphGenerator.java $
  * $Id: GraphGenerator.java 17316 2012-08-15 10:44:47Z soelvpil $
  */
package org.codehaus.mojo.chronos.report.chart;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.chronos.report.ReportConfig;
import org.jfree.chart.JFreeChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Generates the charts of the jmeter report.
 *
 * @author ksr@lakeside.dk
 */
public final class GraphGenerator
{
    private final Log log;
    
    
    private List<ChartSource> summaryChartSources = new ArrayList<ChartSource>();

    private Map<String, List<ChartSource>> detailsChartSources = new LinkedHashMap<String, List<ChartSource>>();

    public GraphGenerator( List<ChartSource> summaryChartSources, Map<String, List<ChartSource>> detailsChartSources, Log log)
    {
        this.log = log;
        this.summaryChartSources = summaryChartSources;

        this.detailsChartSources = detailsChartSources;
    }

    /**
     * Generates response, throughput, histogram and gc charts according to report parameters.
     *
     * @param renderer The <code>ChartRenderer</code> instance used for rendering.
     * @param bundle   The <code>ResourceBundle</code> instance used for localization.
     * @param config   The <code>ReportConfig</code> instance used for controlling the graph rendering.
     * @throws IOException Thrown if the operation fails.
     */
    public void generateGraphs( ChartRenderer renderer, ResourceBundle bundle, ReportConfig config )
        throws IOException
    {
        for ( ChartSource chartSource : summaryChartSources )
        {
            if ( chartSource.isEnabled( config ) )
            {
                JFreeChart chart = chartSource.getChart( bundle, config );
                String fileName = chartSource.getFileName( config );
                log.debug("Rendering summary chart " + fileName);
                renderer.renderChart( fileName, chart );
            }
        }
        if (config.isShowdetails())
        {
            for ( List<ChartSource> sources : detailsChartSources.values() )
            {
                for ( ChartSource source : sources )
                {
                    if ( source.isEnabled( config ) )
                    {
                        JFreeChart chart = source.getChart( bundle, config );
                        String fileName = source.getFileName( config );
                        log.debug("Rendering detailed chart " + fileName);
                        renderer.renderChart( fileName, chart );
                    }
                }
            }
        }
    }

    public List<ChartSource> getSummaryChartSources()
    {
        return summaryChartSources;
    }

    public List<ChartSource> getDetailsChartSources( String testName )
    {
        return detailsChartSources.get( testName );
    }
}
