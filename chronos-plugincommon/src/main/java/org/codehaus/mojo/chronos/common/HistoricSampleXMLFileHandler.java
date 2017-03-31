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
  * $HeadURL:$
  * $Id:$
  */
package org.codehaus.mojo.chronos.common;


import org.codehaus.mojo.chronos.common.model.HistoricSample;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SAX parser for historic sample XML. Not thread safe.
 *
 * @author Dragisa Krsmanovic
 */
public class HistoricSampleXMLFileHandler extends DefaultHandler {

    private boolean inHistory = false;
    private boolean inPercentiles = false;
    private boolean inAverages = false;


    private HistoricSample historicSample = null;

    private Map<String, Double> currentPercentiles = null;
    private Map<String, Double> currentAverages = null;


    public HistoricSampleXMLFileHandler(File file) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(file, this);
    }

    /**
     * @param uri        See {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param localName  See {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param qName      See {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param attributes See {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @throws org.xml.sax.SAXException See {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if ("history".equals(qName)) {
            historicSample = new HistoricSample();
            historicSample.setTimestamp(Long.parseLong(attributes.getValue("timestamp")));
            historicSample.setGcRatio(Double.parseDouble(attributes.getValue("gcRatio")));
            historicSample.setCollectedPrSecond(Double.parseDouble(attributes.getValue("collectedPrSecond")));

            historicSample.setResponsetimeAverage(Double.parseDouble(attributes.getValue("responsetimeAverage")));
            historicSample.setResponsetime95Percentile(Double.parseDouble(attributes.getValue("responsetime95Percentile")));
            historicSample.setMaxAverageThroughput(Double.parseDouble(attributes.getValue("maxAverageThroughput")));
            inHistory = true;

        } else if ("individualPercentiles".equals(qName)) {
            if (!inHistory) {
                throw new SAXException("individualPercentiles should be inside history");
            }
            currentPercentiles = new HashMap<String, Double>();
            inPercentiles = true;
        } else if ("individualAverages".equals(qName)) {
            if (!inHistory) {
                throw new SAXException("individualAverages should be inside history");
            }
            currentAverages = new HashMap<String, Double>();
            inAverages = true;
        } else if ("entry".equals(qName)) {
            if (!inAverages & !inPercentiles) {
                throw new SAXException("entry should be inside individualPercentiles or individualAverages");
            }

            if (inAverages) {
               currentAverages.put(attributes.getValue("key"), Double.parseDouble(attributes.getValue("value")));
            } else if (inPercentiles) {
               currentPercentiles.put(attributes.getValue("key"), Double.parseDouble(attributes.getValue("value")));
            }
        }
    }

    /**
     * @param uri       See {@link org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)}
     * @param localName See {@link org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)}
     * @param qName     See {@link org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)}
     * @throws org.xml.sax.SAXException See {@link org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)}
     * @see org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("history".equals(qName)) {
            if (inAverages) {
                throw new SAXException("individualAverages not properly ended. Encountered end of history.");
            }
            if (inPercentiles) {
                throw new SAXException("individualPercentiles not properly ended. Encountered end of history.");
            }
            inHistory = false;

        } else if ("individualPercentiles".equals(qName)) {
            if (!inPercentiles) {
                throw new SAXException("individualPercentiles not properly ended.");
            }
            historicSample.setIndividualPercentiles(currentPercentiles);
            currentPercentiles = null;
            inPercentiles = false;
        } else if ("individualAverages".equals(qName)) {
            if (!inHistory) {
                throw new SAXException("individualAverages not properly ended.");
            }
            historicSample.setIndividualAverages(currentAverages);
            currentAverages = null;
            inAverages = false;
        }
    }


    public HistoricSample getHistoricSample() {
        return historicSample;
    }
}
