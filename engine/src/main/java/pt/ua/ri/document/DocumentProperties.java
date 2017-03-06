package pt.ua.ri.document;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.text.Collator.getInstance;
import static java.util.stream.Collectors.joining;


public class DocumentProperties<S extends DocumentSchema<S>> implements Comparable<DocumentProperties<S>> {
    protected static final String DELIMITER = ",";

    private final Map<String, Serializable> map;
    private final S schema;

    public DocumentProperties(S schema, Map<String, Serializable> properties) {
        Preconditions.checkNotNull(properties, "Must not be null");
        Preconditions.checkArgument(!properties.isEmpty(), "Must not be empty");
        this.schema = schema;
        this.map = properties;
    }


    @Override public int compareTo(final DocumentProperties<S> o) {
        Preconditions.checkNotNull(o);
        Preconditions.checkArgument(this.getClass().isInstance(o));
        MapDifference<String, Serializable> difference = Maps.difference(map, o.map);
        return difference.entriesDiffering()
                .values()
                .stream()
                .mapToInt(vd -> getInstance().compare(vd.leftValue(), vd.rightValue()))
                .findFirst()
                .orElse(0);
    }

    public String toString() {
        return propertiesToString(schema.getFields());
    }

    protected Optional<Serializable> get(String key) {
        return Optional.ofNullable(map.get(key));
    }

    String propertiesToString(final Collection<String> fields) {
        return fieldStream(fields).collect(joining(DELIMITER));
    }

    private Stream<String> fieldStream(final Collection<String> keys) {
        return keys.stream().map((key) -> get(key).map(Object::toString).orElse(""));
    }

}
