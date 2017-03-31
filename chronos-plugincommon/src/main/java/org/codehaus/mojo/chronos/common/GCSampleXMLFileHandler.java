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


import org.codehaus.mojo.chronos.common.model.GCSample;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * SAX Parser for GC sample file. Not thread safe.
 *
 * @author Dragisa Krsmanovic
 */

public class GCSampleXMLFileHandler extends DefaultHandler {

    private boolean inSamples = false;


    private GCSamples samples = null;


    public GCSampleXMLFileHandler(File file) throws IOException, SAXException, ParserConfigurationException {
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
        if ("gcsamples".equals(qName)) {
            samples = new GCSamples();
            inSamples = true;
        } else if ("gcsample".equals(qName)) {
            if (!inSamples) {
                throw new SAXException("gcsample should be inside gcsamples");
            }
            double timestamp = Double.parseDouble(attributes.getValue("timestamp"));
            int heapBefore = Integer.parseInt(attributes.getValue(("heapBefore")));
            int heapAfter = Integer.parseInt(attributes.getValue(("heapAfter")));
            int heapTotal = Integer.parseInt(attributes.getValue(("heapTotal")));
            double processingTime = Double.parseDouble(attributes.getValue(("processingTime")));
            samples.add(new GCSample(timestamp, heapBefore, heapAfter, heapTotal, processingTime));
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
        if ("gcsamples".equals(qName)) {
          inSamples = false;
        }
    }


    public GCSamples getSamples() {
        return samples;
    }
}
