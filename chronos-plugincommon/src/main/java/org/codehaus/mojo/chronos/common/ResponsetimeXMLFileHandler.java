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


import org.codehaus.mojo.chronos.common.model.ResponsetimeSampleGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SAX parser for the response time XML.
 *
 * Not thread safe.
 *
 * @author Dragisa Krsmanovic
 */
public class ResponsetimeXMLFileHandler extends DefaultHandler {

    private boolean inSampleGroup = false;

    private boolean insideGroups = false;
    private boolean insideSample = false;

    private List<ResponsetimeSampleGroup> groupedResponsetimeSamples = new ArrayList<ResponsetimeSampleGroup>(  );
    private DateFormat dateFormat;
    private long absoluteTestStart = 0;
    private ResponsetimeSampleGroup currentGroup = null;



    public ResponsetimeXMLFileHandler(File file) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(file, this);
    }

    /**
     * @param uri        See {@link DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param localName  See {@link DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param qName      See {@link DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @param attributes See {@link DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @throws org.xml.sax.SAXException See {@link DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if ("groupedresponsetimesamples".equals(qName)) {
            absoluteTestStart = Long.parseLong( attributes.getValue( "starttime" ) );
            this.dateFormat = new SimpleDateFormat( attributes.getValue( "dateformat" ) );
            insideGroups = true;
        } else if ("responsetimesamplegroup".equals(qName)) {
            if (!insideGroups) {
                throw new SAXException("responsetimesamplegroup should be inside groupedresponsetimesamples");
            }
            currentGroup = new ResponsetimeSampleGroup(
                    attributes.getValue("name"), 
                    Integer.parseInt(attributes.getValue("index")));
            inSampleGroup = true;
        } else if ("sample".equals(qName)) {
            if (!inSampleGroup) {
                throw new SAXException("sample should be inside responsetimesamplegroup");
            }
            currentGroup.addSample( Long.parseLong(attributes.getValue("timestamp")),
                    Integer.parseInt(attributes.getValue("responsetime")),
                    "true".equalsIgnoreCase(attributes.getValue("success")),
                    attributes.getValue("threadId"));
            insideSample = true;
        }
    }

    /**
     * @param uri       See {@link DefaultHandler#endElement(String, String, String)}
     * @param localName See {@link DefaultHandler#endElement(String, String, String)}
     * @param qName     See {@link DefaultHandler#endElement(String, String, String)}
     * @throws SAXException See {@link DefaultHandler#endElement(String, String, String)}
     * @see DefaultHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("groupedresponsetimesamples".equals(qName)) {
            if (insideSample || inSampleGroup) {
                throw new SAXException("responsetimesamplegroup not properly ended. Encountered end of groupedresponsetimesamples.");
            }
            insideGroups = false;
        } else if ("responsetimesamplegroup".equals(qName)) {
            if (insideSample) {
                throw new SAXException("sample not properly ended. Encountered end of responsetimesamplegroup.");
            }
            groupedResponsetimeSamples.add(currentGroup);
            currentGroup = null;
            inSampleGroup = false;
        } else if ("sample".equals(qName)) {
            insideSample = false;
        }
    }

    public final long getAbsoluteTestStart() {
        return absoluteTestStart;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }


    public final Iterable<ResponsetimeSampleGroup> getSampleGroups() {
        return groupedResponsetimeSamples;
    }
}
