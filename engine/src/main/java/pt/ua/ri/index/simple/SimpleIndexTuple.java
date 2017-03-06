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
package pt.ua.ri.index.simple;

import pt.ua.ri.index.AbstractIndexTuple;
import pt.ua.ri.index.DocumentInfo;
import pt.ua.ri.index.IndexTuple;

/**
 * @author mjrp1_000
 */
class SimpleIndexTuple extends AbstractIndexTuple implements IndexTuple {

    static IndexTuple decode(final String line) {
        return decode(line, SimpleIndexTuple::new, SimpleDocumentInfo::parse);
    }

    SimpleIndexTuple(String term, final int docId, final SimpleDocumentInfo documentInfo) {
        super(term, docId, documentInfo);
    }

    private SimpleIndexTuple(String term) {
        super(term);
    }

    @Override protected boolean typeCheck(final DocumentInfo di) {
        return di != null && di instanceof SimpleDocumentInfo;
    }
}
