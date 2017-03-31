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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/chart/ResponseChartGenerator.java $
  * $Id: ResponseChartGenerator.java 17280 2012-08-09 11:55:34Z soelvpil $
  */
package org.codehaus.mojo.chronos.report.chart;

import org.codehaus.mojo.chronos.common.model.ResponsetimeSamples;
import org.codehaus.mojo.chronos.report.ReportConfig;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.text.DateFormat;
import java.util.ResourceBundle;

/**
 * This class is responsible for generating responsetime charts.
 *
 * @author ksr@lakeside.dk
 */
public abstract class ResponseChartGenerator
{
    protected final JFreeChart createResponseChart( ResponsetimeSamples samples, ResourceBundle bundle,
                                                    ReportConfig config, DateFormat dateFormat )
    {
        XYDataset dataset = createResponseDataset( samples, bundle, config );

        String title = bundle.getString( "chronos.label.responsetimes" );
        String timeAxisLabel = bundle.getString( "chronos.label.responsetimes.time" );
        String valueAxisLabel = bundle.getString( "chronos.label.responsetimes.responsetime" );
        JFreeChart chart = ChartUtil.createTimeSeriesChart( dataset, title, timeAxisLabel, valueAxisLabel );
        ChartUtil.setupXYPlot( chart, dateFormat );
        // change rendering order - so average is in front
        chart.getXYPlot().setSeriesRenderingOrder( SeriesRenderingOrder.FORWARD );

        if ( config.isShowpercentile95() )
        {
            String text = bundle.getString( "chronos.label.percentile95.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getPercentile95() );
        }
        if ( config.isShowpercentile99() )
        {
            String text = bundle.getString( "chronos.label.percentile99.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getPercentile99() );
        }
        if ( config.isShowaverage() )
        {
            String text = bundle.getString( "chronos.label.average.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getAverage() );
        }
        return chart;
    }

    private TimeSeriesCollection createResponseDataset( ResponsetimeSamples samples, ResourceBundle bundle,
                                                        ReportConfig config )
    {
        TimeSeries series = samples.createResponseTimeSeries();
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries( series );

        String averageLabel = bundle.getString( "chronos.label.average" );
        TimeSeries averageseries =
            MovingAverage.createMovingAverage( series, averageLabel, config.getAverageduration(), 0 );
        dataset.addSeries( averageseries );
        return dataset;
    }
}
