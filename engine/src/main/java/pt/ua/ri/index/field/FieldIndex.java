/* 
 * Copyright (C) 2016 Tiago Novo <tmnovo at ua.pt>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.ri.index.field;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentProperties;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.Index;
import pt.ua.ri.index.IndexTuple;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class FieldIndex<S extends DocumentSchema<S>> implements Index<S> {

    private final static BiPredicate<? extends DocumentProperties, String> ALWAYS_FALSE = (properties, defaultValue) -> false;


    private final Map<String, BiPredicate<DocumentProperties<S>, String>> fieldCheckMap;
    private final TIntObjectMap<DocumentProperties<S>> documentProperties;
    private final Index<S> decorated;


    public FieldIndex(final Index<S> decorated,
            final Map<String, BiPredicate<DocumentProperties<S>, String>> field_check_map) {
        checkNotNull(decorated);
        this.documentProperties = new TIntObjectHashMap<>();
        this.decorated = decorated;
        fieldCheckMap = field_check_map;
    }

    public FieldIndex(final Index<S> decorated) {
        this(decorated, ImmutableMap.of());
    }

    @SuppressWarnings("unchecked") @Override
    public boolean documentsHasField(final int doc_id, final String field, final String value) {
        checkNotNull(field, "Field must not be null");
        checkNotNull(value, "Value must not be null");
        final DocumentProperties<S> di = documentProperties.get(doc_id);

        return fieldCheckMap.getOrDefault(field, (BiPredicate<DocumentProperties<S>, String>) ALWAYS_FALSE)
                .test(di, value);

    }

    @Override public void finish() {
        decorated.finish();
    }

    @Override public Optional<IndexTuple> get(final String tokens) {
        return decorated.get(tokens);
    }

    @Override public String getDocumentName(final int docId) {
        return decorated.getDocumentName(docId);
    }

    @SuppressWarnings("unchecked") @Override public int index(final Document<S> document) {
        final int docID = decorated.index(document);
        documentProperties.put(docID, document.getDocumentProperties());
        return docID;

    }

    @Override public int numberOfDocuments() {
        return decorated.numberOfDocuments();
    }

    @Override public Map<String, Optional<IndexTuple>> query(final String query) {
        return decorated.query(query);
    }


}
