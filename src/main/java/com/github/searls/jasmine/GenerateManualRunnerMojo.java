package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.searls.jasmine.runner.ReporterType;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator;
import com.github.searls.jasmine.util.JasminePluginFileUtils;


/**
 * @component
 * @goal generateManualRunner
 * @phase generate-test-sources
 */
public class GenerateManualRunnerMojo extends AbstractJasmineMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
		if(jsSrcDir.exists() && jsTestSrcDir.exists()) {
			getLog().info("Generating runner files in the Jasmine plugin's target directory to open in a browser to facilitate faster feedback.");
			try {
				writeSpecRunnerToSourceSpecDirectory();
			} catch (final Exception e) {
				getLog().error(e.getMessage(), e);
				throw new MojoFailureException(e,
				        "JavaScript Test execution failed.",
				        "Failed to generate " + manualSpecRunnerHtmlFileName);
			}
		} else {
			getLog().warn("Skipping manual spec runner generation. Check to make sure that both JavaScript directories `"+jsSrcDir.getAbsolutePath()+"` and `"+jsTestSrcDir.getAbsolutePath()+"` exist.");
		}
	}

	private void writeSpecRunnerToSourceSpecDirectory() throws IOException {
		final SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(
		        jsSrcDir, jsTestSrcDir, jasmineTargetDir, preloadSources,
		        sourceEncoding);

		final List<URI> paths = new LinkedList<URI>();

		for (final File specFile : JasminePluginFileUtils.filesForScriptsInDirectory(jsTestSrcDir, specFilePostfix)) {
			final String relPath = jsTestSrcDir.toURI()
			        .relativize(specFile.toURI()).toString();

            getLog().info("Generate runner file for " + relPath);

            final String runner = htmlGenerator.generate(ReporterType.TrivialReporter, customRunnerTemplate, JasminePluginFileUtils.fileToString(specFile));
            final File destination = createManualRunnerFile(specFile);

            paths.add(destination.toURI());

            final String existingRunner = loadExistingManualRunner(destination);

            if(!StringUtils.equals(runner, existingRunner)) {
                FileUtils.writeStringToFile(destination, runner);
            } else {
                getLog().info("Skipping spec runner generation, because an identical spec runner already exists.");
            }
		}

		generateRunnerIndex(paths);
	}

	private void generateRunnerIndex(final List<URI> paths) throws IOException {
	    final File indexFile = new File(jasmineTargetDir, runnerIndexFileName);
	    getLog().info("Generating runner index at " + indexFile);
		new SpecRunnerIndexGenerator(indexFile, jasmineTargetDir).generate(paths);
    }

	private String loadExistingManualRunner(final File destination) {
		String existingRunner = null;
		try {
			if(destination.exists()) {
				existingRunner = FileUtils.readFileToString(destination);
			}
		} catch(final Exception e) {
			getLog().warn("An error occurred while trying to open an existing manual spec runner. Continuing");
		}
		return existingRunner;
	}

}
