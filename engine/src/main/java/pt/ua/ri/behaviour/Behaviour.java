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
package pt.ua.ri.behaviour;

import com.google.common.base.Converter;
import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.DocumentProperties;
import pt.ua.ri.document.DocumentSchema;
import pt.ua.ri.index.Index;
import pt.ua.ri.index.field.FieldIndex;
import pt.ua.ri.index.proximity.ProximityIndex;
import pt.ua.ri.index.simple.SimpleIndex;
import pt.ua.ri.tokenizer.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public abstract class Behaviour<S extends DocumentSchema<S>> implements Runnable {

    final Tokenizer tok;
    final Index<S> idx;

    Behaviour(Configuration<S> conf) throws IOException {
        Tokenizer tk = new WordTokenizer();
        final StreamTokenizer st = new StreamTokenizer();
        if (conf.isUsingStopwords()) {
            final HashSet<String> stopwords = new HashSet<>(Files.readAllLines(conf.getStopwordsFile()));
            tk = new StopWordFilter(tk, stopwords);
            st.addFilter(stopwords::contains);
        }
        if (conf.isUsingPorterStemmer()) {
            tk = new PorterStemmerNormalizer(tk);
            st.addNormalization(new StemmingFunction());
        }
        this.tok = tk;
        final Converter<String, DocumentProperties<S>> converter = conf.getSchema().lineParser();
        Index<S> index;
        index = conf.usePositions() ?
                new ProximityIndex<>(conf.getIndexPath(), st, converter) :
                new SimpleIndex<>(conf.getIndexPath(), st, converter);
        idx = conf.isFieldEnabled() ? new FieldIndex<>(index, conf.getSchema().fieldChecker()) : index;
    }

    public abstract void action();

    @Override public final void run() {
        action();
    }
}
