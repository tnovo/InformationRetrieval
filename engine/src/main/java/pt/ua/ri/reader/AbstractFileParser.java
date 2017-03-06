package pt.ua.ri.reader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentSchema;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author tiago.novo
 */
public abstract class AbstractFileParser<S extends DocumentSchema<S>> implements FileParser<S> {
    private final static Logger logger = LoggerFactory.getLogger(AbstractFileParser.class);
    protected final Map<String, Serializable> properties;
    protected final PeekingIterator<String> lines;
    private final S schema;
    protected volatile Document<S> nextDocument;

    AbstractFileParser(final Path path, final S schema) throws IOException {
        Preconditions.checkNotNull(path, "Path cannot be null");
        Preconditions.checkNotNull(schema);
        this.properties = new LinkedHashMap<>(schema.size());
        this.lines = Iterators.peekingIterator(Files.lines(path, StandardCharsets.UTF_8).iterator());
        this.schema = schema;
    }

    protected abstract void fetchNextDocument();

    @Override public boolean hasNext() {
        return nextDocument != null;
    }

    @Override public Document<S> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Document<S> doc = nextDocument;
        logger.debug("Document:\"{}\" has {} lines.", doc, doc.stream().count());
        fetchNextDocument();
        return doc;
    }

    protected S getSchema() {
        return schema;
    }
}
