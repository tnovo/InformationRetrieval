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
package pt.ua.ri.index.proximity;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import pt.ua.ri.document.DocumentProperties;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.AbstractIndex;
import pt.ua.ri.index.DocumentInfo;
import pt.ua.ri.index.Index;
import pt.ua.ri.index.IndexTuple;
import pt.ua.ri.tokenizer.StreamTokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class ProximityIndex<S extends DocumentSchema<S>> extends AbstractIndex<S> implements Index<S> {

    private final static Pattern QUERY_PATTERN = Pattern.compile(
            "\"(?<query>[a-z A-Z]+)\"(?:~(?<dist>(?:[0-9]|[1-9][0-9]+)))?");

    public ProximityIndex(final Path dir,
            final StreamTokenizer st,
            final Converter<String, DocumentProperties<S>> converter) throws IOException {
        super(dir, st, converter);
    }

    @Override protected IndexTuple decodeIndexTuple(final String line) {
        return ProximityIndexTuple.decode(line);
    }

    @Override protected DocumentInfo newDocumentInfo(final int docId, final int position) {
        return new ProximityDocumentInfo(docId, position);
    }

    protected IndexTuple newIndexTuple(String term, final int docId, final DocumentInfo documentInfo) {
        Preconditions.checkNotNull(documentInfo);
        Preconditions.checkArgument(documentInfo instanceof ProximityDocumentInfo);
        return new ProximityIndexTuple(term, docId, (ProximityDocumentInfo) documentInfo);
    }

    @Override public Map<String, Optional<IndexTuple>> query(final String queryString) {
        final Matcher matcher = QUERY_PATTERN.matcher(queryString);
        while (matcher.find()) {
            final String group = matcher.group("query");
            final String dist = matcher.group("dist");

            final Map<String, Optional<IndexTuple>> results = super.query(group);

            // is a linked hash map, so it keeps insertion order (in this case, query order)
            final Iterator<Map.Entry<String, Optional<IndexTuple>>> entryIterator = results.entrySet().iterator();

            Map.Entry<String, Optional<IndexTuple>> first;
            Map.Entry<String, Optional<IndexTuple>> second;

            if (entryIterator.hasNext()) {
                first = entryIterator.next();
            }
            if (entryIterator.hasNext()) {
                second = entryIterator.next();
            }


        }


        return super.query(queryString);
    }

    @Override protected Map<String, Optional<IndexTuple>> searchMapImplementation() {
        return new LinkedHashMap<>();
    }

    private void positionalIntersect(final ProximityDocumentInfo p1, final ProximityDocumentInfo p2, int distance) {
        Preconditions.checkNotNull(p1);
        Preconditions.checkNotNull(p2);
        Preconditions.checkArgument(p1.getDocId() == p2.getDocId());

        // answer should return a result
        ProximityDocumentInfo answer = new ProximityDocumentInfo(p1.getDocId());

        final PrimitiveIterator.OfInt pp1 = p1.positionsStream().iterator();
        final PrimitiveIterator.OfInt pp2 = p2.positionsStream().iterator();
        /*
         * receive 2 index tuples + distance
          * check first document of each
          * if documents are equal then for each position pos1, get all positions pos2 that are within distance (and create a result), advance both
          * advance lowest
         */

        IntStream.Builder l = IntStream.builder();
        while (pp1.hasNext()) {
            int pos1 = pp1.nextInt();
            while (pp2.hasNext()) {
                int pos2 = pp2.nextInt();
                if (Math.abs(pos1 - pos2) <= distance) {
                    l.add(pos2);
                } else if (pos2 > pos1) {
                    break;
                }
            }
            IntStream positions = l.build().filter(value -> (value - pos1) <= distance);

            // for-each ps in position:
            // answer.add(docid, pos1, ps)
        }

    }

}
