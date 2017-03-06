package pt.ua.ri.index;

import com.google.common.base.Converter;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentProperties;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.tokenizer.StreamTokenizer;
import pt.ua.ri.tokenizer.Token;
import pt.ua.ri.utils.Division;
import pt.ua.ri.utils.Divisions;
import pt.ua.ri.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;
import static java.lang.Character.isLetter;
import static java.lang.Integer.parseInt;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.nio.file.Files.*;
import static java.util.Spliterator.*;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;
import static java.util.stream.StreamSupport.stream;
import static pt.ua.ri.index.AbstractIndexTuple.TOKEN_KEY_SEPARATOR;
import static pt.ua.ri.index.AbstractIndexTuple.checkWord;
import static pt.ua.ri.utils.Divisions.countExistingDivisions;
import static pt.ua.ri.utils.FileUtils.copyDirectory;
import static pt.ua.ri.utils.FileUtils.deleteDirectory;
import static pt.ua.ri.utils.StaticUtils.*;

/**
 * Created by tiago.novo on 14-01-2017.
 */
@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess"})
public abstract class AbstractIndex<S extends DocumentSchema<S>> implements Index<S> {

    private static final String DOCUMENT_SEPARATOR = ",";
    private final static Logger logger = LoggerFactory.getLogger(AbstractIndex.class);

    private static <S extends DocumentSchema<S>> TIntObjectMap<DocumentProperties<S>> loadDocuments(Path indexDir,
            Converter<String, DocumentProperties<S>> converter) {
        TIntObjectMap<DocumentProperties<S>> docs = null;
        final Path path = indexDir.resolve(DOCUMENT_FILENAME);
        if (exists(path) && isRegularFile(path)) {
            try (Stream<String> lines = lines(path)) {
                final Map<Integer, DocumentProperties<S>> map = lines.map(line -> line.split(DOCUMENT_SEPARATOR, 2))
                        .collect(toMap(split -> parseInt(split[0], 16), split -> converter.convert(split[1])));
                docs = new TIntObjectHashMap<>(map.size());
                docs.putAll(map);
            } catch (IOException ignored) {
                logger.warn("Error loading documents to memory.", ignored);
            }

        }
        return docs != null ? docs : new TIntObjectHashMap<>();
    }

    protected final Path indexDirectory;
    protected final TIntObjectMap<DocumentProperties<S>> documents;
    protected final Divisions divisions;
    private final Map<String, IndexTuple> tokenIndex;
    private final StreamTokenizer st;
    private final Converter<String, DocumentProperties<S>> propertiesParser;
    protected boolean needsMerging;
    protected int nVersion = 0;

    protected AbstractIndex(Path dir,
            final StreamTokenizer st,
            final Converter<String, DocumentProperties<S>> propertiesParser) throws IOException {
        checkNotNull(dir);
        createDirectories(dir);
        this.indexDirectory = dir;
        this.st = Objects.requireNonNull(st);
        this.tokenIndex = new PatriciaTrie<>();
        this.documents = loadDocuments(indexDirectory, propertiesParser);
        this.needsMerging = false;
        this.divisions = new Divisions();
        divisions.splitToHave(1 + countExistingDivisions(indexDirectory));
        this.propertiesParser = propertiesParser;


    }

    protected abstract IndexTuple decodeIndexTuple(final String line);

    protected abstract DocumentInfo newDocumentInfo(final int docId, final int position);

    protected abstract IndexTuple newIndexTuple(final String term, final int docId, final DocumentInfo documentInfo);

    public void clear() {
        tokenIndex.clear();
    }

    @Override public boolean documentsHasField(int doc_id, String field, String value) {
        return false;
    }

    @Override public void finish() {
        logger.info("Finishing index.");
        liberateMemory();
        divisions.splitToHave(2 * (nVersion + 1));
        mergeSubIndexes();
    }

    @Override public Optional<IndexTuple> get(String token) {

        if (!needsMerging) {
            try {
                return retrieveToken(divisions.getDivisionFor(token), token);
            } catch (IOException e) {
                logger.warn("Error retrieving token {}", token, e);
            }
        }

        return Optional.empty();
    }

    @Override public String getDocumentName(int docId) {
        return documents.get(docId).toString();

    }

