package runtime;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;

import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
final class JAXPValidatorImpl extends Validator {
    private final ValidatorHandler handler;

    public JAXPValidatorImpl(ValidatorHandler handler) {
        this.handler = handler;
    }

    public void reset() {
    }

    public void validate(Source source, Result result) throws SAXException, IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ErrorHandler getErrorHandler() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setResourceResolver(org.w3c.dom.ls.LSResourceResolver lsResourceResolver) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public org.w3c.dom.ls.LSResourceResolver getResourceResolver() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
