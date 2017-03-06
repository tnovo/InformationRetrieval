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
package pt.ua.ri.index.proximity;

import pt.ua.ri.index.AbstractIndexTuple;
import pt.ua.ri.index.DocumentInfo;
import pt.ua.ri.index.IndexTuple;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;

/**
 * @author mjrp1_000
 */
public class ProximityIndexTuple extends AbstractIndexTuple implements IndexTuple {

    static IndexTuple decode(final String line) {
        return decode(line, ProximityIndexTuple::new, ProximityDocumentInfo::parse);
    }

    ProximityIndexTuple(final String term, final int docId, final ProximityDocumentInfo documentInfo) {
        super(term, docId, documentInfo);
    }

    private ProximityIndexTuple(final String term) {
        super(term);
    }

    public Iterable<Integer> getDocumentPositions(final int doc_id) {
        final ProximityDocumentInfo di = (ProximityDocumentInfo) docs.get(doc_id);
        return (di != null) ? copyOf(di.boxed()) : of();
    }

    @Override protected boolean typeCheck(final DocumentInfo di) {
        return di != null && di instanceof ProximityDocumentInfo;
    }
}
