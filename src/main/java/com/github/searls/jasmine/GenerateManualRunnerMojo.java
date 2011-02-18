package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;

import com.github.searls.jasmine.util.JasminePluginFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.searls.jasmine.runner.ReporterType;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator;


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
			} catch (Exception e) {
				throw new MojoFailureException(e,"JavaScript Test execution failed.","Failed to generate "+manualSpecRunnerHtmlFileName);
			}
		} else {
			getLog().warn("Skipping manual spec runner generation. Check to make sure that both JavaScript directories `"+jsSrcDir.getAbsolutePath()+"` and `"+jsTestSrcDir.getAbsolutePath()+"` exist.");
		}
	}

	private void writeSpecRunnerToSourceSpecDirectory() throws IOException {
		SpecRunnerHtmlGenerator htmlGenerator = new SpecRunnerHtmlGenerator(jsSrcDir, jsTestSrcDir, preloadSources, sourceEncoding);
		for (File specFile : JasminePluginFileUtils.filesForScriptsInDirectory(jsTestSrcDir, specFilePostfix)) {
            getLog().info("Generate runner file for " + specFile.getName());
            String runner = htmlGenerator.generate(ReporterType.TrivialReporter, customRunnerTemplate, JasminePluginFileUtils.fileToString(specFile));

            File destination = createManualRunnerFile(specFile);
            String existingRunner = loadExistingManualRunner(destination);

            if(!StringUtils.equals(runner, existingRunner)) {
                FileUtils.writeStringToFile(destination, runner);
            } else {
                getLog().info("Skipping spec runner generation, because an identical spec runner already exists.");
            }
        }
	}

	private String loadExistingManualRunner(File destination) {
		String existingRunner = null;
		try {
			if(destination.exists()) {
				existingRunner = FileUtils.readFileToString(destination);
			}
		} catch(Exception e) {
			getLog().warn("An error occurred while trying to open an existing manual spec runner. Continuing");
		}
		return existingRunner;
	}

}
