package runtime;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Validator implemented as SAX ContentHandler.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class Validatelet implements ContentHandler {
    
    private final BaliSchema schema;
    
    private State currentState = State.emptySet;
    
    /** Cached value of <code>currentState.getTextSensitivity()</code>. */
    private int textSensitivity;
    
    private StateFactory factory = new StateFactory();
    
    /** Stores unprocessed text. */
    private StringBuffer textBuffer = new StringBuffer();
    /** True if the text in the text buffer is ignorable. */
    private boolean isTextBufferIgnorable = true;

    /**
     * Set to true once we see an error.
     */
    private boolean hadError =false;

    /**
     * Once an error is detected, we set this value to >0,
     * and count down for each successful successive matches.
     * We will start reporting errors again once this counter hits 0.
     *
     * The net effect is that we won't report a new error unless
     * we know that we are back in sync with the document.
     */
    private int errorRecoveryCountDown = 0;

//
// attributes
//
    // array is used in the reverse order. attribute[depth] always
    // point to the top-most active attributes.
    private AttributesSet[] attributes;
    private int depth;
    
    private Locator locator;
    
    
    
    public Validatelet( BaliSchema schema ) {
        this.schema = schema;
        
        // prepare empty objects
        attributes = new AttributesSet[16];
        for( int i=0; i<attributes.length; i++ )
            attributes[i] = new AttributesSet();
            
        // initialize nameCode map
        for (Iterator itr = schema.nameLiterals.entrySet().iterator(); itr.hasNext();) {
            Map.Entry e = (Map.Entry) itr.next();
            
            String[] key = (String[])e.getKey();
            Integer value = (Integer)e.getValue();
            
            nameCodes.put(key[0], key[1], value.intValue());
        }

        defaultNameCode = new NameCodeMap.Entry(
            0,BaliSchema.WILDCARD,BaliSchema.WILDCARD,
            schema.defaultNameCode,
            null );
    }
    
    
    
    
    /**
     * Sets the current state to the given state.
     * 
     * @param newState
     *      The caller needs to addRef.
     */
    private final void setCurrentState( State newState ) {
        currentState = newState;
        textSensitivity = newState.textSensitivity;
    }

    /**
     * Returns true if this validator has reported an error on this document so far.
     */
    public boolean hadError() {
        return hadError;
    }

    public void startDocument() throws SAXException {
        setCurrentState(schema.initialState);
        context = initialContext;
        depth=attributes.length-1;
        hadError = false;
    }
    
    

    
    private final AttributesSet pushAttributes( Attributes atts ) {
        if( depth==0 ) {
            int len = attributes.length;
            AttributesSet[] newBuf = new AttributesSet[len*2];
            System.arraycopy(attributes,0,newBuf,len,len);
            for( int i=len-1; i>=0; i-- )
                newBuf[i] = new AttributesSet();
            attributes = newBuf;
            depth = len;
        }
        
        depth--;
        
        AttributesSet r = attributes[depth];
        r.reset(this,atts,context);
        
        return r;
    }

//
//
// validation context
//
//
    /**
     * Immutable ValidationContext implementation.
     * Because we don't process attribute transitions when we see a start element,
     * ValidationContext implementation needs to be immutable.
     */
    private final static class Context implements ValidationContext {
        Context( Context previous, String prefix, String uri ) {
            this.previous = previous;
            this.prefix = prefix;
            this.uri = uri;
        }
        private final Context previous;
        private final String prefix,uri;
        public String resolveNamespacePrefix(String prefix) {
            for( Context c = this; c!=null; c=c.previous ) {
                if( prefix.equals(c.prefix) )    return c.uri;
            }
            return null;
        }
        public String getBaseUri() { return null; }
        public boolean isNotation(String name) { return true; }
        public boolean isUnparsedEntity(String name) { return true; }
    }
    /** The initial context at the beginning of the validation. */
    private static final Context initialContext = new Context(null,"xml","http://www.w3.org/XML/1998/namespace");
    
    private Context context=null;

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        context = new Context( context, prefix, uri );
    }
    public void endPrefixMapping(String prefix) throws SAXException {
        context = context.previous;
    }


