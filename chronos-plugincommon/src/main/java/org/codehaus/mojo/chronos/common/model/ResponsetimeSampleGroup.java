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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/ResponsetimeSampleGroup.java $
  * $Id: ResponsetimeSampleGroup.java 15687 2012-01-04 11:31:15Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import org.jfree.data.time.TimeSeries;

/**
 * @author ksr@lakeside.dk
 */
public class ResponsetimeSampleGroup
{
    private final String name;

    private final int index;

    private ResponsetimeSamples samples = new ResponsetimeSamples();

    public ResponsetimeSampleGroup( String name, int index )
    {
        this.name = name;
        samples.responseTimeSeries = new TimeSeries( name );
        this.index = index;
    }


    public final int getIndex()
    {
        return index;
    }

    /**
     * @return the name of this samplegroup
     */
    public final String getName()
    {
        return name;
    }

    public ResponsetimeSamples getSamples() {
        return this.samples;
    }

    public void addSample( long startTime, int responseTime, boolean succes, String threadId )
    {
        samples.add( new ResponsetimeSample( responseTime, startTime, succes, threadId ) );
    }

    public final double getAverage()
    {
        return samples.getAverage();
    }

    public final double getPercentile95()
    {
        return samples.getPercentile95();
    }

    public int size()
    {
        return samples.size();
    }

    public double getPercentile99()
    {
        return samples.getPercentile99();
    }

    public double getMax()
    {
        return samples.getMax();
    }

    public double getMin()
    {
        return samples.getMin();
    }

    public double getSuccessrate()
    {
        return samples.getSuccessrate();
    }

    public double getFailed()
    {
        return samples.getFailed();
    }
}
