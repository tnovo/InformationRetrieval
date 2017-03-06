/*
 * Copyright (C) 2016 Tiago Novo <tmnovo at ua.pt>
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

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class Division {

    static final Division ANY = new Division("A", "Z");
    private final String prefixStart, prefixEnd;

    private Division(String prefixStart, String prefixEnd) {
        Preconditions.checkNotNull(prefixStart);
        Preconditions.checkNotNull(prefixEnd);
        Preconditions.checkArgument(prefixStart.compareToIgnoreCase(prefixEnd) < 0,
                "Prefixes must be in alphabetical order");
        this.prefixStart = prefixStart.toUpperCase();
        this.prefixEnd = prefixEnd.toUpperCase();
    }

    public String getFilename() {
        return "result" + toString() + ".txt";
    }

    public boolean inRange(final String str) {
        Preconditions.checkNotNull(str);
        return (prefixStart.compareToIgnoreCase(str) <= 0) &&
               (prefixEnd.compareToIgnoreCase(str) >= 0 || str.toUpperCase().startsWith(prefixEnd));
    }

    @Override public String toString() {
        return Stream.of(prefixStart, prefixEnd).collect(joining("-", "(", ")"));
    }

    // TODO: Refactor this shit. it's ugly as fuck
    Division[] split(final int nDivs) {
        Preconditions.checkArgument(nDivs > 0);
        final int tNDivs = Math.min(13, nDivs);
        final StringBuffer mp[][] = new StringBuffer[tNDivs][2];

        mp[0][0] = new StringBuffer(this.prefixStart);
        mp[tNDivs - 1][1] = new StringBuffer(this.prefixEnd);
        for (int i = 1; i < tNDivs; i++) {
            mp[i - 1][1] = new StringBuffer();
            mp[i][0] = new StringBuffer();
        }
        int j;
        for (j = 0;
             j < Math.min(prefixStart.length(), prefixEnd.length()) && prefixStart.charAt(j) == prefixEnd.charAt(j);
             j++) {
            char c = prefixStart.charAt(j);
            for (int i = 1; i < tNDivs; i++) {
                mp[i - 1][1].append(c);
                mp[i][0].append(c);
            }
        }

        char st;
        if (j < prefixStart.length()) {
            st = prefixStart.charAt(j);
        } else {
            st = 'A';
        }
        char en;
        if (j < prefixEnd.length()) {
            en = prefixEnd.charAt(j);
        } else {
            en = 'Z';
        }

        int step = (en - st + 1) / tNDivs;


        for (int i = 1; i < tNDivs; i++) {
            char c = (char) (st + step * i);
            mp[i - 1][1].append((char) (c - 1));
            mp[i][0].append(c);
        }

        return IntStream.range(0, tNDivs)
                .mapToObj(idx -> new Division(mp[idx][0].toString(), mp[idx][1].toString()))
                .toArray(Division[]::new);
    }

}
