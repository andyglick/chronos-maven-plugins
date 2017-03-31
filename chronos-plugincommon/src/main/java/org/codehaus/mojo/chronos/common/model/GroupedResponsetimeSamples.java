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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/GroupedResponsetimeSamples.java $
  * $Id: GroupedResponsetimeSamples.java 15773 2012-01-24 12:50:47Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.codehaus.mojo.chronos.common.IOUtil;
import org.codehaus.mojo.chronos.common.ResponsetimeXMLFileHandler;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A grouping collection of samples (grouped by the name of the samples).
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public final class GroupedResponsetimeSamples
{
    private final ResponsetimeSamples samples = new ResponsetimeSamples();
    private final SortedMap<String, ResponsetimeSampleGroup> sampleGroupsByName =
        new TreeMap<String, ResponsetimeSampleGroup>();

    private final long absoluteTestStart;
    private final DateFormat dateFormat;

    protected final Min relativeTestStart = new Min();

    protected final Max relativeTestEnd = new Max();

    public GroupedResponsetimeSamples( File file)
        throws IOException, SAXException, ParserConfigurationException
    {
        samples.responseTimeSeries =
            new TimeSeries( "Individual samples" ); // label shown for the series in the responsetime graph

        IOUtil.copyDTDToDir( "chronos-responsetimesamples.dtd", file.getParentFile() );
        ResponsetimeXMLFileHandler xmlFileHandler = new ResponsetimeXMLFileHandler( file );
        this.absoluteTestStart = xmlFileHandler.getAbsoluteTestStart();
        dateFormat = xmlFileHandler.getDateFormat();
        for ( ResponsetimeSampleGroup sampleGroup : xmlFileHandler.getSampleGroups() )
        {
            add( sampleGroup );
        }
    }

    private void add( ResponsetimeSampleGroup group )
    {
        sampleGroupsByName.put( group.getName(), group );
        for ( ResponsetimeSample sample : group.getSamples() )
        {
            samples.add( sample );
            relativeTestStart.increment( sample.getStartTime() );
            relativeTestEnd.increment( ( sample.getEndTime() ) );
        }
    }

    public ResponsetimeSamples getAllSamples()
    {
        return samples;
    }

    public DateFormat getDateFormat()
    {
        return dateFormat;
    }

    /**
     * Retrieve the total execution time of the <code>ResponsetimeSample</code> instances.
     *
     * @return Total execution time in milliseconds.
     */
    public final long getTotalTime()
    {
        return (long) (relativeTestEnd.getResult() - relativeTestStart.getResult());
    }

    public final long getAbsoluteTestStartTime()
    {
        return absoluteTestStart;
    }

    /**
     * Retrieve the maximum average throughput of the <code>ResponsetimeSample</code> instances.
     *
     * @return the maximum of a moving average of the throughput
     */
    public final double getMaxAverageThroughput( int averageduration )
    {
        TimeSeries series = createMovingThroughput( "" );
        TimeSeries averageseries = MovingAverage.createMovingAverage( series, "", averageduration, 0 );
        double max = 0;
        for ( Iterator it = averageseries.getItems().iterator(); it.hasNext(); )
        {
            TimeSeriesDataItem item = (TimeSeriesDataItem) it.next();
            if ( item.getValue() != null )
            {
                max = Math.max( max, item.getValue().doubleValue() );
            }
        }
        return max;
    }

    public final TimeSeries createMovingThroughput( String name )
    {
        return extractTimeSeries( name, new IncrementCalculator()
        {
            public double getIncrement( Second second, long samleStart, long sampleEndTime )
            {
                double coveredPart = getCoveredPart( second, samleStart, sampleEndTime );
                return coveredPart / ( sampleEndTime - samleStart );
            }
        } );
    }

    public TimeSeries getThreadCount()
    {
        return extractTimeSeries( "Threads", new IncrementCalculator()
        {
            public double getIncrement( Second second, long samleStart, long sampleEndTime )
            {
                double coveredPart = getCoveredPart( second, samleStart, sampleEndTime );
                return coveredPart / 1000.0d;
            }
        } );
    }


    private TimeSeries extractTimeSeries( String name, IncrementCalculator incrementCalculator )
    {
        TimeSeries series = new TimeSeries( name );
        for ( ResponsetimeSample sample : samples )
        {
            long sampleStart = sample.getStartTime();
            long sampleEndTime = sample.getEndTime();
            Second second = ModelUtil.createSecond( sampleStart );
            while ( second.getFirstMillisecond() <= sampleEndTime )
            {
                double increment = incrementCalculator.getIncrement( second, sampleStart, sampleEndTime );
                updateSeries( series, second, increment );
                second = ModelUtil.next( second );
            }
        }
        return series;
    }

    private interface IncrementCalculator
    {
        double getIncrement( Second second, long samleStart, long sampleEndTime );
    }


    private static void updateSeries( TimeSeries series, Second second, double increment )
    {
        Number old = series.getValue( second );
        if ( old == null )
        {
            old = 0d;
        }
        double newValue = old.doubleValue() + increment;
        series.addOrUpdate( second, newValue );
    }

    private static double getCoveredPart( Second second, long sampleStart, long sampleEnd )
    {
        double firstCovered = Math.max( sampleStart, second.getFirstMillisecond() );
        double lastCovered = Math.min( sampleEnd, second.getLastMillisecond() );
        return lastCovered - firstCovered + 1;
    }


    /**
     * @return a list of {@link ResponsetimeSampleGroup} sorted by the name of the group
     */
    public List<ResponsetimeSampleGroup> getSampleGroups()
    {
        return new ArrayList<ResponsetimeSampleGroup>( sampleGroupsByName.values() );
    }

}