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
 * $HeadURL: https://svn.codehaus.org/mojo/tags/chronos-1.0/chronos-jmeter-maven-plugin/src/main/java/org/codehaus/mojo/chronos/check/CheckMojo.java $
 * $Id: CheckMojo.java 14893 2011-10-24 12:08:52Z soelvpil $
 */
package org.codehaus.mojo.chronos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.chronos.data.HibernateUtil;
import org.codehaus.mojo.chronos.data.TestCase;
import org.codehaus.mojo.chronos.data.TestCaseResult;
import org.codehaus.mojo.chronos.data.Thresholds;
import org.codehaus.mojo.chronos.xml.GroupedResponseTimeSamples;
import org.codehaus.mojo.chronos.xml.ResponseTimeSampleGroup;
import org.codehaus.plexus.util.DirectoryScanner;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Goal which collects the execution of each performed unittest, and compares the execution time to previous measured<br/>
 * execution times for the same test.<br />
 * If the new execution time is larger then the old ones, the build will fail with a list of non-complient tests.
 * 
 * @goal collect
 * 
 * @phase verify
 */
public class TimerMojo extends AbstractMojo {

	private static final String TESTCASE_TAG = "testcase";

	private List<String> compliencyProblems = new ArrayList<String>();

    /**
   	 * The id of the data, to create a report from.
   	 *
   	 * @parameter default-value = "performancetest"
   	 */
   	protected String dataid;

	/**
	 * The directory where surefire reports results are stored.
	 * 
	 * @parameter expression="${basedir}/target/surefire-reports"
	 */
	/* pp */File surefiredir;

	/**
	 * The directory to store chronos timing output.
	 * 
	 * @parameter expression="${basedir}/target/chronos"
	 */
	/* pp */File chronosdir;

	/**
	 * The number of (latest) results to include in the final graphs.<br />
	 * The more result - the longer processing time!
	 * 
	 * @parameter default-value="200"
	 */
	/* pp */int maxnumberofresults;

	/**
	 * The maximum difference allowed between two measurements in percentage.<br />
	 * Setting this to 0 (or less) will cause many failed builds, since there will almost always be a small discrepancy between two identical runs.
	 * 
	 * @parameter default-value="5"
	 */
	/* pp */int discrepancy;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
      */
    private File testSourceDirectory;

    /**
   	 * Instructs chronos-timing to either fail or ignore the descrepancy (if any).
   	 *
   	 * @parameter default-value="true"
   	 */
   	/* pp */boolean failbuild;


	/**
	 * List of test cases to exclude from reporting and validation
	 *
	 * @parameter
	 */
	private String[] excludes;

    /**
   	 * List of test cases to include in reporting and validation
   	 *
   	 * @parameter
   	 */
    private String[] includes;

    /**
   	 * List of test cases to exclude from discrepancy tests.<br />
   	 * Chronos will still collect and report the execution time, but will not fail the build.
   	 *
   	 * @parameter
   	 */
    private String[] reports;

	/**
	 * Many unittests only takes a short time (< 100 m.seconds). Such tests will usually fail in chronos-surefire,<br />
	 * because the default discrepancy of 50 m.seconds is 5 m.seconds, which is below the precision of the execution time recorder.<br />
	 * In order to prevent such problems, chronos-timing will per default filter out tests with an execution time less the a certain minimum execution time.
	 * 
	 * @parameter default-value="100"
	 */
	private long minimumexecutiontime = 100;
	
