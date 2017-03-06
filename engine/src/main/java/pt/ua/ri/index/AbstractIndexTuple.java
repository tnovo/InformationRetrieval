package pt.ua.ri.index;

import com.google.common.collect.ImmutableList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractIndexTuple implements IndexTuple {

    static final String TOKEN_KEY_SEPARATOR = " ";
    private static final String DOCUMENT_INFO_SEPARATOR = " ";
    private static final String ID_SEPARATOR = ":";

    static boolean checkWord(final String line, final String word) {
        checkNotNull(line);
        checkNotNull(word);
        final int idxOf = line.indexOf(TOKEN_KEY_SEPARATOR);
        return idxOf > 0 && idxOf < line.length() && word.equalsIgnoreCase(line.substring(0, idxOf));
    }

    protected static AbstractIndexTuple decode(final String line,
            final Function<String, AbstractIndexTuple> sup,
            final Function<String, AbstractDocumentInfo> parser) {

        checkNotNull(sup, "Supplier must not be null");
        checkNotNull(line, "Line cannot be null");
        final int idxOf = line.indexOf(TOKEN_KEY_SEPARATOR);
        checkArgument(idxOf > 0, "Line is invalid. %s", line);
        final AbstractIndexTuple tuple = sup.apply(line.substring(0, idxOf));
        final List<String> strings = ImmutableList.copyOf(line.substring(idxOf).split(DOCUMENT_INFO_SEPARATOR));

        strings.parallelStream().forEach(str -> {
            final String[] parts = str.split(ID_SEPARATOR, 2);
            final int id = Integer.parseInt(parts[0], 16);
            final AbstractDocumentInfo di = parser.apply(parts[1]);
            tuple.add(id, di);
        });
        return tuple;
    }

    protected final TIntObjectMap<DocumentInfo> docs; //Document Frequency is docs.size()
    private final String term;

    protected AbstractIndexTuple(final String term) {
        checkNotNull(term);
        this.term = term;
        docs = new TIntObjectHashMap<>();
    }

    protected AbstractIndexTuple(final String term, final int docId, final DocumentInfo doc) {
        this(term);
        checkNotNull(doc, "Document Info cannot be null");
        checkArgument(typeCheck(doc), "Document Info is not of a valid type");
        docs.put(docId, doc);
    }

    protected abstract boolean typeCheck(DocumentInfo di);

    @Override public synchronized int getDocumentFrequency() {
        return docs.size();
    }

    @Override public synchronized Map<Integer, DocumentInfo> getDocumentsInfo() {
        final Map<Integer, DocumentInfo> map = new LinkedHashMap<>(docs.size());

        docs.forEachEntry((docId, docsInfo) -> {
            map.put(docId, docsInfo);
            return true;
        });
        return map;
    }

    @Override public synchronized double getNormalizedDocumentFrequency(final int id) {
        final Optional<DocumentInfo> documentInfo = Optional.ofNullable(docs.get(id));
        return documentInfo.map(DocumentInfo::getNormalizedDocumentFrequency).orElse(0.0);
    }

    @Override public String getTerm() {
        return term;
    }

    @Override public synchronized IndexTuple merge(final IndexTuple value) {
        checkArgument(this.getClass().isInstance(value), "IndexTuple not of the same type");


        ((AbstractIndexTuple) value).docs.forEachEntry((doc_id, docsInfo) -> {

            this.add(doc_id, docsInfo);
            return true;
        });
        return this;
    }

    public synchronized Iterable<Integer> getDocumentsID() {
        return IntStream.of(docs.keySet().toArray()).boxed().collect(Collectors.toList());
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder(getTerm()).append(TOKEN_KEY_SEPARATOR);
        docs.forEachEntry((doc_id, doc_info) -> {
            sb.append(Integer.toHexString(doc_id)).append(":").append(doc_info).append(' ');
            return true;
        });

        return sb.toString();
    }

    private void add(int docId, DocumentInfo di) {
        checkNotNull(di, "Document Info cannot be null");

        if (typeCheck(di)) {
            synchronized (this) {
                // checkArgument(docs.containsKey(docId), "Document Id already exists", docId, docs.get(docId));
                docs.put(docId, di);
            }
        }
    }
}
