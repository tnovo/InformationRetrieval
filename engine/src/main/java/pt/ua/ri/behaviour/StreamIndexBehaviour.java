package pt.ua.ri.behaviour;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.reader.CorpusAdapter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.Stream;

import static pt.ua.ri.utils.StaticUtils.estimatedUsedMemory;
import static pt.ua.ri.utils.StaticUtils.memoryLimit;

/**
 * @author tiago.novo
 */
public class StreamIndexBehaviour<S extends DocumentSchema<S>> extends Behaviour<S> {

    private static final Object MEMORY_ASYNC = new Object() {
        final DecimalFormat format = new DecimalFormat("###,###,###,###,##0");
        final DecimalFormat percent = new DecimalFormat("##0.00%");

        @Override public String toString() {
            final long mem = estimatedUsedMemory();
            return format.format(mem) + " (" + percent.format(((double) mem) / memoryLimit()) + ')';
        }
    };
    private final static Logger logger = LoggerFactory.getLogger(StreamIndexBehaviour.class);

    private final Stream<Document> documentStream;

    public StreamIndexBehaviour(Configuration<S> conf) throws IOException {
        super(conf);
        this.documentStream = CorpusAdapter.of(conf.getSchema()).streamCorpus(conf.getCorpusPath());
    }

    @Override public void action() {
        logger.info("Starting Indexing");
        final Stopwatch sw = Stopwatch.createStarted();

        documentStream.mapToInt(idx::index).forEach(docId -> {
            if (docId % 2500 == 0) {
                logger.info("MEM=[{}], TIME=[{}], DOCS={}", MEMORY_ASYNC, sw, docId);
            } else {
                logger.trace("MEM=[{}], TIME=[{}], DOCS={}", MEMORY_ASYNC, sw, docId);
            }
        });

        logger.info("Finishing TIME={}, DOCS={}, MEM={}", sw, idx.numberOfDocuments(), MEMORY_ASYNC);
        idx.finish();
        sw.stop();
        logger.info("Finished {}", sw);
    }


}
