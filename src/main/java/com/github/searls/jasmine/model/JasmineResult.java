package com.github.searls.jasmine.model;


import java.awt.event.TextEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JasmineResult {
	private String description;
	private String details;

    private static int totalTests = 0;
    private static int totalFailures = 0;

    private final Pattern TEST_COUNT_EXTRACTOR = Pattern.compile("(\\d+)\\sspecs,\\s(\\d+)\\sfailures");

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
        Matcher matcher = TEST_COUNT_EXTRACTOR.matcher(description);
        if (matcher.matches()) {
            try {
                totalTests += Integer.parseInt(matcher.group(1));
                totalFailures += Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {}
        }
    }

	public boolean didPass() {
		if(description == null) {
			throw new IllegalStateException("Can only determine success after description is set.");
		}
		return description.contains("0 failures");
	}
	
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

    public static int getTotalTests() {
        return totalTests;
    }

    public static int getTotalFailures() {
        return totalFailures;
    }
}
