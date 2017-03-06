package pt.ua.ri.tokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.function.Function;

/**
 * @author tiago.novo
 */
public class StemmingFunction implements Function<CharSequence, CharSequence> {
    private static final Logger logger = LoggerFactory.getLogger(StemmingFunction.class);
    private final SnowballStemmer stem = new englishStemmer();

    @Override public synchronized CharSequence apply(final CharSequence str) {
        try {
            stem.setCurrent(str.toString());
            stem.stem();
            return stem.getCurrent();
        } catch (Exception e) {
            logger.error("Error stemming: {}", str, e);
            return str;
        }
    }
}
