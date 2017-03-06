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
package pt.ua.ri.index;

import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentSchema;

import java.util.Map;
import java.util.Optional;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public interface Index<S extends DocumentSchema<S>> {

    String DOCUMENT_FILENAME = Configuration.DOCS_FILE;
    String SUB_INDEX_FILENAME = "sub_results.txt";
    String VERSION_DIR_PREFIX = "version";

    boolean documentsHasField(int doc_id, String field, String value);

    void finish();

    Optional<IndexTuple> get(String token);

    String getDocumentName(int docId);

    int index(Document<S> document);

    int numberOfDocuments();

    Map<String, Optional<IndexTuple>> query(String query);
}
