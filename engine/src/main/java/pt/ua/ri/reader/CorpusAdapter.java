package pt.ua.ri.reader;

import com.google.common.base.Preconditions;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentSchema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;
import static java.util.Spliterator.*;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;


/**
 * @author tiago.novo
 */
public class CorpusAdapter<S extends DocumentSchema<S>> {
    public static <S extends DocumentSchema<S>> CorpusAdapter<S> of(S s) {
        return new CorpusAdapter<>(s::fileParser);
    }

    private final Function<Path, FileParser<S>> parserCreator;

    private CorpusAdapter(final Function<Path, FileParser<S>> parserCreator) {this.parserCreator = parserCreator;}

    public Stream<Document> streamCorpus(Path p) throws IOException {
        Preconditions.checkNotNull(p, "Directory cannot be null");
        return walk(p, 1).filter(Files::isRegularFile).flatMap(this::streamFile);
    }

    private FileParser<S> createFileParser(final Path path) {
        return parserCreator.apply(path);
    }

    private Stream<Document<S>> streamFile(Path path) {
        return stream(spliteratorUnknownSize(createFileParser(path), DISTINCT | NONNULL | ORDERED), false);
    }
}