//
//
// SAX events handling
//    
//  
    public void characters(char[] buf, int start, int len) throws SAXException {
        int ts = textSensitivity;
        
        if( ts == State.TEXT_IGNORABLE )
            return; // no need to accumulate text
            
        if( ts == State.TEXT_SENSITIVE )
            textBuffer.append(buf,start,len);
            
        // make sure that the added text is still ignorable
        if(isTextBufferIgnorable) {
            for( int i=len-1; i>=0; i-- ) {
                char ch = buf[i+start];
                if( ch==' ' || ch=='\t' || ch=='\r' || ch=='\n' )
                    continue;
                else {
                    isTextBufferIgnorable = false;
                    return;
                }
            }
        }
    }
    
    public void ignorableWhitespace(char[] buf, int start, int len) throws SAXException {
        // we know that the string consists entirely from whitespaces.
        if( textSensitivity==State.TEXT_SENSITIVE )
            textBuffer.append(buf,start,len);
    }
    
    private void resetTextBuffer() {
//        if(textBuffer.length()<1024)    textBuffer.setLength(0);
//        else                            textBuffer = new StringBuffer();
        textBuffer.setLength(0);
        isTextBufferIgnorable = true;
    }
    
    private void processText() throws SAXException {
        switch(textSensitivity) {
        case State.TEXT_WHITESPACE_ONLY:
            if( !isTextBufferIgnorable ) {
                setCurrentState(error(false));
                isTextBufferIgnorable = true;
            }
            // fall through next block
            
        case State.TEXT_IGNORABLE:
            if( textBuffer.length()!=0 || !isTextBufferIgnorable)
                // no text should have been accumulated.
                throw new InternalError("assertion failure");
            
            // no need to resetTextBuffer() as the buffer is empty.
            return;
            
        case State.TEXT_SENSITIVE:
            String value = textBuffer.toString();
    
//            if(debug!=null) {
//                String trimmed = value.trim();
//                printIndent();
//                debug.println(MessageFormat.format("text \"{0}\"",new Object[]{trimmed}));
//            }
            
            // handle ignorable texts
            State newState = currentState.text(
                value, isTextBufferIgnorable,
                context, attributes[depth], State.emptySet, factory );
            
//            if(debug!=null) {
//                if( value.trim().length()!=0) {
//                    printIndent();
//                    debug.println(MessageFormat.format("{0} => {1}",new Object[]{currentState,newState}));
//                }
//            }
            
            if(newState==State.emptySet)
                newState = error(false);
            setCurrentState(newState);
            resetTextBuffer();
            break;
            
        default:
            throw new InternalError("assertion failure");
        }
        
    }