    @Override public int index(final Document<S> document) {
        checkNotNull(document, "Document cannot be null");

        if (memoryLimitReached()) {
            logger.info("Memory Limit reached: {}", estimatedUsedMemory());
            liberateMemory();
        }
        final int currentDocId = documents.size() + 1;
        final AtomicInteger position = new AtomicInteger(0);
        final PatriciaTrie<DocumentInfo> currentTokens = st.tokenize(document.stream())
                .collect(toMap(Token::getString, token -> newDocumentInfo(currentDocId, position.getAndIncrement()),
                        DocumentInfo::merge, PatriciaTrie::new));

        final float weight = (float) sqrt(
                currentTokens.values().stream().mapToLong(DocumentInfo::getTermFrequency).map(tf -> tf * tf).sum());

        currentTokens.values().parallelStream().forEach(docsInfo -> docsInfo.setDocumentLength(weight));

        currentTokens.forEach(
                (s, docsInfo) -> tokenIndex.merge(s, newIndexTuple(s, currentDocId, docsInfo), IndexTuple::merge));
        documents.put(currentDocId, document.getDocumentProperties());

        return currentDocId;

    }

    @Override public int numberOfDocuments() {
        return documents.size();
    }

    @Override public Map<String, Optional<IndexTuple>> query(final String query) {
        try (Stream<Token> tokenStream = optimizeSearchStream(st.tokenize(of(query)))) {
            return needsMerging ?
                   tokenStream.collect(toMap(Token::getString, token -> Optional.empty())) :
                   tokenStream.collect(toMap(Token::getString, token -> get(token.getString()),
                           (indexTuple, indexTuple2) -> indexTuple, this::searchMapImplementation));
        }
    }

    protected Stream<Token> optimizeSearchStream(final Stream<Token> tokenize) {return tokenize;}

    protected Map<String, Optional<IndexTuple>> searchMapImplementation() {
        return new HashMap<>();
    }

