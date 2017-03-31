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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/responsetime/ResponsetimeSamples.java $
  * $Id: ResponsetimeSamples.java 14459 2011-08-12 13:41:52Z soelvpil $
  */
package org.codehaus.mojo.chronos.common;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Parser. Parses the serverside logs, and store the output in a format more suitable for later analytics
 *
 * @author ksr@lakeside.dk
 */
public class ServerLogParser
{
    private final File logInput;

    private final File logOutput;
    
    private final File structureOutput;

    private Map<String, List<ServerLogLine>> parentId2Children = new HashMap<String, List<ServerLogLine>>();

    private Map<String, SortedSet<String>> structure = new HashMap<String, SortedSet<String>>();
    private SortedSet<String> rootNodes = new TreeSet<String>(  );

    public ServerLogParser( File logInput, File logOutput, File structureOutput )
    {
        this.logInput = logInput;
        this.logOutput = logOutput;
        this.structureOutput = structureOutput;
    }

    public void parse()
        throws IOException, XMLStreamException
    {

        final BufferedReader input = new BufferedReader( new FileReader( this.logInput ) );
        try {
            write(this.logOutput, new ServerLogAppender( input ));
        } finally {
            input.close();
        }
        
    }

    private void write( File file, XMLStreamAppender appender )
        throws IOException, XMLStreamException
    {
        XMLStreamWriter writer = createXmlStreamWriter( file );
        try
        {
            writer.writeStartDocument( "UTF-8", "1.0" );
            appender.appendContentTo( writer );
            writer.writeEndDocument();
        }
        finally
        {
            writer.close();
        }
    }

    private interface XMLStreamAppender
    {
        public void appendContentTo(XMLStreamWriter streamWriter)
            throws XMLStreamException, IOException;
    }

    private class ServerLogAppender implements XMLStreamAppender {
        private final BufferedReader input;

        private ServerLogAppender( BufferedReader input )
        {
            this.input = input;
        }

        public void appendContentTo( XMLStreamWriter streamWriter )
            throws XMLStreamException, IOException
        {
            streamWriter.writeStartElement( "serverlog" );
            while ( input.ready() )
            {
                append( new ServerLogLine( input.readLine() ), streamWriter );
            }
            streamWriter.writeEndElement();

            write( structureOutput, new StructureAppender() );
        }

    }

    private class StructureAppender implements XMLStreamAppender {
        public void appendContentTo( XMLStreamWriter streamWriter )
            throws XMLStreamException, IOException
        {
            streamWriter.writeStartElement( "structure" );
            for ( String rootPath : rootNodes )
            {
                writeStructure( streamWriter, rootPath );
            }
            streamWriter.writeEndElement();
        }

        private void writeStructure( XMLStreamWriter xmlStreamWriter, String path )
            throws XMLStreamException
        {
            SortedSet<String> children = structure.remove( path );
            if (children != null) {
                for (String childName : children) {
                    writeStructure( xmlStreamWriter, path + '.' + childName );
                }

            }
            xmlStreamWriter.writeStartElement( "node" );
            xmlStreamWriter.writeAttribute( "path", path );
            xmlStreamWriter.writeEndElement();
        }
    }


    private XMLStreamWriter createXmlStreamWriter( File file )
        throws FileNotFoundException, XMLStreamException
    {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream( new FileOutputStream( file ) );
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        return xmlOutputFactory.createXMLStreamWriter( bufferedOutputStream, "UTF-8" );
    }

    private void append( ServerLogLine newLine, XMLStreamWriter output )
        throws XMLStreamException
    {
        if ( newLine.parentUuid == null )
        {
            List<OutputLogEntry> outputEntries = flushTree( newLine );
            for ( OutputLogEntry entry : outputEntries )
            {
                entry.writeTo( output );
            }

        }
        else
        {
            List<ServerLogLine> siblings = parentId2Children.get( newLine.parentUuid );
            if ( siblings == null )
            {
                siblings = new ArrayList<ServerLogLine>();
                parentId2Children.put( newLine.parentUuid, siblings );
            }
            siblings.add( newLine );
        }
    }

    private List<OutputLogEntry> flushTree( ServerLogLine rootLine )
    {
        rootNodes.add( rootLine.name );
        return flushTree( rootLine, rootLine.uuid, rootLine.startTimeMillis, rootLine.name );
    }

    private List<OutputLogEntry> flushTree( ServerLogLine parent, String rootUuid, long startTimeRoot, String qualifiedName )
    {
        List<OutputLogEntry> outputEntries = new ArrayList<OutputLogEntry>();
        List<ServerLogLine> children = parentId2Children.remove( parent.uuid );
        if ( children != null )
        {
            for ( ServerLogLine child : children )
            {
                String qualifiedChildName = qualifiedName + '.' + child.name;
                outputEntries.addAll( flushTree( child, rootUuid, startTimeRoot, qualifiedChildName ) );
                appendStructure(parent.name, child.name);
            }
        }
        OutputLogEntry outputEntry = new OutputLogEntry( qualifiedName, rootUuid, startTimeRoot, parent.responsetimeNanos );
        outputEntries.add( outputEntry );
        return outputEntries;
    }

    private void appendStructure( String parentName, String childName )
    {
        SortedSet<String> children = structure.get( parentName );
        if (children == null) {
            children = new TreeSet<String>(  );
            structure.put( parentName, children );
        }
        children.add( childName );
    }

    private class OutputLogEntry
    {
        // the path from root to this entry
        private final String qualifiedName;

        private final String rootUUid;
        
        // the starttime of this entry's root
        private final long startTimeMillisRoot;

        // the responsetime
        private final long responseTime;


        private OutputLogEntry( String qualifiedName, String rootUUid, long startTimeMillisRoot, long responseTime )
        {
            this.qualifiedName = qualifiedName;
            this.rootUUid = rootUUid;
            this.startTimeMillisRoot = startTimeMillisRoot;
            this.responseTime = responseTime;
        }

        public void writeTo( XMLStreamWriter writer )
            throws XMLStreamException
        {
            writer.writeStartElement( "log" );
            writer.writeAttribute( "qualifiedname", qualifiedName );
            writer.writeAttribute( "rootuuid", rootUUid );
            writer.writeAttribute( "starttimemillis", String.valueOf( startTimeMillisRoot ) );
            writer.writeAttribute( "responsetime", String.valueOf( responseTime ) );
            writer.writeEndElement();
        }
    }

    private class ServerLogLine
    {
        private final String name;

        private final String uuid;

        private final String parentUuid;

        private final long startTimeMillis;

        private final long responsetimeNanos;

        private ServerLogLine( String line )
        {
            String[] elements = line.split( "," );
            if ( elements.length != 5 )
            {
                throw new IllegalArgumentException( "Incorrect number of elements in " + line );
            }
            name = elements[0];
            parentUuid = "null".equals( elements[1] ) ? null : elements[1];
            uuid = elements[2];
            startTimeMillis = Long.valueOf( elements[3] );
            responsetimeNanos = Long.valueOf( elements[4] );
        }

    }
}
