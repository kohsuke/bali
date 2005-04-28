package runtime;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;

/**
 * Wraps a compiled {@link runtime.BaliSchema} object into the JARV
 * {@link BaliSchema} interface.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class JARVSchemaImpl implements Schema {
    /**
     * @param schema
     *      Schema object being wrapped.
     */
    public JARVSchemaImpl( BaliSchema schema ) {
        this.schema = schema;
    }

    private final BaliSchema schema;
    
    public Verifier newVerifier() throws VerifierConfigurationException {
        return new JARVVerifierImpl(schema.createValidatelet());
    }

}
