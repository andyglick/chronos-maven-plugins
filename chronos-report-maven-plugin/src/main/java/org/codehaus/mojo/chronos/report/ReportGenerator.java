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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/ReportGenerator.java $
  * $Id: ReportGenerator.java 17316 2012-08-15 10:44:47Z soelvpil $
  */
package org.codehaus.mojo.chronos.report;

import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSampleGroup;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSamples;
import org.codehaus.mojo.chronos.report.chart.ChartSource;
import org.codehaus.mojo.chronos.report.chart.GraphGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Generates the JMeter report.
 *
 * @author ksr@lakeside.dk
 */
public final class ReportGenerator
{
    private static final String IMG_EXT = ".png";

    private NumberFormat formatter;

    private ReportConfig config;

    private ResourceBundle bundle;

    private ReportSink reportSink;

    private Sink sink;

    private GraphGenerator graphs;

    /**
     * @param bundle The {@link ResourceBundle} to extract messages from
     * @param config The {@link ReportConfig} of the report generation
     */
    public ReportGenerator( ResourceBundle bundle, ReportConfig config, GraphGenerator graphs )
    {
        this.formatter = new DecimalFormat( "#.#" );
        this.bundle = bundle;
        this.config = config;
        this.graphs = graphs;
    }

    /**
     * Generate a report (as an html page).
     *
     * @param aSink   The {@link Sink} to output the report content to
     * @param samples The {@link ResponsetimeSamples} to create a report from
     */
    public void doGenerateReport( Sink aSink, GroupedResponsetimeSamples samples )
    {
        this.reportSink = new ReportSink( bundle, aSink );
        this.sink = aSink;

        aSink.head();
        aSink.text( bundle.getString( "chronos.description" ) );
        aSink.head_();
        aSink.body();

        constructReportHeaderSection();
        if ( config.isShowsummary() )
        {
            constructReportSummarySection( samples );
        }
        if ( config.isShowdetails() )
        {
            constructIndividualTestsSection( samples );
        }
        aSink.body_();
        aSink.flush();
        aSink.close();
    }

    private void constructReportHeaderSection()
    {
        String title = config.getTitle();
        String description = config.getDescription();
        String anchor = "Report" + config.getId();

        reportSink.constructHeaderSection( title, description, anchor );

        Map metadataFile = parseMetadata( config.getMetadata() );
        String titleMetadata = bundle.getString( "chronos.label.summary" );
        String anchorMetadata = "Summary" + config.getId();
        reportSink.metadataTable( titleMetadata, anchorMetadata, metadataFile );
    }

    private Map parseMetadata( String metadata )
    {
        if ( metadata == null || "null".equals( metadata ) )
        {
            return Collections.emptyMap();
        }

        Map res = new LinkedHashMap();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader( new FileReader( new File( metadata ) ) );

            String strLine;
            while ( ( strLine = br.readLine() ) != null )
            {
                int tabIndex = strLine.indexOf( '\t' );
                if ( tabIndex == -1 )
                {
                    res.put( strLine, "&lt;no value&gt;" );
                }
                else
                {
                    res.put( strLine.substring( 0, tabIndex ), strLine.substring( tabIndex + 1 ) );
                }
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            try
            {
                if ( br != null )
                {
                    br.close();
                }
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
        }
        return res;
    }

    private void constructReportSummarySection( GroupedResponsetimeSamples samples )
    {
        String text = bundle.getString( "chronos.label.summary" );
        String anchor = "Summary" + config.getId();
        reportSink.title2( text, anchor );
        constructReportHotLinks();

        sink.table();
        sink.tableRow();
        reportSink.th( "chronos.label.tests" );
        if ( config.isShowpercentile95() )
        {
            reportSink.th( "chronos.label.percentile95" );
        }
        if ( config.isShowpercentile99() )
        {
            reportSink.th( "chronos.label.percentile99" );
        }
        reportSink.th( "chronos.label.averagetime" );
        reportSink.th( "chronos.label.iterations" );
        reportSink.th( "chronos.label.successrate" );
        sink.tableRow_();

        for ( ResponsetimeSampleGroup sampleGroup : samples.getSampleGroups() )
        {
            sink.tableRow();

            reportSink.sinkCellLink( sampleGroup.getName(), "#a" + sampleGroup.getIndex() + config.getId() );
            if ( config.isShowpercentile95() )
            {
                double percentile95 = sampleGroup.getPercentile95();
                reportSink.sinkCell( formatter.format( percentile95 ) );
            }
            if ( config.isShowpercentile99() )
            {
                double percentile99 = sampleGroup.getPercentile99();
                reportSink.sinkCell( formatter.format( percentile99 ) );
            }
            reportSink.sinkCell( formatter.format( sampleGroup.getAverage() ) );
            // Line merged from Atlessian.
            reportSink.sinkCell( "" + sampleGroup.size() );
            reportSink.sinkCell( formatter.format( sampleGroup.getSuccessrate() ) + " %" );
            sink.tableRow_();
        }
        sink.table_();
        reportSink.sinkLineBreak();

        for ( ChartSource chartSource : graphs.getSummaryChartSources() )
        {
            if ( chartSource.isEnabled( config ) )
            {
                reportSink.graphics( chartSource.getFileName( config ) + IMG_EXT );
            }
        }

    }

    private void constructIndividualTestsSection( GroupedResponsetimeSamples samples )
    {
        reportSink.title2( bundle.getString( "chronos.label.testcases" ), "Test_Cases" + config.getId() );

        for ( ResponsetimeSampleGroup sampleGroup : samples.getSampleGroups() )
        {
            constructReportHotLinks();
            reportSink.title3( sampleGroup.getName(), "a" + sampleGroup.getIndex() + config.getId() );

            List<String> headerLabels = new ArrayList<String>();
            List<String> dataLine = new ArrayList<String>();

            headerLabels.add( "chronos.label.mintime" );
            dataLine.add( formatter.format( sampleGroup.getMin() ) );
            headerLabels.add( "chronos.label.averagetime" );
            dataLine.add( formatter.format( sampleGroup.getAverage() ) );

            if (config.isShowpercentile95()) {
                headerLabels.add( "chronos.label.percentile95" );
                dataLine.add( formatter.format( sampleGroup.getPercentile95() ) );
            }


            headerLabels.add( "chronos.label.maxtime" );
            dataLine.add( formatter.format( sampleGroup.getMax() ) );
            headerLabels.add( "chronos.label.iterations" );
            dataLine.add( "" + sampleGroup.size() );
            headerLabels.add( "chronos.label.failures" );
            dataLine.add( formatter.format( sampleGroup.getFailed() ) );
            headerLabels.add( "chronos.label.successrate" );
            dataLine.add( formatter.format( sampleGroup.getSuccessrate() ) + " %" );

            reportSink.table( headerLabels, Collections.singletonList( dataLine ) );

            if ( config.isShowdetails() )
            {
                for ( ChartSource source : graphs.getDetailsChartSources( sampleGroup.getName() ) )
                {
                    if ( source.isEnabled( config ) )
                    {
                        reportSink.graphics( source.getFileName( config ) + IMG_EXT );
                    }
                }
            }
        }
        reportSink.sinkLineBreak();
    }

    private void constructReportHotLinks()
    {
        sink.section3();
        if ( config.isShowsummary() )
        {
            reportSink.sinkLink( bundle.getString( "chronos.label.summary" ), "Summary" + config.getId() );
        }

        if ( config.isShowdetails() )
        {
            reportSink.sinkLink( bundle.getString( "chronos.label.testcases" ), "Test_Cases" + config.getId() );
        }
        sink.section3_();
    }

}
