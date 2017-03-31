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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-jmeter-maven-plugin/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterLogParser.java $
  * $Id: JMeterLogParser.java 15990 2012-02-21 09:55:35Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.chronos.common.IOUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for parsing the jmeter log.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public final class JMeterLogParser
{
    private final SAXParser saxParser;
    private final XMLOutputFactory xmlOutputFactory;

    private final Log log;
    public static final String CHRONOS_RESPONSETIMESAMPLES_DTD = "chronos-responsetimesamples.dtd";

    public JMeterLogParser(Log log)
    {
        this.log = log;
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try
        {
            saxParser = saxParserFactory.newSAXParser();
        }
        catch ( ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }
        catch ( SAXException e )
        {
            throw new RuntimeException( e );
        }

        xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    /**
     * Convert the jmeter (jtl) log to Chronos XML.
     *
     * @param file The file to parse
     * @param output The Chronos XML file to write to
     * @throws SAXException If there is some XML related error in the logfile
     * @throws IOException  If the JMeter logfile cannot be read
     * @throws javax.xml.stream.XMLStreamException If it cannot write to Chronos XML file
     */
    public void convertToChronosXml( File file, File output )
            throws SAXException, IOException, XMLStreamException {

        JMeterSAXFileHandler saxHandler = new JMeterSAXFileHandler();
        log.debug("Parsing " + file.getName());
        saxParser.parse(file, saxHandler);
        log.debug("Writing Chronos document to " + output.getName());

        File directory = IOUtil.ensureDir(output.getParentFile());

        IOUtil.copyDTDToDir(CHRONOS_RESPONSETIMESAMPLES_DTD, directory);

        XMLStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(new BufferedOutputStream(new FileOutputStream(output)), "UTF-8");
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeDTD("<!DOCTYPE responsetimesamples PUBLIC \"SYSTEM\" \"" + CHRONOS_RESPONSETIMESAMPLES_DTD + "\">");
            saxHandler.writeTo(xmlStreamWriter);
        } finally {
            if (xmlStreamWriter != null) {
                xmlStreamWriter.close();
            }
        }
    }

}
