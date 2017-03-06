package pt.ua.ri.tokenizer;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

/**
 * @author tiago.novo
 */
public class StreamTokenizer {

    private final static Predicate<String> SIZE_CHECKER = s -> s.length() >= 3;
    private final static Pattern WORD_PATTERN = Pattern.compile("\\p{L}{3,}(?:(?:-(?:\\p{L}){2,}?)|'s)?");
    private final static Charset ascii = StandardCharsets.US_ASCII;
    private final static Charset utf8 = StandardCharsets.UTF_8;

    private static Stream<String> breakIntoWords(final CharSequence line) {
        Preconditions.checkNotNull(line);

        final StringTokenizer tokenizer = new StringTokenizer(new String(line.toString().getBytes(utf8), ascii));

        return StreamSupport.stream(Spliterators.spliterator(new Iterator<String>() {
            @Override public boolean hasNext() {
                return tokenizer.hasMoreTokens();
            }

            @Override public String next() {
                return tokenizer.nextToken();
            }
        }, tokenizer.countTokens(), Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED), false);
    }

    private final Map<String, Token> tokens;
    private Predicate<String> predicate;
    private Function<CharSequence, CharSequence> normalizer;

    public StreamTokenizer() {
        predicate = SIZE_CHECKER.and(WORD_PATTERN.asPredicate());
        normalizer = str -> str.codePoints()
                .filter(Character::isLetter)
                .map(Character::toLowerCase)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);

        tokens = new PatriciaTrie<>();
    }

    public void addFilter(Predicate<String> filterCondition) {
        predicate = predicate.and(filterCondition);
    }

    public void addNormalization(Function<CharSequence, CharSequence> transform) {
        normalizer = normalizer.andThen(transform);
    }

    public Stream<Token> tokenize(final Stream<String> lines) {
        Preconditions.checkNotNull(lines);
        final Stream<String> words = lines.flatMap(StreamTokenizer::breakIntoWords);
        final Stream<String> filteredWords = words.filter(predicate);
        final Stream<CharSequence> normalizedWords = filteredWords.map(normalizer);
        return normalizedWords.map(CharSequence::toString)
                .map((word) -> tokens.compute(word, (key, token) -> ofNullable(token).orElse(new Token(key))));
    }
}
