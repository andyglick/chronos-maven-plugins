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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/HistoricSample.java $
  * $Id: HistoricSample.java 15773 2012-01-24 12:50:47Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is a historic sample representing the statistics from a previous run.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public class HistoricSample {
    private static final int DEFAULT_DURATION = 20000;

    private long timestamp;

    private double gcRatio = -1d;

    private double collectedPrSecond = -1d;

    private double responsetimeAverage = -1d;

    private double responsetime95Percentile = -1d;

    private Map<String, Double> individualPercentiles;

    private Map<String, Double> individualAverages;

    private double maxAverageThroughput = -1d;

    public HistoricSample(GroupedResponsetimeSamples responseSamples, GCSamples gcSamples) {
        timestamp = responseSamples.getAbsoluteTestStartTime();
        responsetimeAverage = responseSamples.getAllSamples().getAverage();
        responsetime95Percentile = responseSamples.getAllSamples().getPercentile95();
        individualAverages = new HashMap<String, Double>();
        individualPercentiles = new HashMap<String, Double>();
        Iterator it = responseSamples.getSampleGroups().iterator();
        while (it.hasNext()) {
            ResponsetimeSampleGroup group = (ResponsetimeSampleGroup) it.next();
            individualAverages.put(group.getName(), group.getAverage());
            individualPercentiles.put(group.getName(), group.getPercentile95());
        }
        if (gcSamples != null) {
            gcRatio = gcSamples.getGarbageCollectionRatio(responseSamples.getTotalTime());
            collectedPrSecond = gcSamples.getCollectedKBPerSecond(responseSamples.getTotalTime());
        }
        int averageDuration = Math.max(DEFAULT_DURATION, (int) responsetime95Percentile);
        maxAverageThroughput = responseSamples.getMaxAverageThroughput(averageDuration);
    }

    public HistoricSample() {
        // Do nothing
    }

    /**
     * @return Returns the timestamp.
     */
    public final long getTimestamp() {
        return timestamp;
    }

    /**
     * @return Returns the gcRatio.
     */
    public final double getGcRatio() {
        return gcRatio;
    }

    /**
     * @return Returns the collectedPrSecond.
     */
    public final double getCollectedPrSecond() {
        return collectedPrSecond;
    }

    /**
     * @return Returns the responsetimeAverage.
     */
    public final double getResponsetimeAverage() {
        return responsetimeAverage;
    }

    /**
     * @return Returns the responsetime95Percrntile.
     */
    public final double getResponsetime95Percentile() {
        return responsetime95Percentile;
    }

    public final Set<String> getGroupNames() {
        return individualAverages.keySet();
    }

    public final double getResponsetimeAverage(String groupName) {
        return individualAverages.get(groupName);
    }

    public final double getResponsetimePercentiles(String groupName) {
        return individualPercentiles.get(groupName);
    }

    /**
     * @return Returns the maxAverageThroughput.
     */
    public final double getMaxAverageThroughput() {
        return maxAverageThroughput;
    }

    public final void writeTo(XMLStreamWriter writer) throws XMLStreamException {

        writer.writeStartElement("history");
        writer.writeAttribute("timestamp", Long.toString(timestamp));

        writer.writeAttribute("gcRatio", Double.toString(gcRatio));
        writer.writeAttribute("collectedPrSecond", Double.toString(collectedPrSecond));

        writer.writeAttribute("responsetimeAverage", Double.toString(responsetimeAverage));
        writer.writeAttribute("responsetime95Percentile", Double.toString(responsetime95Percentile));
        writer.writeAttribute("maxAverageThroughput", Double.toString(maxAverageThroughput));

        writer.writeStartElement("individualPercentiles");

        for (Entry<String, Double> entry : individualPercentiles.entrySet()) {
            writer.writeEmptyElement("entry");
            writer.writeAttribute("key", entry.getKey());
            writer.writeAttribute("value", entry.getValue().toString());
        }

        writer.writeEndElement(); // end individualPercentiles

        writer.writeStartElement("individualAverages");

        for (Entry<String, Double> entry : individualAverages.entrySet()) {
            writer.writeEmptyElement("entry");
            writer.writeAttribute("key", entry.getKey());
            writer.writeAttribute("value", entry.getValue().toString());
        }
        writer.writeEndElement(); // end individualAverages

        writer.writeEndElement(); // end history
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setGcRatio(double gcRatio) {
        this.gcRatio = gcRatio;
    }

    public void setCollectedPrSecond(double collectedPrSecond) {
        this.collectedPrSecond = collectedPrSecond;
    }

    public void setResponsetimeAverage(double responsetimeAverage) {
        this.responsetimeAverage = responsetimeAverage;
    }

    public void setResponsetime95Percentile(double responsetime95Percentile) {
        this.responsetime95Percentile = responsetime95Percentile;
    }

    public void setMaxAverageThroughput(double maxAverageThroughput) {
        this.maxAverageThroughput = maxAverageThroughput;
    }

    public void setIndividualPercentiles(Map<String, Double> individualPercentiles) {
        this.individualPercentiles = individualPercentiles;
    }

    public void setIndividualAverages(Map<String, Double> individualAverages) {
        this.individualAverages = individualAverages;
    }
}
