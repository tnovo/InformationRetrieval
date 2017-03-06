/* 
 * Copyright (C) 2015 Tiago
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
package pt.ua.ri.document;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.copyOf;

/**
 * @author Tiago
 */
public class Document<S extends DocumentSchema<S>> implements Comparable<Document<S>> {

    private final List<String> lines;

    private final DocumentProperties<S> documentProperties;

   /* public Document(final Map<String, Serializable> properties, final List<String> lines) {
        this(new DocumentProperties(properties), lines);
    }*/

    public Document(final DocumentProperties<S> doc, final List<String> lines) {
        this.documentProperties = doc;
        this.lines = copyOf(lines);
    }

    @Override public int compareTo(final Document<S> t) {
        return (this == t) ? 0 : documentProperties.compareTo(t.getDocumentProperties());
    }

    public DocumentProperties<S> getDocumentProperties() {
        return documentProperties;
    }

    public Stream<String> stream() { return lines.stream(); }

    @Override public String toString() {
        return documentProperties.toString();
    }
}
