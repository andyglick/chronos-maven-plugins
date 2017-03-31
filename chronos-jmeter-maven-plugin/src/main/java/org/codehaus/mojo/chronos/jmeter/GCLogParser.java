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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-jmeter-maven-plugin/src/main/java/org/codehaus/mojo/chronos/jmeter/GCLogParser.java $
  * $Id: GCLogParser.java 15990 2012-02-21 09:55:35Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.chronos.common.IOUtil;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GCSample;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing garbage collection logs.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public final class GCLogParser
{
    
    private final Log log;
    private static final String CHRONOS_GC_DTD = "chronos-gc.dtd";
    private final XMLOutputFactory xmlOutputFactory;

    public GCLogParser(Log log) {
        this.log = log;
        xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    /**
     * Parses the garbage collection log.
     *
     * @param gcLogFile The garbage collection logfile
     * @throws IOException if the logfile could not be parsed
     */
    private GCSamples readGCSampleFile( File gcLogFile )
        throws IOException
    {
        FileReader fileReader = new FileReader( gcLogFile );
        LineNumberReader reader = new LineNumberReader( fileReader );

        GCSamples samples = new GCSamples();

        String line;
        StringBuilder concatLines = new StringBuilder();
        try
        {
            while ( ( line = reader.readLine() ) != null )
            {
                concatLines.append( line );
                if ( line.indexOf( "]" ) > -1 )
                { // end of the logentry
                    GCSample sample = parseGCLogItem( concatLines.toString() );
                    samples.add( sample );
                    concatLines = new StringBuilder();
                }
            }
        }
        finally
        {
            reader.close();
        }
        
        return samples;
    }

    /**
     * Runs a regular expression on source.
     *
     * @param source
     */
    private GCSample parseGCLogItem( String source )
    {
        String timeinstant = null, heapbeforeStr = null, heapafterStr = null, totalheap = null, processingtimeStr =
            null;

        Pattern pattern = Pattern.compile( "[0-9]*\\.[0-9]*:|[0-9]*K|[0-9]*\\.[0-9]*" );
        Matcher matcher = pattern.matcher( source );
        int index = 0;
        while ( matcher.find() )
        {
            if ( matcher.group().length() > 0 )
            {
                switch ( index )
                {
                    case 0:
                        timeinstant = matcher.group();
                        timeinstant = timeinstant.substring( 0, timeinstant.length() - 1 );
                        break;
                    case 1:
                        heapbeforeStr = matcher.group();
                        heapbeforeStr = heapbeforeStr.substring( 0, heapbeforeStr.length() - 1 );
                        break;
                    case 2:
                        heapafterStr = matcher.group();
                        heapafterStr = heapafterStr.substring( 0, heapafterStr.length() - 1 );
                        break;
                    case 3:
                        totalheap = matcher.group();
                        totalheap = totalheap.substring( 0, totalheap.length() - 1 );
                        break;
                    case 4:
                        processingtimeStr = matcher.group();
                        processingtimeStr = matcher.group();
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                index++;
            }
        }

        return new GCSample(
                Double.parseDouble(timeinstant), 
                Integer.parseInt(heapbeforeStr), 
                Integer.parseInt(heapafterStr),
                Integer.parseInt(totalheap), 
                Double.parseDouble(processingtimeStr));
    }

    public void convertToGCXml( File file, File output )
            throws SAXException, IOException, XMLStreamException {

        log.debug("Parsing " + file.getName());
        GCSamples samples = readGCSampleFile(file);                                                                                           //"gcsamples", "SYSTEM", "chronos-gc.dtd"
        log.debug("Writing GC document to " + output.getName());

        File directory = IOUtil.ensureDir(output.getParentFile());

        IOUtil.copyDTDToDir(CHRONOS_GC_DTD, directory);

        XMLStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(new BufferedOutputStream(new FileOutputStream(output)), "UTF-8");
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeDTD("<!DOCTYPE gcsamples PUBLIC \"SYSTEM\" \"" + CHRONOS_GC_DTD + "\">");
            samples.writeTo(xmlStreamWriter);
        } finally {
            if (xmlStreamWriter != null) {
                xmlStreamWriter.close();
            }
        }
    }

}
