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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/GCSamples.java $
  * $Id: GCSamples.java 15773 2012-01-24 12:50:47Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for {@link org.codehaus.mojo.chronos.common.model.GCSample}.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public class GCSamples
        implements Serializable {

    private final List<GCSample> samples = new ArrayList<GCSample>();

    public final void add(GCSample sample) {
        samples.add(sample);
    }

    public final int getSampleCount() {
        return samples.size();
    }

    public final void extractHeapBefore(TimeSeries heapBeforeSeries) {
        for (GCSample sample : samples) {
            heapBeforeSeries.addOrUpdate(getTimestamp(sample), sample.getHeapBefore());
        }
    }

    public final void extractHeapAfter(TimeSeries heapAfterSeries) {
        for (GCSample sample : samples) {
            heapAfterSeries.addOrUpdate(getTimestamp(sample), sample.getHeapAfter());
        }
    }

    public final double getGarbageCollectionRatio(long totalTime) {
        double totalProcessing = 0.0d;
        for (GCSample sample : samples) {
            totalProcessing += sample.getProcessingTime();
        }
        return totalProcessing / totalTime;
    }

    public final double getCollectedKBPerSecond(long totalTime) {
        double totalCollected = 0.0d;
        for (GCSample sample : samples) {
            totalCollected += (sample.getHeapBefore() - sample.getHeapAfter());
        }
        return (totalCollected / 1000) / totalTime;
    }

    private Millisecond getTimestamp(GCSample sample) {
        int milliseconds = (int) (sample.getTimestamp() * 1000);
        return ModelUtil.createMillis(milliseconds);
    }

    public void addAll(GCSamples tmp) {
        this.samples.addAll(tmp.samples);
    }

    public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("gcsamples");
        for (GCSample sample : samples) {
            sample.writeTo(writer);
        }
        writer.writeEndElement();
    }
}