	public void execute() throws MojoExecutionException {
		File[] xmlFiles = surefiredir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});

		final Set<String> testCases = new HashSet<String>();
        final Thresholds baseTimes;
        try
        {
            baseTimes = new Thresholds( discrepancy );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "unable to load basetimes", e );
        }

        for ( final File xmlFile : xmlFiles )
        {
            Element testsuiteXML = loadFile( xmlFile );

            List<Element> testCaseXMLs = testsuiteXML.getChildren( TESTCASE_TAG );
            for ( Element testCaseXML : testCaseXMLs )
            {
                final String classname = testCaseXML.getAttributeValue( "classname" );
                final String name = testCaseXML.getAttributeValue( "name" );
                final int executionTime = convertTime( testCaseXML.getAttributeValue( "time" ) );
                if ( !getIncludedTests().contains( asPath( classname ) ) )
                {
                    continue;
                }

                HibernateUtil.run( new Transactional()
                {
                    public void run( Session session )
                        throws MojoExecutionException
                    {
                        Query query = session.getNamedQuery("testcase.byClassAndName");
                        query.setString("classname", classname );
                        query.setString("name", name );

                        List<TestCase> res = query.list();
                        TestCase tc = res.isEmpty() ? null : res.get( 0 );
                        if ( tc == null && executionTime >= minimumexecutiontime )
                        {
                            tc = new TestCase( classname, name );
                        }
                        if ( tc != null )
                        {
                            checkExecutionTime( tc, executionTime, baseTimes );
                            tc.addExecution( true, xmlFile.lastModified(), executionTime );
                            session.saveOrUpdate( tc );
                            testCases.add( classname );
                        }
                    }
                } );
            }
        }

        verifyCompliancy();
        try
        {
            baseTimes.store();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "unable to store basetimes", e );
        }
        exportToXml( testCases );
	}

    private void verifyCompliancy()
        throws MojoExecutionException
    {
        if ( compliencyProblems.isEmpty() )
        {
            return;
        }
        for ( String compliencyProblem : compliencyProblems )
        {
            getLog().error( compliencyProblem );
        }
        if ( failbuild )
        {
            throw new MojoExecutionException( "Chronos-timing failed build due to compliency problems." );
        }
    }

    private int convertTime(String time) {
		double dTime = Double.parseDouble(time);
		dTime = dTime * 1000;

		return new Double(dTime).intValue();
	}

	private void exportToXml( final Set<String> testClasses ) throws MojoExecutionException {
        HibernateUtil.run( new Transactional()
        {
            public void run( Session session )
                throws MojoExecutionException
            {
                GroupedResponseTimeSamples samples = new GroupedResponseTimeSamples();
        		try {
        			for (String testClass : testClasses) {
                        Query query = session.getNamedQuery("testcase.byClass");
                        query.setString("classname", testClass );
                        List<TestCase> testCases = (List<TestCase>) query.list();
        				for (TestCase testCase : testCases) {
                            samples.addGroup(assempleGroup(testCase));
        				}
        			}
        		} catch (IOException ex) {
        			throw new MojoExecutionException("Error while saving results", ex);
        		}
                getOutputdir().mkdirs();
                try {
                    samples.dumpResults(getOutputdir());
                } catch (IOException ex) {
                    throw new MojoExecutionException("Error while saving results", ex);
                }
            }
        } );
	}

    private File getOutputdir() {
        return new File( chronosdir, dataid );
    }

	private ResponseTimeSampleGroup assempleGroup(TestCase testCase) throws IOException {
		ResponseTimeSampleGroup tr = new ResponseTimeSampleGroup(testCase.getTestIdentifier());
		int count = 0;
        int startIndex = testCase.getResults().size() - maxnumberofresults;
		for (TestCaseResult tcr : testCase.getResults()) {
			if (count >= startIndex) {
                tr.addMeasurement( tcr.getTimeOfMeasurement(), tcr.getTime(), tcr.isSuccess(), tcr.getIdentifier() );
            }
            count++;
        }
		return tr;
	}

    private void checkExecutionTime( TestCase tc, int executionTime, Thresholds basetimes )
    {
        if ( getValidatedTests().contains(asPath( tc.getClassname() ) ) )
        {
            boolean valid = basetimes.validate( tc, executionTime );

            if ( !valid )
            {
                compliencyProblems.add(
                    tc.getTestIdentifier() + " failed with an execution executionTime of " + executionTime + " against a base executionTime of "
                        + basetimes.getBaseTime( tc ) );
            }
        }

    }

    private Set<String> getIncludedTests()
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( testSourceDirectory );
        scanner.setIncludes( merge( includes, reports ) );
        scanner.setExcludes( excludes );
        scanner.scan();
        Set<String> res = new HashSet<String>( );
        for (String filePath : scanner.getIncludedFiles()) {
            res.add( filePath );
        }
        return res;
    }

    private Set<String> getValidatedTests() {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( testSourceDirectory );
        scanner.setIncludes( includes );
        scanner.setExcludes( merge(  excludes, reports ) );
        scanner.scan();
        Set<String> res = new HashSet<String>( );
        for (String filePath : scanner.getIncludedFiles()) {
            res.add( filePath );
        }
        return res;
    }

    private String asPath(String className) {
        return className.replace( '.', File.separatorChar ) + ".java";
    }

    private static String[] merge(String[] src1, String[] src2) {
        if ( src1 == null )
        {
            src1 = new String[0];
        }
        if ( src2 == null )
        {
            src2 = new String[0];
        }
        String[] dest = new String[src1.length + src2.length];
        System.arraycopy( src1, 0, dest, 0, src1.length );
        System.arraycopy( src2, 0, dest, src1.length, src2.length );
        return dest;
    }

    private Element loadFile(File xmlFile) throws MojoExecutionException {
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(xmlFile);
			return doc.getRootElement();
		} catch (JDOMException e) {
			throw new MojoExecutionException("Error during parsing xml file.", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Error loading xml file.", e);
		}
	}
}