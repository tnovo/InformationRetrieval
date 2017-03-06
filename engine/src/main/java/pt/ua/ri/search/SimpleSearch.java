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
package pt.ua.ri.search;

import com.google.common.collect.ImmutableList;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.Index;
import pt.ua.ri.index.IndexTuple;
import pt.ua.ri.search.results.Result;
import pt.ua.ri.search.results.SimpleResult;
import pt.ua.ri.tokenizer.Tokenizer;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.google.common.collect.Maps.transformValues;

/*

 
 */
public class SimpleSearch<S extends DocumentSchema<S>> implements Search<S> {

    protected final Index<S> index;
    final Tokenizer tok;

    SimpleSearch(final Index<S> idx) {
        this(idx, null);
    }

    SimpleSearch(Index<S> index, Tokenizer tok) {
        this.index = index;
        this.tok = tok;
    }

    @Override public Iterable<Result> search(String query) {

        // get an optional tuple for each query term
        final Map<String, Optional<IndexTuple>> tuples = index.query(query);
        final double logDocumentFrequency = Math.log(index.numberOfDocuments());

        Map<String, Double> idf = transformValues(tuples, v -> v != null ?
                                                               logDocumentFrequency - Math.log(
                                                                       v.map(IndexTuple::getDocumentFrequency)
                                                                               .orElse(0)) :
                                                               0);


        final double queryLength = Math.sqrt(idf.values().parallelStream().mapToDouble(v -> v * v).sum());
        final double factor = 1.0 / queryLength;
        idf.replaceAll((word, value) -> value * factor);

        final TIntDoubleMap scoreMap = new TIntDoubleHashMap();


        tuples.forEach((word, tuple) -> tuple.map(IndexTuple::getDocumentsInfo)
                .ifPresent(idx -> idx.forEach((docId, documentInfo) -> {
                    final double deltaScore = idf.get(word) * documentInfo.getNormalizedDocumentFrequency();
                    scoreMap.adjustOrPutValue(docId, deltaScore, deltaScore);
                })));

        return ImmutableList.sortedCopyOf(() -> new Iterator<Result>() {
            private final TIntDoubleIterator iterator = scoreMap.iterator();

            @Override public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override public Result next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                iterator.advance();
                return new SimpleResult(iterator.key(), iterator.value());
            }
        });
    }
}
