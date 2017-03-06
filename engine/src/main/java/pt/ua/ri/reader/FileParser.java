package pt.ua.ri.reader;

import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentSchema;

import java.util.Iterator;

/**
 * @author tiago.novo
 */
public interface FileParser<S extends DocumentSchema<S>> extends Iterator<Document<S>> {
    @Override boolean hasNext();

    @Override Document<S> next();
}
