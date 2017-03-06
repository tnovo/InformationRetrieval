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
package pt.ua.ri.tokenizer;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public class StopWordFilter extends TokenizerExtender implements Predicate<CharSequence> {

    private final Set<String> stopWords;

    public StopWordFilter(Tokenizer tok, Collection<String> stopWords) {
        super(tok);
        this.stopWords = ImmutableSet.copyOf(stopWords);
    }

    @Override public boolean isAcceptableWord(CharSequence str) {
        return !stopWords.contains(str.toString());
    }

    @Override public boolean test(final CharSequence charSequence) {
        return isAcceptableWord(charSequence);
    }
}