//    private HashMap startTagCache = new HashMap();

    private final NameCodeMap nameCodes = new NameCodeMap();
    
    private final NameCodeMap.Entry defaultNameCode;

    public void startElement(
        String nsUri, String localName, String qname, Attributes atts) throws SAXException {
        
        if( !isTextBufferIgnorable )
            processText();
        else
            resetTextBuffer();
        
        // look up name code table
        NameCodeMap.Entry nameCode = getNameCode(nsUri,localName);
        
        if(debug!=null) {
            printIndent();
            debug.println(MessageFormat.format("<{0}> (name code:{1})",
                new Object[]{qname,Integer.toString(nameCode.nameCode)}));
            indent++;
        }
        
        AttributesSet curAtts = pushAttributes(atts);
        
        State newState;
        
        if( atts.getLength()==0 ) {
//            HashMap m = (HashMap)startTagCache.get(nameCode);
//            if(m==null)     startTagCache.put(nameCode,m=new HashMap());
//            
            newState = (State)nameCode.startTagCache.get(currentState);
            if(newState==null) {
                newState = currentState.startElement(
                    nameCode.nameCode, curAtts, State.emptySet, factory );
                nameCode.startTagCache.put( currentState, newState );
            }
        } else {
            newState = currentState.startElement(
                nameCode.nameCode, curAtts, State.emptySet, factory );
        }
        
        if(debug!=null) {
            printIndent();
            debug.println(MessageFormat.format("{0} => {1}",new Object[]{currentState,newState}));
        }

        errorCountDown();
        if(newState==State.emptySet) {
            newState = error(false);
            newState = factory.makeAfter(State.emptySet,newState);
        }
        setCurrentState(newState);
    }

    private void errorCountDown() {
        if(errorRecoveryCountDown>0)
            errorRecoveryCountDown--;
    }

    public void endElement(String nsUri, String localName, String qname) throws SAXException {
        processText();

        if(debug!=null) {
            indent--;
            printIndent();
            debug.println(MessageFormat.format("</{0}>",new Object[]{qname}));
        }

        depth++;    // pop attribute
        AttributesSet atts = attributes[depth];
        
        State newState;
        if( atts.size()==0 ) {
            // optimization
            newState = currentState.endElementFast(factory);
        } else {
            // normal
            newState = currentState.endElement( atts, State.emptySet, factory, false);
        }
                
        if(debug!=null) {
            printIndent();
            debug.println(MessageFormat.format("{0} => {1}",new Object[]{currentState,newState}));
        }
        errorCountDown();
        if(newState==State.emptySet)
            newState = error(true);
        setCurrentState(newState);
    }




    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    public Locator getLocator() { return locator; }


    public void endDocument() throws SAXException {
        setCurrentState(State.emptySet);
        locator = null;
    }



    private ErrorHandler errorHandler;
    public void setErrorHandler( ErrorHandler newHandler ) {
        this.errorHandler = newHandler;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Reports an error and recovers.
     *
     * @param endElement
     *      if true, we recover from a premature end element.
     */
    private State error(boolean endElement) throws SAXException {
        Set tokens = new TreeSet();

        State state = currentState.recoverFromError(currentState,factory,tokens,nameCodes);
        if(endElement)
            // recovery from end element is different from just ignoring the token,
            // so we need to create a different state
            state = currentState.endElement(attributes[depth],State.emptySet,factory,true);

        if(errorRecoveryCountDown==0) {
            StringBuffer buf = new StringBuffer("validation error. Expecting: ");
            boolean first = true;
            for (Iterator itr = tokens.iterator(); itr.hasNext();) {
                Object o = itr.next();
                if(!first) {
                    buf.append(',');
                }
                first = false;
                buf.append(o);
            }

            SAXParseException e = new SAXParseException(buf.toString(),locator);
            if(errorHandler!=null)
                errorHandler.error(e);
        }

        errorRecoveryCountDown = 5;
        return state;
    }

    protected final NameCodeMap.Entry getNameCode( String uri, String localName ) {
        
        NameCodeMap.Entry e;
        
        e = nameCodes.get(uri,localName);
        if(e!=null)     return e;
        
        e = nameCodes.get(uri,BaliSchema.WILDCARD);
        if(e!=null)     return e;
        
        return defaultNameCode;
    }

    // unhandled call back
    public void processingInstruction(String target, String data) throws SAXException {}
    public void skippedEntity(String name) throws SAXException {}


//
//
// debug support
//
//

    /** debug messages will be sent to this object if set to non-null. */
    private static final PrintStream debug;
    private int indent=0;
    
    private void printIndent() {
        for( int i=0; i<indent; i++ )
            debug.print("  ");
    }

    static {
        PrintStream s = null;
        try {
            s = System.getProperty("DEBUG_BALI")!=null?System.out:null;
        } catch (Exception e) {
            ; // ignore
        }
        debug = s;
    }
}
