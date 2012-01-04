package com.imaginea.brightest.driver;

import java.util.ArrayList;
import java.util.List;

import com.imaginea.brightest.ExecutionContext;
import com.imaginea.brightest.format.CSVFormatHandler;
import com.imaginea.brightest.format.FormatHandler;
import com.imaginea.brightest.format.UnknownFormatException;
import com.imaginea.brightest.format.XLSFormatHandler;
import com.imaginea.brightest.test.CommandBasedTest;
import com.thoughtworks.selenium.Selenium;

public class TestDriverManager {
	private final List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
	CommandBasedTest test;
	ExecutionContext context;
	public TestDriverManager(ExecutionContext context) {
		this.context=context;
		formatHandlers.add(new XLSFormatHandler());
		formatHandlers.add(new CSVFormatHandler());
	}

	public void loadTest(String filename) {

		for (FormatHandler formatHandler : formatHandlers) {
			this.test = formatHandler.loadDriverTestFile(filename);
			if (this.test != null) {
				break;
			}
		}
		if (this.test == null) {
			throw new UnknownFormatException(filename);
		}
	}
	
	public void runTest(Selenium selenium) {
		context.getExecutor().setExceutionContext(context);
		test.setCommandExecutor(context.getExecutor());
		test.runTest(selenium);
	}
}
