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

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.function.Function;

public class PorterStemmerNormalizer extends TokenizerExtender implements Function<CharSequence, CharSequence> {

    private final SnowballStemmer stem;

    public PorterStemmerNormalizer(Tokenizer tok) {
        super(tok);
        stem = new englishStemmer();
    }

    @Override public CharSequence apply(final CharSequence charSequence) {
        return cleanWord(charSequence);
    }

    @Override public CharSequence cleanWord(CharSequence str) {
        stem.setCurrent(str.toString());
        stem.stem();
        return stem.getCurrent();
    }

}
