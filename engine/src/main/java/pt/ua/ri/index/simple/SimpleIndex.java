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
package pt.ua.ri.index.simple;

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

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class SimpleIndex<S extends DocumentSchema<S>> extends AbstractIndex<S> implements Index<S> {

    public SimpleIndex(final Path dir,
            final StreamTokenizer st,
            final Converter<String, DocumentProperties<S>> converter) throws IOException {
        super(dir, st, converter);
    }

    @Override protected IndexTuple decodeIndexTuple(final String line) {
        return SimpleIndexTuple.decode(line);
    }

    @Override protected DocumentInfo newDocumentInfo(final int docId, final int position) {
        return new SimpleDocumentInfo(docId);
    }

    @Override protected IndexTuple newIndexTuple(final String term, final int docId, final DocumentInfo documentInfo) {
        Preconditions.checkNotNull(documentInfo);
        Preconditions.checkArgument(documentInfo instanceof SimpleDocumentInfo);
        return new SimpleIndexTuple(term, docId, (SimpleDocumentInfo) documentInfo);
    }
}
