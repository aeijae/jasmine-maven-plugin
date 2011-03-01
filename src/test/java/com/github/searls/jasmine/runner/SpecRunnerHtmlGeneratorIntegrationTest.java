package com.github.searls.jasmine.runner;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.searls.jasmine.io.FileUtilsWrapper;
import com.github.searls.jasmine.io.IOUtilsWrapper;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URI;

import static com.github.searls.jasmine.Matchers.containsScriptTagWith;
import static com.github.searls.jasmine.Matchers.containsStyleTagWith;
import static com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class SpecRunnerHtmlGeneratorIntegrationTest {

    private static final String HTML5_DOCTYPE = "<!DOCTYPE html>";
    private static final String SOURCE_ENCODING = "as9du20asd xanadu";
	static {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	}

	private SpecRunnerHtmlGenerator specRunnerHtmlGenerator;

	@Mock private FileUtilsWrapper fileUtilsWrapper;
    @Mock private File mockDir;
	@Spy private IOUtilsWrapper ioUtilsWrapper = new IOUtilsWrapper();

    @Before
    public void setUp() throws Exception {
        when(mockDir.toURI()).thenReturn(new URI("file://host/path"));
        specRunnerHtmlGenerator = new SpecRunnerHtmlGenerator(mockDir, mockDir, null, SOURCE_ENCODING);
    }

	@Test
	public void shouldBuildBasicHtmlWhenNoDependenciesAreProvided() {
		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsString("<html>"));
		assertThat(html, containsString("</html>"));
	}

	@Test
	public void shouldPutInADocTypeWhenNoDependenciesAreProvided() {
		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsString(HTML5_DOCTYPE));
		assertThat(getPage(html).getDoctype().getName(), is("html"));
	}

	@Test
	public void shouldAssignSpecifiedSourceEncoding() {
		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null,"");

		HtmlMeta contentType = getPage(html).getFirstByXPath("//meta");
		assertThat(contentType.getContentAttribute(), is("text/html; charset=" + SOURCE_ENCODING));
	}

	@Test
	public void shouldDefaultSourceEncodingWhenUnspecified() {
		specRunnerHtmlGenerator = new SpecRunnerHtmlGenerator(mockDir, mockDir, null, "");

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		HtmlMeta contentType = getPage(html).getFirstByXPath("//meta");
		assertThat(contentType.getContentAttribute(), is("text/html; charset=" + SpecRunnerHtmlGenerator.DEFAULT_SOURCE_ENCODING));
	}

	@Test
	public void populatesJasmineSource() throws Exception {
		String expected = "javascript()";
		when(ioUtilsWrapper.toString(JASMINE_JS)).thenReturn(expected);

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsScriptTagWith(expected));
	}

	@Test
	public void populatesJasmineHtmlSource() throws Exception {
		String expected = "javascript()";
		when(ioUtilsWrapper.toString(JASMINE_HTML_JS)).thenReturn(expected);

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsScriptTagWith(expected));
	}
	@Test
	public void populatesConsoleXSource() throws Exception {
		String expected = "javascript()";
		when(ioUtilsWrapper.toString(CONSOLE_X_JS)).thenReturn(expected);

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsScriptTagWith(expected));
	}

	@Test
	public void populatesJson2Source() throws Exception {
		String expected = "javascript()";
		when(ioUtilsWrapper.toString(JSON_2_JS)).thenReturn(expected);

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsScriptTagWith(expected));
	}

	@Test
	public void shouldPopulateCSSIntoHtmlWhenProvided() throws Exception {
		String expected = "h1 { background-color: awesome}";
		when(ioUtilsWrapper.toString(JASMINE_CSS)).thenReturn(expected);

		String html = specRunnerHtmlGenerator.generate(ReporterType.TrivialReporter, null, "");

		assertThat(html, containsStyleTagWith(expected));
	}

	private HtmlPage getPage(String html) {
		MockWebConnection webConnection = new MockWebConnection();
		webConnection.setDefaultResponse(html);
		WebClient webClient = new WebClient();
		webClient.setWebConnection(webConnection);
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setIncorrectnessListener(new IncorrectnessListener() {
			public void notify(String arg0, Object arg1) {
			}
		});
		try {
			return webClient.getPage("http://blah");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
