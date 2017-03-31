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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/DependencyUtil.java $
  * $Id: DependencyUtil.java 14240 2011-06-30 12:33:52Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jfree.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manages dependencies for the current maven project (making them available for jmeter).
 *
 * @author ksr@lakeside.dk
 */
public final class DependencyUtil
{
    private String jmeterhome;

    private Log log;

    public DependencyUtil( String jmeterHome, Log log )
    {
        this.jmeterhome = jmeterHome;
        this.log = log;
    }

    public List<File> getDependencies( MavenProject project )
    {
        List<File> result = new ArrayList<File>();
        getDependencies( project, result, result );
        return result;
    }

    List copyDependencies( MavenProject project )
        throws IOException
    {
        final List<File> copied = new ArrayList<File>();
        List<File> compDeps = new ArrayList<File>();
        List<File> testDeps = new ArrayList<File>();
        getDependencies( project, compDeps, testDeps );
        File lib = new File( jmeterhome, "lib" );
        for (File artifactFile : compDeps)
        {
            File junitdir = new File( lib, "ext" );
            copyFileToDir( copied, artifactFile, junitdir );
        }
        for (File artifactFile : testDeps)
        {
            File junitdir = new File( lib, "junit" );
            copyFileToDir( copied, artifactFile, junitdir );
        }
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                cleanUpDependencies( copied );
            }
        } );
        return copied;
    }

    void cleanUpDependencies( List copied )
    {
        for ( Iterator iterator = copied.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();
            if ( file.exists() )
            {
                file.delete();
            }
        }
    }

    private void getDependencies( MavenProject project, List<File> compileTime, List<File> testTime )
    {
        Iterator<Artifact> it = project.getAttachedArtifacts().iterator();
        while ( it.hasNext() )
        {
            registerDependency( it.next(), compileTime, testTime );
        }
        registerDependency( project.getArtifact(), compileTime, testTime );
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        if ( transitiveDependencies != null )
        {
            for (Artifact dependency : transitiveDependencies) {
                registerDependency( dependency, compileTime, testTime );
            }
        }
    }

    private void registerDependency( Artifact artifact, List<File> compileTime, List<File> testTime )
    {
        if ( artifact == null )
        {
            log.warn( "Artifact not found. Note that if Your JMeter test contains JUnittestcases, "
                          + "You can only invoke this goal through the default lifecycle." );
            return;
        }
        File artifactFile = artifact.getFile();
        if ( artifactFile == null )
        {
            log.warn( "Artifact not found. Note that if Your JMeter test contains JUnittestcases, "
                          + "You can only invoke this goal through the default lifecycle." );
            return;
        }
        System.out.println("found dependency: " + artifact + " " + artifact.getScope() + "  " + artifactFile + "  " + artifact.getClassifier());
        if ( Arrays.asList( Artifact.SCOPE_COMPILE, Artifact.SCOPE_RUNTIME, Artifact.SCOPE_SYSTEM,
                            Artifact.SCOPE_IMPORT ).contains( artifact.getScope() ) || artifact.getScope() == null)
        {
            compileTime.add( artifactFile );
        }
        else if ( Arrays.asList( Artifact.SCOPE_TEST ).contains( artifact.getScope() ))
        {
            testTime.add( artifactFile );
        }
    }

    private void copyFileToDir( List copied, File file, File targetDirr )
        throws IOException
    {
        File target = new File( targetDirr, file.getName() );
        if (target.exists()) target.delete();

        target.createNewFile();
        InputStream input = new BufferedInputStream( new FileInputStream( file ) );
        OutputStream output = new BufferedOutputStream( new FileOutputStream( target ) );
        IOUtils.getInstance().copyStreams( input, output );
        output.close();
        input.close();
        log.debug( "Dependency copied to jmeter distribution at: " + target );
        copied.add( target );
    }
}
