package com.github.searls.jasmine.runner;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.codehaus.plexus.util.StringUtils;

import com.github.searls.jasmine.io.FileUtilsWrapper;
import com.github.searls.jasmine.io.IOUtilsWrapper;
import com.github.searls.jasmine.util.JasminePluginFileUtils;

public class SpecRunnerHtmlGenerator {

    public static final String DEFAULT_RUNNER_HTML_TEMPLATE_FILE = "/jasmine-templates/SpecRunner.htmltemplate";
    public static final String DEFAULT_SOURCE_ENCODING = "UTF-8";

    private static final String SOURCE_ENCODING = "sourceEncoding";
    private static final String CSS_DEPENDENCIES_TEMPLATE_ATTR_NAME = "cssDependencies";
    private static final String JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME = "javascriptDependencies";
    private static final String SOURCES_TEMPLATE_ATTR_NAME = "sources";
    private static final String REPORTER_ATTR_NAME = "reporter";
    private static final String PATH_VARIABLES = "pathVariables";

    //TODO - simplify this by finding all resources by folder instead
    public static final String JASMINE_JS = "/vendor/js/jasmine.js";
    public static final String JASMINE_HTML_JS = "/vendor/js/jasmine-html.js";
    public static final String CONSOLE_X_JS = "/vendor/js/consolex.js";
    public static final String JSON_2_JS = "/vendor/js/json2.js";
    public static final String LOAD_SCRIPT = "/vendor/js/test-helper.js";
    public static final String JASMINE_CSS = "/vendor/css/jasmine.css";

    public static final String JASMINE_PLUGIN_JS_NAMESPACE = "jasmine.plugin";
    public static final String ROOT_DIR_JS_VARIABLE = JASMINE_PLUGIN_JS_NAMESPACE + ".rootDir";
    public static final String SPEC_DIR_JS_VARIABLE = JASMINE_PLUGIN_JS_NAMESPACE + ".jsTestDir";
    public static final String SRC_DIR_JS_VARIABLE = JASMINE_PLUGIN_JS_NAMESPACE + ".jsSrcDir";


    private final FileUtilsWrapper fileUtilsWrapper = new FileUtilsWrapper();
    private final IOUtilsWrapper ioUtilsWrapper = new IOUtilsWrapper();

    private final File sourceDir;
    private final File jasmineTargetDir;
    private final File specDir;

    private final List<String> sourcesToLoadFirst;
    private List<String> fileNamesAlreadyWrittenAsScriptTags = null;
    private final String sourceEncoding;

	public SpecRunnerHtmlGenerator(final File sourceDir, final File specDir,
	        final File jasmineTargetDir, final List<String> sourcesToLoadFirst,
	        final String sourceEncoding) {
		this.jasmineTargetDir = jasmineTargetDir;
		this.sourcesToLoadFirst = sourcesToLoadFirst;
        this.sourceDir = sourceDir;
        this.sourceEncoding = sourceEncoding;
        this.specDir = specDir;
    }

    public String generate(final ReporterType reporterType, final File customRunnerTemplate, final String specName) {
        try {
            fileNamesAlreadyWrittenAsScriptTags = new ArrayList<String>();
            final String htmlTemplate = resolveHtmlTemplate(customRunnerTemplate);
            final StringTemplate template = new StringTemplate(htmlTemplate, DefaultTemplateLexer.class);

            includeJavaScriptDependencies(asList(JASMINE_JS, JASMINE_HTML_JS, CONSOLE_X_JS, JSON_2_JS, LOAD_SCRIPT), template);
            includeCssDependencies(asList(JASMINE_CSS), template);
            setJavaScriptSourcesAttribute(template, specName);
            template.setAttribute(PATH_VARIABLES, pathVariables());
            template.setAttribute(REPORTER_ATTR_NAME, reporterType.name());
            template.setAttribute(SOURCE_ENCODING, StringUtils.isNotBlank(sourceEncoding) ? sourceEncoding : DEFAULT_SOURCE_ENCODING);

            return template.toString();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load file names for dependencies or scripts", e);
        }
    }

    private String pathVariables() {
        return String.format(
                "<script type='text/javascript'>\n" +
	                "%s = %s || {};\n" +
	                "%s = '%s';\n" +
	                "%s = '%s';\n" +
	                "%s = '%s';\n" +
                "</script>",
                JASMINE_PLUGIN_JS_NAMESPACE, JASMINE_PLUGIN_JS_NAMESPACE,
                ROOT_DIR_JS_VARIABLE, JasminePluginFileUtils.fileToString(jasmineTargetDir),
                SPEC_DIR_JS_VARIABLE, JasminePluginFileUtils.fileToString(specDir),
                SRC_DIR_JS_VARIABLE, JasminePluginFileUtils.fileToString(sourceDir));

    }

    private String resolveHtmlTemplate(final File customRunnerTemplate) throws IOException {
        return customRunnerTemplate != null ? fileUtilsWrapper.readFileToString(customRunnerTemplate) : ioUtilsWrapper.toString(DEFAULT_RUNNER_HTML_TEMPLATE_FILE);
    }

    private void includeJavaScriptDependencies(final List<String> dependencies, final StringTemplate template) throws IOException {
        final StringBuilder js = new StringBuilder();
        for (final String jsFile : dependencies) {
            js.append("<script type=\"text/javascript\">").append(ioUtilsWrapper.toString(jsFile)).append("</script>");
        }
        template.setAttribute(JAVASCRIPT_DEPENDENCIES_TEMPLATE_ATTR_NAME, js.toString());
    }

    private void includeCssDependencies(final List<String> dependencies, final StringTemplate template) throws IOException {
        final StringBuilder css = new StringBuilder();
        for (final String cssFile : dependencies) {
            css.append("<style type=\"text/css\">").append(ioUtilsWrapper.toString(cssFile)).append("</style>");
        }
        template.setAttribute(CSS_DEPENDENCIES_TEMPLATE_ATTR_NAME, css.toString());
    }

    private void setJavaScriptSourcesAttribute(final StringTemplate template, final String specName) throws IOException {
        final StringBuilder scriptTags = new StringBuilder();
        appendScriptTagsForFiles(scriptTags, expandSourcesToLoadFirstRelativeToSourceDir());
        appendScriptTagsForFiles(scriptTags, asList(specName));
        template.setAttribute(SOURCES_TEMPLATE_ATTR_NAME, scriptTags.toString());
    }



    private List<String> expandSourcesToLoadFirstRelativeToSourceDir() {
        final List<String> files = new ArrayList<String>();
        if (sourcesToLoadFirst != null) {
            for (final String sourceToLoadFirst : sourcesToLoadFirst) {
                final File file = new File(sourceDir, sourceToLoadFirst);
                final File specFile = new File(specDir, sourceToLoadFirst);
                if (file.exists()) {
                    files.add(JasminePluginFileUtils.fileToString(file));
                } else if (specFile.exists()) {
                    files.add(JasminePluginFileUtils.fileToString(specFile));
                } else {
                    files.add(sourceToLoadFirst);
                }
            }
        }
        return files;
    }


    private void appendScriptTagsForFiles(final StringBuilder sb, final List<String> sourceFiles) {
        for (final String sourceFile : sourceFiles) {
            if (!fileNamesAlreadyWrittenAsScriptTags.contains(sourceFile)) {
                sb.append("<script type=\"text/javascript\" src=\"").append(sourceFile).append("\"></script>");
                fileNamesAlreadyWrittenAsScriptTags.add(sourceFile);
            }
        }
    }
}
