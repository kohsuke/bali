package runtime;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

/**
 * {@link Schema} implementation that wraps a {@link BaliSchema}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JAXPSchemaImpl extends Schema {
    /**
     * @param schema
     *      Schema object being wrapped.
     */
    public JAXPSchemaImpl( BaliSchema schema ) {
        this.schema = schema;
    }

    private final BaliSchema schema;

    public Validator newValidator() {
        return new JAXPValidatorImpl(newValidatorHandler());
    }

    public ValidatorHandler newValidatorHandler() {
        return new JAXPValidatorHandlerImpl(schema);
    }
}
