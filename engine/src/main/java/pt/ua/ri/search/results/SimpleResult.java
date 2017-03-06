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
package pt.ua.ri.search.results;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * @author mjrp1_000
 */
public class SimpleResult implements Result {

    private final int docId;
    private final double docScore;

    public SimpleResult(int docId, double docScore) {
        Preconditions.checkArgument(docId >= 0, "Document Id must be positive");
        Preconditions.checkArgument(docScore >= 0 && docScore <= 1, "Score must be between 0 and 1");
        this.docId = docId;
        this.docScore = docScore;
    }

    @Override public int getDocId() {
        return docId;
    }

    @Override public double getScore() {
        return docScore;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("docId", docId).add("docScore", docScore).toString();
    }
}
