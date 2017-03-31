package org.codehaus.mojo.chronos.common;

import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public interface TestDataDirectory {

    /**
     * Prefix for GC files.
     */
    String GC_FILE_PREFIX = "gc";
    String PERFORMANCESAMPLE_FILE_PREFIX = "perf";
    /**
     * Extension for XML files.
     */
    String XML_FILE_EXTENSION = "xml";

    /**
	 * Read the saved <code>GSSamples</code> with the specified id.<br />
	 *
	 * @return The corresponding <code>GCSamples</code> instance.
	 * @throws java.io.IOException Thrown if loading the contents fails.
	 */
	public abstract GCSamples readGCSamples() throws IOException, SAXException, ParserConfigurationException;

	/**
	 * Read the saved <code>GroupedResponsetimeSamples</code> with the specified id.<br />
	 *
	 * @return The corresponding <code>GroupedResponsetimeSamples</code> instance.
	 * @throws java.io.IOException Thrown if loading the contents fails.
	 */
	public abstract GroupedResponsetimeSamples readResponsetimeSamples() throws IOException, SAXException, ParserConfigurationException;

	public abstract File getResponsetimeSamplesFile(String jtlName) throws IOException;

	public abstract File getGCLogFile() throws IOException;

	public abstract TestDataDirectory ensure();

	public abstract File getDirectory();

}