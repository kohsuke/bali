package org.kohsuke.bali;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * @author Kohsuke Kawaguchi
 */
final class ErrorReporter implements ErrorHandler {
    public void error(SAXParseException exception) {
        print(exception);
    }

    public void fatalError(SAXParseException exception) {
        print(exception);
    }

    public void warning(SAXParseException exception) {
        print(exception);
    }

    private void print(SAXParseException exception) {
        System.out.println(exception.getMessage());
        System.out.println("  at line "+exception.getLineNumber()+" of "+exception.getSystemId());

    }
}
