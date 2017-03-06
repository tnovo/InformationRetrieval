package pt.ua.ri.behaviour;

import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.search.FieldSearch;
import pt.ua.ri.search.Search;

import java.io.IOException;

/**
 * @author tiago.novo
 */
public class StreamSearchBehaviour<S extends DocumentSchema<S>> extends Behaviour<S> {
    private final Iterable<String> queries;
    private final Search<S> searcher;

    StreamSearchBehaviour(final Configuration<S> conf, Iterable<String> queries) throws IOException {
        super(conf);
        this.queries = queries;
        this.searcher = new FieldSearch<>(idx);
    }

    @Override public void action() {

    }
}
