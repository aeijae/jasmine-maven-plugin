package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.searls.jasmine.format.JasmineResultLogger;
import com.github.searls.jasmine.model.JasmineResult;
import com.github.searls.jasmine.runner.ReporterType;
import com.github.searls.jasmine.runner.SpecRunnerExecutor;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator;
import com.github.searls.jasmine.util.JasminePluginFileUtils;


/**
 * @component
 * @goal test
 * @phase test
 */
public class TestMojo extends AbstractJasmineMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		if(!skipTests) {
			getLog().info("Executing Jasmine Tests");
            try {
                getLog().info(JasmineResultLogger.HEADER);
                for (final File specFile : JasminePluginFileUtils.filesForScriptsInDirectory(jsTestSrcDir, specFilePostfix)) {
					final String relPath = jsTestSrcDir.toURI()
					        .relativize(specFile.toURI()).toString();

                    getLog().info("Generate runner file for " + relPath);

                    JasmineResult result;
                    try {
                        final File runnerFile = writeSpecRunnerToOutputDirectory(specFile);
                        result = new SpecRunnerExecutor().execute(runnerFile.toURI().toURL(), new File(jasmineTargetDir,junitXmlReportFileName), browserVersion);
                    } catch (final Exception e) {
                    	getLog().error(e.getMessage(), e);
                        throw new MojoExecutionException(e,"There was a problem executing Jasmine specs",e.getMessage());
                    }
                    logResults(result);
                    if(haltOnFailure && !result.didPass()) {
                        throw new MojoFailureException("There were Jasmine spec failures.");
                    }
                }
            } catch (final IOException e) {
                throw new MojoFailureException("IO Exception: " + e.getMessage());
            } catch (final MojoFailureException mfe) {
                throw mfe;
            } finally {
                logSummary();
            }
        } else {
			getLog().info("Skipping Jasmine Tests");
		}
	}

    private void logSummary() {
        final JasmineResultLogger resultLogger = new JasmineResultLogger();
        resultLogger.setLog(getLog());
        resultLogger.logSummary();
    }

    private void logResults(final JasmineResult result) {
		final JasmineResultLogger resultLogger = new JasmineResultLogger();
		resultLogger.setLog(getLog());
		resultLogger.log(result);
	}

	private File writeSpecRunnerToOutputDirectory(final File specFile) throws IOException {
		final SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(
				new File(jasmineTargetDir, srcDirectoryName),
				new File(jasmineTargetDir, specDirectoryName),
				jasmineTargetDir, preloadSources, sourceEncoding);

		final String html = htmlGenerator.generate(ReporterType.JsApiReporter, customRunnerTemplate, JasminePluginFileUtils.fileToString(specFile));
		getLog().debug("Writing out Spec Runner HTML " + html + " to directory " + jasmineTargetDir);
		final File runnerFile = createRunnerFile(specFile);
		FileUtils.writeStringToFile(runnerFile, html);
		return runnerFile;
	}

}
