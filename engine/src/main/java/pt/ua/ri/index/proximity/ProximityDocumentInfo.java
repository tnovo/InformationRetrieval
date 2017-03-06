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

import com.google.common.base.Preconditions;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.index.AbstractDocumentInfo;
import pt.ua.ri.index.DocumentInfo;

import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * @author mjrp1_000
 */
public class ProximityDocumentInfo extends AbstractDocumentInfo implements DocumentInfo {
    private static final String POSITIONS_DELIMITER = ",";
    private static final String POSITIONS_PREFIX = "[";
    private static final String POSITIONS_SUFFIX = "]";
    private static final Logger logger = LoggerFactory.getLogger(ProximityDocumentInfo.class);

    static ProximityDocumentInfo parse(final String str) {
        final String[] parts = str.split(SEPARATOR, 3);
        try {
            final int docId = Integer.parseInt(parts[0]);
            final float normalizedDocumentFrequency = DECIMAL_FORMAT.parse(parts[1]).floatValue();
            final TIntList positions = TIntArrayList.wrap(Arrays.stream(
                    parts[2].substring(parts[2].indexOf(POSITIONS_PREFIX), parts[2].indexOf(POSITIONS_SUFFIX))
                            .split(POSITIONS_DELIMITER)).mapToInt((p) -> Integer.parseInt(p, 16)).toArray());
            return new ProximityDocumentInfo(docId, normalizedDocumentFrequency, positions);
        } catch (ParseException e) {
            logger.error("Invalid format. {}", str, e);
            return null;
        }
    }

    private final TIntList positions;

    ProximityDocumentInfo(final int docId, final int position) {
        this(docId);
        positions.add(position);
    }

    ProximityDocumentInfo(final int docId) {
        super(docId);
        this.positions = new TIntLinkedList();
    }

    private ProximityDocumentInfo(final int docId, final float normalizedDocumentFrequency, final TIntList positions) {
        super(docId, normalizedDocumentFrequency);
        this.positions = new TIntLinkedList(positions);
    }

    @Override public int getTermFrequency() {
        return positions.size();
    }

    @Override public DocumentInfo merge(final DocumentInfo documentInfo) {
        Preconditions.checkArgument(documentInfo instanceof ProximityDocumentInfo, "Class must match");
        this.positions.addAll(((ProximityDocumentInfo) documentInfo).getPositions());
        return this;
    }

    @Override public String toString() {
        return positionsStream().mapToObj(Integer::toHexString)
                .collect(
                        Collectors.joining(POSITIONS_DELIMITER, super.toString() + POSITIONS_PREFIX, POSITIONS_SUFFIX));
    }

    Iterable<Integer> boxed() {
        return positionsStream().boxed().collect(toImmutableList());
    }

    IntStream positionsStream() {
        final TIntIterator iterator = positions.iterator();
        return IntStream.generate(iterator::next).limit(positions.size());
    }

    private TIntList getPositions() {
        return positions;
    }


}
