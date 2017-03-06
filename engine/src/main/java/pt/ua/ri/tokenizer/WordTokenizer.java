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
package pt.ua.ri.tokenizer;

import org.apache.commons.collections4.trie.PatriciaTrie;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class WordTokenizer implements Tokenizer {

    private final static Pattern word = Pattern.compile("\\p{L}{3,}(?:(?:-(?:\\p{L}){2,})|'s)?");
    private final Map<String, Token> tokens;
    private final BreakIterator bi;
    private final StringBuilder bf;
    private Token current;
    private int nextIndex;

    public WordTokenizer() {
        this(Locale.ENGLISH.getCountry());
    }

    private WordTokenizer(String locale) {
        checkNotNull(locale);
        bi = BreakIterator.getWordInstance(Locale.forLanguageTag(locale));
        tokens = new PatriciaTrie<>();
        bf = new StringBuilder();
    }

    @Override public CharSequence cleanWord(final CharSequence word) {
        checkNotNull(word);
        return word.codePoints()
                .filter(Character::isLetter)
                .map(Character::toLowerCase)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);
    }

    @Override public boolean isAcceptableWord(final CharSequence str) {
        checkNotNull(str);
        return word.matcher(str).find();
    }

    @Override public void setText(final CharSequence cs) {
        checkNotNull(cs);
        bf.insert(0, cs);
        bf.setLength(cs.length());
        bi.setText(cs.toString());
        nextIndex = bi.first();
        findNext();
    }

    @Override public boolean hasNext() {
        return nonNull(bf) && (nextIndex != BreakIterator.DONE);
    }

    @Override public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Token ret = current;
        findNext();
        return ret;
    }

    private void findNext() {
        checkState(bf.length() > 0);
        CharSequence dirtySequence;
        String dirtyString;
        Token nextToken;
        do {
            int firstIndex = nextIndex;
            nextIndex = bi.next();
            dirtySequence = bf.subSequence(firstIndex, (nextIndex == BreakIterator.DONE) ? bi.last() : nextIndex);
            dirtyString = dirtySequence.toString();
            nextToken = tokens.getOrDefault(dirtyString, null);

        } while (nextToken == null && !isAcceptableWord(dirtySequence) && hasNext());
        if (nextToken == null) {
            final String afterNormalization = cleanWord(dirtySequence).toString();
            nextToken = tokens.compute(afterNormalization, (key, token) -> token == null ? new Token(key) : token);
            // also store the dirty sequence, to increase performance
            tokens.putIfAbsent(dirtyString, nextToken);
        }
        current = nextToken;

    }

}
