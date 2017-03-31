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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/ResponsetimeSamples.java $
  * $Id: ResponsetimeSamples.java 15769 2012-01-24 12:09:52Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains info from a jmeter jtl file.
 *
 * @author ksr@lakeside.dk
 */
public class ResponsetimeSamples implements Iterable<ResponsetimeSample>
{

    private static final int PERCENTILE_95 = 95;

    private static final int PERCENTILE_99 = 99;

    /**
     * <code>List</code> containing loaded samples.
     */
    protected final List<ResponsetimeSample> samples = new ArrayList<ResponsetimeSample>();

    protected final DescriptiveStatistics responsetimeStats = new DescriptiveStatistics();

    protected TimeSeries responseTimeSeries;

    /**
     * <code>int</code> representing the number of succeeded samples.
     */
    protected int succeeded;

    /**
     * add a (hopefully successful) sample.
     *
     * @param sample <code>JMeterSample</code> to add.
     */
    protected void add( ResponsetimeSample sample )
    {
        responsetimeStats.addValue( sample.getResponsetime() );
        samples.add( sample );

        if ( sample.isSuccess() )
        {
            succeeded++;
        }
    }

    /**
     * Retrieve the number of samples contained by this <code>ResponsetimeSample</code> instance.
     *
     * @return the number of samples
     */
    public final int size()
    {
        return samples.size();
    }

    /**
     * Retrieve the success rate of the contained <code>ResponsetimeSample</code> instances.
     *
     * @return the successrate (in percentage)
     */
    public final double getSuccessrate()
    {
        return 100 * ( (double) succeeded ) / size();
    }

    /**
     * Retrieve the number of failed <code>ResponsetimeSample</code> instances.
     *
     * @return the number of failed samples
     */
    public final int getFailed()
    {
        return size() - succeeded;
    }

    /**
     * Retrieve average response time of the <code>ResponsetimeSample</code> instances.
     *
     * @return the average responsetime of all samples
     */
    public final double getAverage()
    {
        return responsetimeStats.getMean();
    }


    public double getMin()
    {
        return responsetimeStats.getMin();
    }

    /**
     * Retrieve the maximum response time of the <code>ResponsetimeSample</code> instances.
     *
     * @return the maximum responsetime of all samples
     */
    public final double getMax()
    {
        return responsetimeStats.getMax();
    }

    /**
     * Calculate the 95 % fractile of the <code>ResponsetimeSample</code> instances.
     *
     * @return the 95% fractile of all responsetimes
     */
    public final double getPercentile95()
    {
        return responsetimeStats.getPercentile( PERCENTILE_95 );
    }

    /**
     * Calculate the 99 % fractile of the <code>ResponsetimeSample</code> instances.
     *
     * @return the 99% fractile of all responsetimes
     */
    public final double getPercentile99()
    {
        return responsetimeStats.getPercentile( PERCENTILE_99 );
    }

    /**
     * Extracts all responsetimes.
     *
     * @return the responsetimes as an array
     */
    public final double[] extractResponsetimes()
    {
        return responsetimeStats.getValues();
    }

    /**
     * create a TimeSeries of responsetimes.
     * Note that if several requests are spawned at the same millisecond,
     * only one of them will be part of this TimeSeries
     * @return
     */
    public final TimeSeries createResponseTimeSeries()
    {
        if ( samples.isEmpty() )
        {
            return this.responseTimeSeries;
        }
        for ( ResponsetimeSample sample : samples )
        {
            Millisecond timestamp = ModelUtil.createMillis( sample.getStartTime() );
            Integer responseTime = sample.getResponsetime();
            this.responseTimeSeries.addOrUpdate( timestamp, responseTime );
        }
        return this.responseTimeSeries;
    }

    public Iterator<ResponsetimeSample> iterator()
    {
        return samples.iterator();
    }
}