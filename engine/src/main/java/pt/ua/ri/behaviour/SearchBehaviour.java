/*
 * Copyright (C) 2016 Tiago Novo <tmnovo at ua.pt>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pt.ua.ri.behaviour;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.search.FieldSearch;
import pt.ua.ri.search.Search;
import pt.ua.ri.search.results.Result;

import java.io.IOException;

public class SearchBehaviour<S extends DocumentSchema<S>> extends Behaviour<S> {

    private final Search<S> s;
    private final Iterable<String> queries;
    private final Logger logger = LoggerFactory.getLogger(SearchBehaviour.class);

    public SearchBehaviour(Configuration<S> conf, Iterable<String> queries) throws IOException {
        super(conf);
        this.queries = queries;
        this.s = new FieldSearch<>(idx, tok);
    }

    @Override public void action() {
        for (String query : queries) {
            Stopwatch sw = Stopwatch.createStarted();

            Iterable<Result> results = s.search(query);
            sw.stop();
            logger.info("Results for: {} ( {} )", query, sw);
            int i = 0;
            logger.info("%8s | %-30s | %10s\n", "#", "Score", "Document");
            for (Result r : results) {
                if (i >= 20) {
                    break;
                }
                logger.info("%8d | %-30s | %9.5f%%\n", ++i, idx.getDocumentName(r.getDocId()), r.getScore() * 100.0f);
            }
        }
    }

}