    private void liberateMemory() {
        logger.info("Cleaning memory.");
        final Path versionDirectory = indexDirectory.resolve(VERSION_DIR_PREFIX + nVersion);
        writeIndexTo(versionDirectory);
        clear();
        nVersion++;
        while (!hasFreeMemory()) {
            forceGarbageCollection();
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void mergeSubIndexes() {
        logger.info("Merging Sub-Indexes.");
        final Path temporaryDirectory = indexDirectory.resolve("temp");
        final Path documentsFile = temporaryDirectory.resolve(DOCUMENT_FILENAME);
        logger.info("TEMP_DIR=[{}]", temporaryDirectory);
        logger.info("DOC_FILE=[{}]", documentsFile);

        try {
            createDirectories(temporaryDirectory);
            write(documentsFile, (Iterable<String>) DocumentsIterator::new);
        } catch (IOException ex) {
            logger.warn("Error writing docs.", ex);
            throw new UncheckedIOException(ex);
        }


        logger.info("Merging sub-indexes to large index on {}", temporaryDirectory);
        try {
            final List<BufferedReader> readers = walk(indexDirectory).filter(Files::isRegularFile)
                    .filter(path -> Objects.equals(SUB_INDEX_FILENAME, path.getFileName().toString()))
                    .map(FileUtils::newBufferedReader)
                    .filter(Objects::nonNull)
                    .collect(toList());

            readers.stream()
                    .flatMap(br -> stream(spliteratorUnknownSize(br.lines().iterator(), ORDERED | NONNULL | SORTED),
                            false))
                    .sorted()
                    .forEachOrdered(new IndexLineMerger(temporaryDirectory));

            logger.info("Deleting Sub-indexes");
            walk(indexDirectory).filter(Files::isDirectory)
                    .filter(path -> !Objects.equals(indexDirectory, path))
                    .filter(path -> !Objects.equals(temporaryDirectory, path))
                    .forEach(FileUtils::deleteDirectory);
            logger.info("Moving to final directory {}", indexDirectory);
            copyDirectory(temporaryDirectory, indexDirectory);
            deleteDirectory(temporaryDirectory);
            needsMerging = false;
        } catch (IOException ignored) {
            logger.warn("Error on mergeSubIndexes.", ignored);
        }
    }

    private Optional<IndexTuple> retrieveToken(final Division divisionToCheck, final String token) throws IOException {
        checkNotNull(token);
        checkNotNull(divisionToCheck);
        checkArgument(divisionToCheck.inRange(token), "Must be in range");

        try (final BufferedReader br = newBufferedReader(indexDirectory.resolve(divisionToCheck.getFilename()))) {
            final Optional<String> find = br.lines().filter(line -> checkWord(line, token)).findFirst();
            return find.map(this::decodeIndexTuple);
        }
    }

    private void writeIndexTo(Path dir) {
        checkNotNull(dir);
        final Path docs = dir.resolve(DOCUMENT_FILENAME);
        final Path subIndexFile = dir.resolve(SUB_INDEX_FILENAME);

        try (final Stream<String> tokenLines = tokenIndex.values().stream().map(Object::toString).sorted()) {
            logger.info("Writing {}", subIndexFile);
            createDirectories(dir);
            write(docs, (Iterable<String>) DocumentsIterator::new);
            write(subIndexFile, (Iterable<String>) tokenLines::iterator);
            logger.info("Written {} token lines", tokenIndex.size());
        } catch (IOException ignored) {
            logger.error("Error Writing files.", ignored);
        }

    }

    private class IndexLineMerger implements Consumer<String> {

        private final Iterator<Division> iterator;
        private final Path indexDirectory;
        private Division currentDivision;
        private String currentTokenString;
        private BufferedWriter writer;

        IndexLineMerger(final Path indexDirectory) throws IOException {
            checkNotNull(divisions);
            checkNotNull(indexDirectory);
            this.iterator = divisions.iterator();
            this.indexDirectory = indexDirectory;
            createDirectories(indexDirectory);
            advance();
        }

        @Override public synchronized void accept(String line) {
            checkNotNull(line, "Line cannot be null");
            checkNotNull(currentDivision, "Current Division is null. Something bad happened");
            if (!isLetter(line.codePointAt(0))) {
                logger.error("Line does not start with a letter: \"{}\"", line);
                return;
            }


            final int idxOf = line.indexOf(TOKEN_KEY_SEPARATOR);
            if (idxOf < 0) {
                logger.warn("Line does not contain separator '{}'. {}", TOKEN_KEY_SEPARATOR, line);
                return;
            }


            final String tokenString = line.substring(0, idxOf);
            final String postingLists = line.substring(idxOf + 1);


            try {

                if (!tokenString.equals(currentTokenString)) {
                    writer.newLine();
                    while (!currentDivision.inRange(tokenString)) {
                        if (!advance()) {
                            logger.error("No more divisions to advance, exiting. {} {}", currentDivision, line);
                            return;
                        }
                        logger.info("Advanced to division {} because of token {}", currentDivision, tokenString);
                    }
                    currentTokenString = tokenString;
                    writer.append(tokenString).append(TOKEN_KEY_SEPARATOR);
                }
                checkState(tokenString.compareTo(currentTokenString) <= 0, "Strings are not in order", tokenString,
                        currentTokenString);
                writer.append(postingLists).append(TOKEN_KEY_SEPARATOR);
            } catch (IOException e) {
                logger.warn("Error in writing {}.", line, e);
            }
        }

        private synchronized boolean advance() {
            if (iterator.hasNext()) {
                this.currentDivision = iterator.next();
                final Path path = indexDirectory.resolve(currentDivision.getFilename());
                try {
                    this.writer = newBufferedWriter(path);
                    logger.debug("Created Writer for file {}", path);
                    return true;
                } catch (IOException ex) {
                    logger.error("Error opening writer to {}", path, ex);
                    throw new UncheckedIOException(ex);
                }
            }
            logger.debug("Could not advance to next Division. Last one is {}", currentDivision);
            return false;
        }
    }

    private class DocumentsIterator implements Iterator<String> {
        private final TIntObjectIterator<DocumentProperties<S>> it;
        private final Converter<DocumentProperties<S>, String> toString;

        public DocumentsIterator() {
            checkNotNull(documents);
            this.it = documents.iterator();
            this.toString = propertiesParser.reverse();
        }

        @Override public boolean hasNext() {
            return it.hasNext();
        }

        @Override public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            it.advance();
            return format("%X%s%s", it.key(), DOCUMENT_SEPARATOR, toString.convert(it.value()));
        }
    }
}
