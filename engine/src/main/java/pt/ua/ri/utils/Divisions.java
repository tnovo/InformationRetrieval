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
package pt.ua.ri.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author mjrp1_000
 */
public class Divisions implements Iterable<Division> {
    private final static String RESULT_GLOB = "result(*-*).txt";
    private static final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + RESULT_GLOB);

    public static int countExistingDivisions(final Path indexDir) throws IOException {
        Preconditions.checkNotNull(indexDir, "Directory Path cannot be null");
        return (int) Files.walk(indexDir, 1).filter(Files::isRegularFile).filter(pathMatcher::matches).count();
    }

    private List<Division> divisionList;

    public Divisions() {
        this(new Division[]{Division.ANY});
    }

    private Divisions(final Division[] divisionList) {
        Preconditions.checkNotNull(divisionList, "Array cannot be null");
        Preconditions.checkArgument(divisionList.length > 0, "Must have at least one division.", divisionList.length);
        this.divisionList = ImmutableList.copyOf(divisionList);
    }

    public Division getDivisionFor(final String word) {
        Preconditions.checkNotNull(word, "Word cannot be null");
        Preconditions.checkArgument(!word.isEmpty(), "Word must not be empty");
        return divisionList.stream().filter(division -> division.inRange(word)).findFirst().orElse(Division.ANY);
    }

    @Override public Iterator<Division> iterator() {
        return divisionList.iterator();
    }

    public void splitToHave(int newSize) {
        Preconditions.checkArgument(newSize >= divisionList.size(), "Cannot decrease number of divisions", newSize,
                divisionList.size());
        if (newSize != divisionList.size()) {
            final int nSplit = (int) (Math.ceil((double) (newSize) / divisionList.size()));
            this.divisionList = divisionList.stream()
                    .flatMap(division -> Arrays.stream(division.split(nSplit)).filter(Objects::nonNull))
                    .collect(ImmutableList.toImmutableList());

        }
    }

    @Override public String toString() {
        return divisionList.toString();

    }
}
