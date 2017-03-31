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
 * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.1.0/chronos-surefire-maven-plugin/src/main/java/org/codehaus/mojo/chronos/data/Thresholds.java $
 * $Id: Thresholds.java 17385 2012-08-22 10:01:54Z soelvpil $
 */
package org.codehaus.mojo.chronos.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

public class Thresholds
{
    private final Properties props = new Properties();

    private final int discrepancy;

    public Thresholds( int discrepancyPercentage )
        throws IOException
    {
        this.discrepancy = discrepancyPercentage;
        File propsFile = new File( "chronos-surefire-basetimes.properties" );
        if ( propsFile.exists() )
        {
            Reader input = new BufferedReader( new FileReader( propsFile ) );
            try
            {
                props.load( input );
            }
            finally
            {
                input.close();
            }
        }
    }


    public void store()
        throws IOException
    {
        File propsFile = new File( "chronos-surefire-basetimes.properties" );
        Writer output = new BufferedWriter( new FileWriter( propsFile ) );
        try
        {
            props.store( output, "threshold times for unittest monitored by chronos-surefire" );
        }
        finally
        {
            output.close();
        }
    }

    public boolean validate( TestCase tc, int executionTime )
    {

        if ( !props.containsKey( tc.getTestIdentifier() ) )
        {
            props.setProperty( tc.getTestIdentifier(), String.valueOf( executionTime ) );
        }
        int baseTime = this.getBaseTime( tc );
        int max = baseTime * ( ( 100 + discrepancy ) / 100 );

        return executionTime <= max;
    }


    public int getBaseTime( TestCase testcase )
    {
        String identifier = testcase.getTestIdentifier();
        return Integer.valueOf( props.getProperty( identifier ) );
    }

}
