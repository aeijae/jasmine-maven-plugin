package com.github.searls.jasmine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;

import com.github.searls.jasmine.io.IOUtilsWrapper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class SpecRunnerIndexGenerator {

	private final File runnerIndexFile;
	private final File jasmineTargetDir;

	public SpecRunnerIndexGenerator(final File runnerIndexFile,
	        final File jasmineTargetDir) {
		this.runnerIndexFile = runnerIndexFile;
		this.jasmineTargetDir = jasmineTargetDir;
	}

	private static final IOUtilsWrapper ioUtilsWrapper = new IOUtilsWrapper();

	public void generate(final List<URI> paths) throws IOException {
		final String htmlTemplate = getHtmlTemplate();
		final StringTemplate template = new StringTemplate(htmlTemplate,
		        DefaultTemplateLexer.class);

		final URI root = jasmineTargetDir.toURI();

		final Collection<RunnerModel> runners = Collections2.transform(paths,
		        new Function<URI, RunnerModel>() {

			        public RunnerModel apply(final URI uri) {
				        final String src = uri.toString();
				        final String name = root.relativize(uri).toString();

			        	return new RunnerModel(src, name);
			        }

		        });

		template.setAttribute("runners", runners);

		final OutputStream out = new FileOutputStream(runnerIndexFile);

		IOUtils.write(template.toString(), out);
		IOUtils.closeQuietly(out);
	}

	private String getHtmlTemplate() throws IOException {
		return ioUtilsWrapper.toString("/jasmine-templates/index.htmltemplate");
	}

	static final class RunnerModel {

		private final String src;
		private final String name;

		RunnerModel(final String src, final String name) {
			this.src = src;
			this.name = name;
        }

		public String getSrc() {
	        return src;
        }

		public String getName() {
	        return name;
        }

	}

}
