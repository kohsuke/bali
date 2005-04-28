package runtime;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierHandler;
import org.iso_relax.verifier.impl.VerifierImpl;
import org.xml.sax.ErrorHandler;

/**
 * Wraps a {@link Validatelet} object into a JARV {@link Verifier} interface.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class JARVVerifierImpl extends VerifierImpl {
    
    /**
     * @param validatelet
     *      The validatelet object to be wrapped. This validatelet
     *      will be "owned" by this JARVVerifierImpl, so the caller
     *      shouldn't attempt to use directly once it's wrapped.
     */
    public JARVVerifierImpl( Validatelet validatelet ) throws VerifierConfigurationException {
        this.validatelet = validatelet;
        this.handler = new JARVVerifierHandlerImpl(validatelet);
    }
    
    
    private final Validatelet validatelet;
    
    private final VerifierHandler handler;

    public VerifierHandler getVerifierHandler() {
        return handler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        super.setErrorHandler(handler);
        validatelet.setErrorHandler(handler);
    }

}
