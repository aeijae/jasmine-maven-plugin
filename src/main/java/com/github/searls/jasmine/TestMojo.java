package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;

import com.github.searls.jasmine.util.JasminePluginFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.searls.jasmine.format.JasmineResultLogger;
import com.github.searls.jasmine.model.JasmineResult;
import com.github.searls.jasmine.runner.ReporterType;
import com.github.searls.jasmine.runner.SpecRunnerExecutor;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator;


/**
 * @component
 * @goal test
 * @phase test
 */
public class TestMojo extends AbstractJasmineMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		if(!skipTests) {
			getLog().info("Executing Jasmine Tests");
            File specsDir = new File(jasmineTargetDir,specDirectoryName);
            try {
                getLog().info(JasmineResultLogger.HEADER);
                for (File specFile : JasminePluginFileUtils.filesForScriptsInDirectory(specsDir, specFilePostfix)) {
                    getLog().info("Generating HTML page for spec " + specFile.getName());
                    JasmineResult result;
                    try {
                        File runnerFile = writeSpecRunnerToOutputDirectory(specFile);
                        result = new SpecRunnerExecutor().execute(runnerFile.toURI().toURL(), new File(jasmineTargetDir,junitXmlReportFileName), browserVersion);
                    } catch (Exception e) {
                        throw new MojoExecutionException(e,"There was a problem executing Jasmine specs",e.getMessage());
                    }
                    logResults(result);
                    if(haltOnFailure && !result.didPass()) {
                        throw new MojoFailureException("There were Jasmine spec failures.");
                    }
                }
            } catch (IOException e) {
                throw new MojoFailureException("IO Exception: " + e.getMessage());
            } catch (MojoFailureException mfe) {
                throw mfe;
            } finally {
                logSummary();
            }
        } else {
			getLog().info("Skipping Jasmine Tests");
		}
	}

    private void logSummary() {
        JasmineResultLogger resultLogger = new JasmineResultLogger();
        resultLogger.setLog(getLog());
        resultLogger.logSummary();
    }

    private void logResults(JasmineResult result) {
		JasmineResultLogger resultLogger = new JasmineResultLogger();
		resultLogger.setLog(getLog());
		resultLogger.log(result);
	}

	private File writeSpecRunnerToOutputDirectory(File specFile) throws IOException {
		SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(new File(jasmineTargetDir,srcDirectoryName),new File(jasmineTargetDir,specDirectoryName),preloadSources, sourceEncoding);
		String html = htmlGenerator.generate(ReporterType.JsApiReporter, customRunnerTemplate, JasminePluginFileUtils.fileToString(specFile));
		getLog().debug("Writing out Spec Runner HTML " + html + " to directory " + jasmineTargetDir);
		File runnerFile = createRunnerFile(specFile);
		FileUtils.writeStringToFile(runnerFile, html);
		return runnerFile;
	}

}
