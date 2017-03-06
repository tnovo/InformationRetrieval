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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.index.AbstractDocumentInfo;
import pt.ua.ri.index.DocumentInfo;

import java.text.ParseException;

/**
 * @author mjrp1_000
 */
public class SimpleDocumentInfo extends AbstractDocumentInfo implements DocumentInfo {
    private static final Logger logger = LoggerFactory.getLogger(SimpleDocumentInfo.class);

    static SimpleDocumentInfo parse(final String str) {
        final String[] parts = str.split(SEPARATOR, 3);
        try {
            final int docId = Integer.parseInt(parts[0]);
            final float normalizedDocumentFrequency = DECIMAL_FORMAT.parse(parts[1]).floatValue();
            final int nTerms = Integer.parseInt(parts[2], 16);
            return new SimpleDocumentInfo(docId, nTerms, normalizedDocumentFrequency);
        } catch (ParseException e) {
            logger.error("Invalid format. {} does not match {}.", parts[1], DECIMAL_FORMAT, e);
            return null;
        }
    }

    private int termFrequency;

    SimpleDocumentInfo(final int docId) {
        super(docId);
        termFrequency = 1;
    }

    private SimpleDocumentInfo(final int docId, final int nTerms, final float normalizedTermFrequency) {
        super(docId, normalizedTermFrequency);
        Preconditions.checkArgument(nTerms > 0, "Must be positive");
        this.termFrequency = nTerms;
    }

    @Override public int getTermFrequency() {
        return termFrequency;
    }

    @Override public DocumentInfo merge(final DocumentInfo documentInfo) {
        Preconditions.checkArgument(documentInfo instanceof SimpleDocumentInfo, "Class must match");
        this.termFrequency += documentInfo.getTermFrequency();
        return this;
    }

    @Override public String toString() {
        return super.toString() + Integer.toString(termFrequency);
    }

}
