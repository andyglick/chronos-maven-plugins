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

import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Representation of the datadirectory for a single performancetest.
 */
public class DefaultTestDataDirectory
    implements TestDataDirectory
{
    protected final File dataDirectory;

    protected final String name;


    DefaultTestDataDirectory( File chronosDir, String dataId )
    {
        this.dataDirectory = new File( chronosDir, dataId );
        this.name = dataId;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.codehaus.mojo.chronos.common.TestDataDirectory#readGCSamples()
      */
    public GCSamples readGCSamples()
        throws IOException, SAXException, ParserConfigurationException
    {
        File[] gcFiles = listFilesWith( GC_FILE_PREFIX, XML_FILE_EXTENSION );

        GCSamples samples = new GCSamples();
        if ( gcFiles != null )
        {
            for ( File gcFile : gcFiles )
            {
                GCSamples tmp = new GCSampleXMLFileHandler( gcFile ).getSamples();
                samples.addAll( tmp );
            }
        }
        return samples;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.codehaus.mojo.chronos.common.TestDataDirectory#readResponsetimeSamples()
      */
    public GroupedResponsetimeSamples readResponsetimeSamples()
        throws IOException, SAXException, ParserConfigurationException
    {
        File[] dirContent = listFilesWith( PERFORMANCESAMPLE_FILE_PREFIX, XML_FILE_EXTENSION );
        if ( dirContent.length > 1 )
        {
            throw new IllegalStateException( "Only a single xml file allowed" );
        }
        if ( dirContent.length == 0 )
        {
            throw new IllegalStateException( "No xml files found" );
        }
        return new GroupedResponsetimeSamples( dirContent[0] );
    }

    /*
    * (non-Javadoc)
    *
    * @see org.codehaus.mojo.chronos.common.TestDataDirectory#ensure()
    */
    public final TestDataDirectory ensure()
    {
        IOUtil.ensureDir( dataDirectory );
        return this;
    }

    /*
    * (non-Javadoc)
    *
    * @see org.codehaus.mojo.chronos.common.TestDataDirectory#getDirectory()
    */
    public final File getDirectory()
    {
        return dataDirectory;
    }

    /*
    * (non-Javadoc)
    *
    * @see org.codehaus.mojo.chronos.common.TestDataDirectory#getGCLogFile()
    */
    public final File getGCLogFile()
        throws IOException
    {
        ensure();
        return new File( dataDirectory, GC_FILE_PREFIX + "-" + name + '.' + XML_FILE_EXTENSION );
    }

    /*
    * (non-Javadoc)
    *
    * @see org.codehaus.mojo.chronos.common.TestDataDirectory#getResponsetimeSamplesFile(java.lang.String)
    */
    public final File getResponsetimeSamplesFile( String jtlName )
        throws IOException
    {
        ensure();
        return new File( dataDirectory,
                         IOUtil.getAdjustedFileName( jtlName, PERFORMANCESAMPLE_FILE_PREFIX, XML_FILE_EXTENSION ) );
    }

    /*
    * (non-Javadoc)
    *
    * @see org.codehaus.mojo.chronos.common.TestDataDirectory#listFilesWith(java.lang.String, java.lang.String)
    */
    protected final File[] listFilesWith( final String prefix, final String extension )
    {
        final FilenameFilter filenameFilter = new FilenameFilter()
        {
            public boolean accept( File parentDir, String name )
            {
                return name.startsWith( prefix + "-" ) && name.endsWith( "." + extension );
            }
        };
        return IOUtil.listFiles( dataDirectory, filenameFilter );
    }
}