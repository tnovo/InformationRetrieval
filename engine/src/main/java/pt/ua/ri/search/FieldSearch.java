/*
 * Copyright (C) 2016 mjrp1_000
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
package pt.ua.ri.search;

import com.google.common.collect.ImmutableList;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.Index;
import pt.ua.ri.search.results.Result;
import pt.ua.ri.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mjrp1_000
 */
public class FieldSearch<S extends DocumentSchema<S>> extends ProximitySearch<S> {

    private final Pattern queryPattern = Pattern.compile(
            "((?:[a-zA-Z]+:[a-zA-Z0-9-]+)|(?:\"(?:[a-z A-Z]+)\"(?:~(?:[0-9]+))?)|(?:[a-zA-Z]+))");

    public FieldSearch(Index<S> index, Tokenizer tok) {
        super(index, tok);
    }

    public FieldSearch(final Index<S> idx) {
        super(idx);
    }

    @Override public Iterable<Result> search(String query) {
        List<Field> fields = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        List<Result> finalResults = new ArrayList<>();

        Matcher m = queryPattern.matcher(query);
        while (m.find()) {
            String group = m.group();

            if (group.contains(":")) {
                String[] split = group.split(":");
                fields.add(new Field(split[0], split[1]));
            } else {
                results.addAll(ImmutableList.copyOf(super.search(group)));
            }
        }

        boolean hasfield;
        for (Result r : results) {
            hasfield = true;
            for (Field f : fields) {
                if (!index.documentsHasField(r.getDocId(), f.field, f.value)) {
                    hasfield = false;
                    break;
                }
            }
            if (hasfield) {
                finalResults.add(r);
            }
        }

        return finalResults;
    }

    private class Field {
        final String field, value;

        Field(String field, String value) {
            this.field = field;
            this.value = value;
        }
    }
}
