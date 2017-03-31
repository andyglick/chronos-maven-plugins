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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/GCSample.java $
  * $Id: GCSample.java 15770 2012-01-24 12:34:33Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Contains info from a garbagecollection logentry.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public final class GCSample
{

    private final double timestamp;

    private final int heapBefore;

    private final int heapAfter;

    private final int heapTotal;

    private final double processingTime;

    /**
     * Constructor for the <code>GCSample</code> clas.
     *
     * @param timestamp      The timestamp of the sample.
     * @param heapBefore     The size of the heap before the sample run.
     * @param heapAfter      The size of the heap after the sample run.
     * @param heapTotal      The total heap size during the sample run.
     * @param processingTime The procession time of the sample run.
     */
    public GCSample( double timestamp, int heapBefore, int heapAfter, int heapTotal, double processingTime )
    {
        this.timestamp = timestamp;
        this.heapAfter = heapAfter;
        this.heapBefore = heapBefore;
        this.heapTotal = heapTotal;
        this.processingTime = processingTime;
    }

    /**
     * @return Returns the timestamp.
     */
    public double getTimestamp()
    {
        return timestamp;
    }

    /**
     * @return Returns the heapBefore.
     */
    public int getHeapBefore()
    {
        return heapBefore;
    }

    /**
     * @return Returns the heapAfter.
     */
    public int getHeapAfter()
    {
        return heapAfter;
    }

    /**
     * @return Returns the heapTotal.
     */
    public int getHeapTotal()
    {
        return heapTotal;
    }

    /**
     * @return Returns the processingTime.
     */
    public double getProcessingTime()
    {
        return processingTime;
    }

    public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement("gcsample");
        writer.writeAttribute("timestamp", Double.toString(timestamp));
        writer.writeAttribute("heapBefore", Integer.toString(heapBefore));
        writer.writeAttribute("heapAfter", Integer.toString(heapAfter));
        writer.writeAttribute("heapTotal", Integer.toString(heapTotal));
        writer.writeAttribute("processingTime", Double.toString(processingTime));
    }
}