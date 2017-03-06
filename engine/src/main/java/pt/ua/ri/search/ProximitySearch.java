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

import com.google.common.util.concurrent.AtomicDouble;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import org.apache.commons.collections4.CollectionUtils;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.Index;
import pt.ua.ri.index.IndexTuple;
import pt.ua.ri.index.proximity.ProximityIndexTuple;
import pt.ua.ri.search.results.Result;
import pt.ua.ri.search.results.SimpleResult;
import pt.ua.ri.tokenizer.Tokenizer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

 
 */
public class ProximitySearch<S extends DocumentSchema<S>> extends SimpleSearch<S> {

    private final Pattern queryPattern = Pattern.compile("\"(?<query>[a-z A-Z]+)\"(~(?<dist>[0-9]+))?");

    ProximitySearch(final Index<S> idx) {
        super(idx);

    }

    ProximitySearch(Index<S> index, Tokenizer tok) {
        super(index, tok);
    }

    @Override public Iterable<Result> search(String query) {


        String nquery = null;
        int dist = 1;


        Matcher m = queryPattern.matcher(query);
        if (m.matches()) {
            nquery = m.group("query");
            try {
                dist = Integer.parseInt(m.group("dist"));
            } catch (NumberFormatException ex) {
                dist = 1;
            }
        }

        if (nquery == null) {
            return super.search(query);
        }

        List<Result> ret = new ArrayList<>();
        Map<String, IndexTuple> tokenInfos = new HashMap<>();
        TObjectFloatHashMap<String> palavrasNLize = new TObjectFloatHashMap<>();
        TIntFloatMap docsnLize = new TIntFloatHashMap();
        tok.setText(query);

        List<String> palavras = new ArrayList<>();
        while (tok.hasNext()) {
            palavras.add(tok.next().getString());
        }

        final AtomicDouble queryLength = new AtomicDouble(0.0);

        for (String palavra : palavras) {
            index.get(palavra).ifPresent(postingList -> {
                tokenInfos.put(palavra, postingList);
                int df = postingList.getDocumentFrequency();
                float idf = (float) (Math.log(index.numberOfDocuments()) - Math.log(df));
                queryLength.addAndGet(idf * idf);
                palavrasNLize.put(palavra, idf);
            });
        }

        queryLength.set(Math.sqrt(queryLength.doubleValue()));
        palavrasNLize.transformValues(new TransformationFunction(queryLength.floatValue()));
        Iterator<String> it = palavras.iterator();


        // for each two words
        if (it.hasNext()) {
            String p_actual;
            String p_next = it.next();
            ProximityIndexTuple t_ac;
            ProximityIndexTuple t_nx = (ProximityIndexTuple) tokenInfos.get(p_next);
            while (it.hasNext()) {
                p_actual = p_next;
                p_next = it.next();
                t_ac = t_nx;
                t_nx = (ProximityIndexTuple) tokenInfos.get(p_next);
                // get all documents from both words
                // see documents in common

                Collection<Integer> isect = CollectionUtils.intersection(t_ac.getDocumentsID(), t_nx.getDocumentsID());

                // for each document get positions of words

                for (int doc_id : isect) {
                    Iterable<Integer> lp_ac = t_ac.getDocumentPositions(doc_id);
                    Iterable<Integer> lp_nx = t_nx.getDocumentPositions(doc_id);

                    Iterator<Integer> it_ac = lp_ac.iterator();
                    Iterator<Integer> it_nx = lp_nx.iterator();

                    if (!it_ac.hasNext() || !it_nx.hasNext()) {
                        break;
                    }

                    int pos_ac = it_ac.next(), pos_nx = it_nx.next();
                    float score = docsnLize.containsKey(doc_id) ? docsnLize.get(doc_id) : 0;

                    score += comparePos(pos_ac, pos_nx, dist, doc_id, palavrasNLize, p_actual, p_next, t_ac, t_nx);

                    while (score <= 0.0f && (it_ac.hasNext() || it_nx.hasNext())) {
                        if (pos_ac < pos_nx) {
                            if (it_ac.hasNext()) {
                                pos_ac = it_ac.next();
                            } else {
                                pos_nx = it_nx.next();
                            }
                        } else {
                            if (it_nx.hasNext()) {
                                pos_nx = it_nx.next();
                            } else {
                                pos_ac = it_ac.next();
                            }
                        }

                        score += comparePos(pos_ac, pos_nx, dist, doc_id, palavrasNLize, p_actual, p_next, t_ac, t_nx);
                    }
                    if (score > 0.0f) {
                        docsnLize.put(doc_id, score);
                    }
                }
            }
        }

        docsnLize.forEachEntry((int doc_id, float score) -> ret.add(new SimpleResult(doc_id, score)));
        Collections.sort(ret);
        return ret;
    }

    private double comparePos(int pos1,
            int pos2,
            int dist,
            int doc_id,
            TObjectFloatHashMap<String> palavrasNLize,
            String palavra1,
            String palavra2,
            IndexTuple t1,
            IndexTuple t2) {
        if (Math.abs(pos1 - pos2) <= dist) {
            return (palavrasNLize.get(palavra1) * (t1.getNormalizedDocumentFrequency(doc_id))) +
                   (palavrasNLize.get(palavra2) * (t2.getNormalizedDocumentFrequency(doc_id)));

        } else {
            return 0.0f;
        }
    }
}
