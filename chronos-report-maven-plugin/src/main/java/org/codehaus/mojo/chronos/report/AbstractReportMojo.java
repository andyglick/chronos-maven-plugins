package org.codehaus.mojo.chronos.report;

import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSampleGroup;
import org.codehaus.mojo.chronos.report.chart.ChartRenderer;
import org.codehaus.mojo.chronos.report.chart.ChartRendererImpl;
import org.codehaus.mojo.chronos.report.chart.ChartSource;
import org.codehaus.mojo.chronos.report.chart.DetailsHistogramChartSource;
import org.codehaus.mojo.chronos.report.chart.DetailsResponsetimeChartSource;
import org.codehaus.mojo.chronos.report.chart.GraphGenerator;
import org.codehaus.mojo.chronos.report.chart.SummaryGCChartSource;
import org.codehaus.mojo.chronos.report.chart.SummaryHistogramChartSource;
import org.codehaus.mojo.chronos.report.chart.SummaryResponsetimeChartSource;
import org.codehaus.mojo.chronos.report.chart.SummaryThroughputChartSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class AbstractReportMojo extends AbstractMavenReport {

	protected void generateReport(Locale locale) throws MavenReportException {
		try {
			final TestDataDirectory testDataDirectory = getTestDataDirectory();
			GroupedResponsetimeSamples jmeterSamples = testDataDirectory.readResponsetimeSamples();
			if (jmeterSamples.getAllSamples().size() == 0) {
				throw new MavenReportException("Response time samples not found for " + getDataId());
			}

			getLog().info("  tests: " + jmeterSamples.getSampleGroups().size());
			getLog().info("  jmeter samples: " + jmeterSamples.getAllSamples().size());

			// charts
			getLog().info(" generating charts...");

			GCSamples gcSamples = testDataDirectory.readGCSamples();

            List<ChartSource> summaryChartSources = new ArrayList<ChartSource>(  );
            summaryChartSources.add( new SummaryResponsetimeChartSource( jmeterSamples ) );
            summaryChartSources.add( new SummaryHistogramChartSource( jmeterSamples.getAllSamples() ) );
            summaryChartSources.add( new SummaryThroughputChartSource( jmeterSamples ) );
            summaryChartSources.add( new SummaryGCChartSource( gcSamples ) );

            Map<String, List<ChartSource>> detailsChartSources = new LinkedHashMap<String, List<ChartSource>>();
            for ( ResponsetimeSampleGroup sampleGroup : jmeterSamples.getSampleGroups() )
            {
                List<ChartSource> sources = new ArrayList<ChartSource>(  );
                DetailsResponsetimeChartSource source = new DetailsResponsetimeChartSource( sampleGroup,
                                                                                            jmeterSamples.getDateFormat() );
                DetailsHistogramChartSource detailsHistogramChartSource =
                    new DetailsHistogramChartSource( sampleGroup );
                sources.add( source );
                sources.add( detailsHistogramChartSource );
                detailsChartSources.put( sampleGroup.getName(), sources );
            }

			GraphGenerator graphGenerator = new GraphGenerator(summaryChartSources, detailsChartSources, getLog());
			ChartRenderer renderer = new ChartRendererImpl(getOutputDirectory());
			graphGenerator.generateGraphs(renderer, getBundle(locale), getConfig());

			// report
			ReportGenerator reportGenerator = new ReportGenerator(getBundle( locale ), getConfig(), graphGenerator);
			getLog().info(" generating report...");
			Sink sink = getSink();
			reportGenerator.doGenerateReport(sink, jmeterSamples);

		} catch (IOException e) {
			throw new MavenReportException("ReportGenerator failed", e);
		} catch (SAXException e) {
			throw new MavenReportException("ReportGenerator failed", e);
		} catch (ParserConfigurationException e) {
			throw new MavenReportException("ReportGenerator failed", e);
		}
	}
	
	protected abstract TestDataDirectory getTestDataDirectory();

	private ResourceBundle getBundle(Locale locale) {
		return Utils.getBundle(locale);
	}

	/**
	 * @return Returns the report.
	 */
    protected abstract ReportConfig getConfig();

    /**
	 * @see org.apache.maven.reporting.MavenReport#getOutputName()
	 */
	public String getOutputName() {
		return getConfig().getId();
	}

	protected abstract String getDataId();
}
