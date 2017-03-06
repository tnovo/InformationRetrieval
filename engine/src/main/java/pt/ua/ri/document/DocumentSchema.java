package pt.ua.ri.document;

import com.google.common.base.Converter;
import pt.ua.ri.reader.FileParser;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.Objects.nonNull;

/**
 * @author tiago.novo
 */
public abstract class DocumentSchema<S extends DocumentSchema<S>> {

    private final Set<String> fields;

    protected DocumentSchema(final Set<String> fields) {this.fields = fields;}

    public abstract Map<String, BiPredicate<DocumentProperties<S>, String>> fieldChecker();

    public abstract FileParser<S> fileParser(final Path path);

    public abstract Converter<String, DocumentProperties<S>> lineParser();

    public Set<String> getFields() {
        return fields;
    }

    public int size() {
        return fields.size();
    }

    protected static class FieldChecker<S extends DocumentSchema<S>>
            implements BiPredicate<DocumentProperties<S>, String> {

        private final String field;

        FieldChecker(final String field) {this.field = field;}

        @Override public boolean test(final DocumentProperties<S> dp, final String value) {
            return nonNull(dp) && dp.get(field).filter(x -> Objects.equals(x, value)).isPresent();
        }
    }
}
