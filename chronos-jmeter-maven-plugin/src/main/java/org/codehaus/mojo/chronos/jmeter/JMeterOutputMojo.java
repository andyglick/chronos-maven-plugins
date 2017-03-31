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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterTestMojo.java $
  * $Id: JMeterTestMojo.java 14241 2011-06-30 13:23:24Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.IOUtil;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.ServerLogParser;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * Analyzes output from JMeter.<br />
 * Is used
 * by specifying one or more .jtl files as input and (possibly) a garbage collection logfile.
 *
 * @author ksr@lakeside.dk
 * @goal jmeteroutput
 * @phase post-integration-test
 */
public class JMeterOutputMojo
    extends AbstractMojo
{
    /**
     * The current maven project.
     *
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * The inputfile of the type .jtl.
     * Will be parsed.
     *
     * @parameter
     * @required
     */
    private File jmeterOutput;

    /**
     * The id of the jmeter invocation.
     *
     * @parameter default-value="performancetest"
     */
    private String dataid;

    /**
     * The name of an (optional) garbage collection logfile. Only used when loggc is set to true.
     *
     * @parameter
     */
    private File gclogfile;

    public void execute()
        throws MojoExecutionException
    {
        if ( !jmeterOutput.exists() )
        {
            throw new MojoExecutionException(
                "Invalid argument 'input', " + jmeterOutput.getPath() + " does not exist." );
        }

        final TestDataDirectory testDataDirectory = new ProjectBaseDir( project ).getDataDirectory( dataid );
        if ( jmeterOutput.isDirectory() )
        {
            File[] inputFiles = IOUtil.listFilesWithExtension( jmeterOutput, "jtl" );
            for ( File outputJtlFile : inputFiles )
            {
                convertJMeterOutput(testDataDirectory, outputJtlFile, getLog());
            }
        }
        else if ( isJtlFile( jmeterOutput ) )
        {
            // exists due to previous check
            convertJMeterOutput(testDataDirectory, jmeterOutput, getLog());
        }
        else
        { // input is a jmx file
            throw new MojoExecutionException(
                "Executing jmeter tests is not supported. We suggest using the jmeter-maven-plugin " );
        }


        convertGCLog(testDataDirectory, getGcLogFile(), getLog());
    }


    public static void convertGCLog(TestDataDirectory testDataDirectory, File gcLogFile, Log log)
        throws MojoExecutionException
    {
        if ( gcLogFile != null && gcLogFile.exists() )
        {
            try
            {
                log.debug("Parsing GC log file " + gcLogFile);
                new GCLogParser(log).convertToGCXml(gcLogFile, testDataDirectory.getGCLogFile());
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to parseJtl20 garbage collection log", e );
            } catch (SAXException e) {
                throw new MojoExecutionException( "Unable to parseJtl20 garbage collection log", e );
            } catch (XMLStreamException e) {
                throw new MojoExecutionException( "Unable to parseJtl20 garbage collection log", e );
            }
        }
    }

    /**
     * Parse the specified .jtl file and save the result under the supplied datadirectory
     *
     * @param testDataDirectory The id of the result.
     * @param jtlFile           <code>File</code> pointing to the .jtl file to parseJtl20.
     * @throws MojoExecutionException Thrown if the IO operation fails.
     */
    public static void convertJMeterOutput(TestDataDirectory testDataDirectory, File jtlFile, Log log)
        throws MojoExecutionException
    {
        try
        {
            String jtlName = IOUtil.removeExtension( jtlFile.getName() );
            new JMeterLogParser(log).convertToChronosXml( jtlFile , testDataDirectory.getResponsetimeSamplesFile( jtlName));
      }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Could not parse jmeter log", e );
        }
    }

    public static void convertServerLog( TestDataDirectory testDataDirectory, File serverLogInput, Log log )
        throws MojoExecutionException
    {
        File serverLogOutput = new File( testDataDirectory.getDirectory(), "serverlog.xml" );
        File serverStructureOutput = new File( testDataDirectory.getDirectory(), "serverstructure.xml" );
        log.info( "converting serverlog: " + serverLogInput );
        ServerLogParser serverLogParser = new ServerLogParser( serverLogInput, serverLogOutput, serverStructureOutput );
        try
        {
            serverLogParser.parse();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to parse serverlog", e );
        }
    }

    private boolean isJtlFile( File input )
    {
        return input.getName().endsWith( ".jtl" );
    }

    private File getGcLogFile()
    {
        if ( gclogfile != null )
        {
            return gclogfile;
        }
        File chronosDir = new ProjectBaseDir( getProject().getBasedir() ).getChronosDir();
        return new File( chronosDir, "gclog-" + dataid + ".txt" );
    }

    protected final MavenProject getProject()
    {
        return project;
    }

}
