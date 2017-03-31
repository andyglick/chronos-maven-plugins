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
  * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-jmeter-maven-plugin/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterSAXFileHandler.java $
  * $Id: JMeterSAXFileHandler.java 17280 2012-08-09 11:55:34Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.commons.math.stat.descriptive.rank.Min;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSample;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * SAXHandler for JMeter xml logs.
 *
 * @author ksr@lakeside.dk
 * @author Dragisa Krsmanovic
 */
public final class JMeterSAXFileHandler
    extends DefaultHandler
{
    private static final String JUNIT_SAMPLER_20 = "org.apache.jmeter.protocol.java.sampler.JUnitSampler";

    private final Collector collector = new Collector();

    private Properties sampleAttributes;

    private boolean inProperty = false;

    private boolean insideSample = false;

    private StringBuffer testMethodNameSB = new StringBuffer();

    private Properties parentSampleAttributes;

    /**
     * @param uri        See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param localName  See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param qName      See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param attributes See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @throws SAXException See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        if ( "sampleResult".equals( qName ) )
        {
            // jtl20
            Properties props = new Properties();
            for ( int i = 0; i < attributes.getLength(); i++ )
            {
                props.put( attributes.getQName( i ), attributes.getValue( i ) );
            }
            sampleAttributes = props;
            insideSample = true;
        }
        else if ( "property".equals( qName ) )
        {
            // jtl20
            // TODO be sure that log cannot contain other types of character
            // data under junitSamples properties
            inProperty = true;
        }
        else if ( "httpSample".equals( qName ) || "sample".equals( qName ) )
        {
            // jtl21

            if ( insideSample )
            {
                parentSampleAttributes = sampleAttributes;
            }

            Properties props = new Properties();
            for ( int i = 0; i < attributes.getLength(); i++ )
            {
                props.put( attributes.getQName( i ), attributes.getValue( i ) );
            }
            sampleAttributes = props;
            insideSample = true;
        }
    }

    /**
     * this method can be called multiple times in one element if there's enough chars.
     *
     * @param ch     See {@link DefaultHandler#characters(char[], int, int)}
     * @param start  See {@link DefaultHandler#characters(char[], int, int)}
     * @param length See {@link DefaultHandler#characters(char[], int, int)}
     * @see DefaultHandler#characters(char[], int, int)
     */
    public void characters( char[] ch, int start, int length )
    {
        if ( insideSample && inProperty )
        {
            testMethodNameSB.append( new String( ch, start, length ) );
        }
    }

    /**
     * @param uri       See {@link DefaultHandler#endElement(String, String, String)}
     * @param localName See {@link DefaultHandler#endElement(String, String, String)}
     * @param qName     See {@link DefaultHandler#endElement(String, String, String)}
     * @throws SAXException See {@link DefaultHandler#endElement(String, String, String)}
     * @see DefaultHandler#endElement(String, String, String)
     */
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( "property".equals( qName ) )
        {
            inProperty = false;
        }
        else if ( "sampleResult".equals( qName ) )
        {
            // jtl20
            String embeddedPropertyValue = testMethodNameSB.toString();
            collectJtl20( embeddedPropertyValue, sampleAttributes );
            reset();
        }
        else if ( "httpSample".equals( qName ) || "sample".equals( qName ) )
        {
            // jtl21
            if ( !insideSample )
            {
                sampleAttributes = parentSampleAttributes;
            }

            collectJtl21(sampleAttributes);
            reset();
        }
    }

    private void collectJtl20( String embeddedPropertyValue, final Properties sampleAttributes )
    {
        final String sampleName;
        // it seems like when generated from Jmeter 2.1, the label will always
        // contain the String
        // 'org.apache.jmeter.protocol.java.sampler.JUnitSampler'
        String label = sampleAttributes.getProperty( "label" );
        if ( JUNIT_SAMPLER_20.equals( label ) && !"".equals( embeddedPropertyValue ) )
        {
            sampleName = embeddedPropertyValue;
        }
        else
        {
            sampleName = label;
        }
        int responsetime = Integer.parseInt( sampleAttributes.getProperty( "time" ) );
        long timestamp = Long.parseLong( sampleAttributes.getProperty( "timeStamp" ) );
        boolean success = "true".equals( sampleAttributes.getProperty( "success" ) );
        String threadId = sampleAttributes.getProperty( "threadName" ).intern();
        collector.collect( sampleName, responsetime, timestamp, success, threadId );
    }

    private void collectJtl21( final Properties sampleAttributes )
    {
        String sampleName = sampleAttributes.getProperty( "lb" );
        int responsetime = Integer.parseInt( sampleAttributes.getProperty( "t" ) );
        long timestamp = Long.parseLong( sampleAttributes.getProperty( "ts" ) );
        boolean success = "true".equals( sampleAttributes.getProperty( "s" ) );
        String threadId = sampleAttributes.getProperty( "tn" ).intern();
        collector.collect( sampleName, responsetime, timestamp, success, threadId );
    }


    private void reset()
    {
        testMethodNameSB.setLength( 0 );
        insideSample = false;
        sampleAttributes = null;
    }



    private static class Collector
    {
        private final SortedMap<String, List<ResponsetimeSample>> sampleGroupsByName = new TreeMap<String, List<ResponsetimeSample>>();

        /**
         * <code>int</code> representing the number of succeeded samples.
         */
        protected int succeeded;

        protected final Min testStart = new Min();

        private void collect(String sampleName, int responsetime, long timestamp, boolean success, String threadId)
        {
            testStart.increment( timestamp );
            if ( success )
            {
                succeeded++;
            }
            List<ResponsetimeSample> groupElement = sampleGroupsByName.get(sampleName);
            if ( groupElement == null )
            {
                groupElement = new ArrayList<ResponsetimeSample>(  );
                sampleGroupsByName.put(sampleName, groupElement);
            }

            groupElement.add(new ResponsetimeSample(responsetime, timestamp, success, threadId));
        }

        /**
         * Writes XML document using StAX
         */
        void writeTo(XMLStreamWriter writer) throws XMLStreamException {

            writer.writeStartElement("groupedresponsetimesamples");
            writer.writeAttribute("succeeded", Integer.toString(succeeded));
            writer.writeAttribute( "starttime",  Long.toString( (long) testStart.getResult()));
            writer.writeAttribute( "dateformat", "HH:mm:ss" );
            int index = 1;
            for ( String groupName : sampleGroupsByName.keySet() )
            {
                List<ResponsetimeSample> samples = sampleGroupsByName.get( groupName );
                writer.writeStartElement( "responsetimesamplegroup" );
                writer.writeAttribute( "name", groupName );
                writer.writeAttribute( "index", Integer.toString( index ) );
                for (ResponsetimeSample sample : samples) {
                    writer.writeEmptyElement( "sample" );
                    long relativeStartTime = sample.getStartTime() - (long) testStart.getResult();
                    writer.writeAttribute( "timestamp", Long.toString( relativeStartTime ) );
                    writer.writeAttribute( "responsetime", Integer.toString( sample.getResponsetime() ) );
                    writer.writeAttribute( "success", Boolean.toString( sample.isSuccess() ) );
                    writer.writeAttribute( "threadId", sample.getThreadId() );
                }
                writer.writeEndElement();
                index++;
            }
            writer.writeEndElement();
        }
    }


    /**
     * Writes XML document using StAX.
     */
    void writeTo(XMLStreamWriter writer) throws XMLStreamException {
        collector.writeTo(writer);
    }
}
