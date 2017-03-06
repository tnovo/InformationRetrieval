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

import java.util.NoSuchElementException;

public abstract class TokenizerExtender implements Tokenizer {

    private final Tokenizer tok;
    private Token next;

    TokenizerExtender(Tokenizer tok) {
        this.tok = tok;
    }

    @Override public CharSequence cleanWord(CharSequence str) {
        return str;
    }

    @Override public boolean isAcceptableWord(CharSequence str) {
        return true;
    }

    @Override public void setText(CharSequence cs) {
        tok.setText(cs);
        next = null;
        findNext();
    }

    @Override public boolean hasNext() {
        return next != null;
    }

    @Override public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Token ret = next;
        findNext();
        return ret;
    }

    private void findNext() {
        Token nx;
        next = null;
        while (tok.hasNext()) {
            nx = tok.next();
            if (isAcceptableWord(nx.getString())) {
                final CharSequence nextString = cleanWord(nx.getString());
                nx.setString(nextString.toString());
                next = nx;
                break;
            }
        }
    }
}
