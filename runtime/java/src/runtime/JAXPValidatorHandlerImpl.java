package runtime;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.validation.ValidatorHandler;
import javax.xml.validation.TypeInfoProvider;

/**
 * @author Kohsuke Kawaguchi
 */
public class JAXPValidatorHandlerImpl extends ValidatorHandler {
    public JAXPValidatorHandlerImpl(BaliSchema schema) {
    }

    public void setContentHandler(ContentHandler contentHandler) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ContentHandler getContentHandler() {
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

    public void setResourceResolver(LSResourceResolver lsResourceResolver) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public LSResourceResolver getResourceResolver() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public TypeInfoProvider getTypeInfoProvider() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void endDocument() throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void startDocument() throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void skippedEntity(String name) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setDocumentLocator(Locator locator) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void processingInstruction(String target, String data) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
