package com.imaginea.brightest.format;

import junit.framework.TestSuite;

/**
 * Same as selenium Formatters, they can read file formats and create a test suite from them
 * 
 * @author apurba
 */
public abstract class FormatHandler {
    public TestSuite loadSuite(String fileName) {
        if (understands(fileName)) {
            return loadSuiteInternal(fileName);
        }
        return null;
    }

    protected abstract boolean understands(String fileName);

    protected abstract TestSuite loadSuiteInternal(String fileName);
}
