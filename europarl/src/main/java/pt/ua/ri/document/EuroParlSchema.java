package pt.ua.ri.document;

import com.google.common.base.Converter;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import pt.ua.ri.reader.EuroParlFileParser;
import pt.ua.ri.reader.FileParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Maps.toMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static pt.ua.ri.document.DocumentProperties.DELIMITER;


/**
 * @author tiago.novo
 */
public class EuroParlSchema extends DocumentSchema<EuroParlSchema> {
    private static final Map<String, BiPredicate<DocumentProperties<EuroParlSchema>, String>> FIELD_CHECKER;
    private static final EuroParlSchema INSTANCE;
    private static final Converter<String, DocumentProperties<EuroParlSchema>> LINE_PARSER;

    static {
        final BiMap<String, Integer> FIELD_MAPPER = ImmutableBiMap.<String, Integer>builder().put("FILENAME", 0)
                .put("CHAPTER_ID", 1)
                .put("SPEAKER_ID", 2)
                .put("SPEAKER_NAME", 3)
                .put("DATE", 4)
                .put("LANGUAGE", 5)
                .build();

        FIELD_CHECKER = toMap(FIELD_MAPPER.keySet(), FieldChecker::new);

        INSTANCE = new EuroParlSchema(FIELD_MAPPER.keySet());

        LINE_PARSER = new Converter<String, DocumentProperties<EuroParlSchema>>() {
            @Override protected DocumentProperties<EuroParlSchema> doForward(final String line) {
                final List<String> split = copyOf(line.split(DELIMITER, FIELD_MAPPER.size()));
                return new DocumentProperties<>(INSTANCE, transformValues(FIELD_MAPPER,
                        value -> ofNullable(split.get(requireNonNull(value))).map(String::toLowerCase).orElse("")));
            }

            @Override protected String doBackward(final DocumentProperties<EuroParlSchema> properties) {
                return properties.propertiesToString(FIELD_MAPPER.entrySet()
                        .stream()
                        .sorted(comparingInt(Entry::getValue))
                        .map(Entry::getKey)
                        .collect(toImmutableList()));
            }
        };

    }

    public static EuroParlSchema instance() {
        return INSTANCE;
    }

    private EuroParlSchema(final Set<String> fields) {
        super(fields);
    }

    @Override public Map<String, BiPredicate<DocumentProperties<EuroParlSchema>, String>> fieldChecker() {
        return FIELD_CHECKER;
    }

    @Override public FileParser<EuroParlSchema> fileParser(final Path path) {
        try {
            return new EuroParlFileParser(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public Converter<String, DocumentProperties<EuroParlSchema>> lineParser() {
        return LINE_PARSER;
    }

}
