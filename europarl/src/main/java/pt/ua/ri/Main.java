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
package pt.ua.ri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.behaviour.Behaviour;
import pt.ua.ri.behaviour.SearchBehaviour;
import pt.ua.ri.behaviour.StreamIndexBehaviour;
import pt.ua.ri.config.Configuration;
import pt.ua.ri.document.EuroParlSchema;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * @param args the command line argument
     */
    public static void main(String[] args) throws IOException {

        logger.debug("DEBUG");
        logger.info("INFO");
        Behaviour option = parseArgs(args);
        if (option != null) {
            option.action();
        } else {
            printUsage();
        }
    }

    private static Behaviour parseArgs(String... args) throws IOException {
        Configuration<EuroParlSchema> confs;
        EuroParlSchema schema = EuroParlSchema.instance();
        if (args.length < 2) {
            return null;
        }
        if (args[0].equals("-x")) {
            final List<String> queries = new ArrayList<>();
            confs = new Configuration<>(schema, args[1]);
            StringBuilder bf = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (args[i].equals("-q")) {
                    queries.add(bf.toString().trim());
                    bf = new StringBuilder();
                } else {
                    bf.append(args[i]).append(' ');
                }
            }
            queries.add(bf.toString().trim());
            queries.removeIf((str) -> Objects.isNull(str) || str.isEmpty());
            final Pattern pat = Pattern.compile("p\\((.+)\\)");
            queries.replaceAll((String t) -> pat.matcher(t).replaceAll("\"$1\""));
            logger.info(queries.toString());
            return new SearchBehaviour<>(confs, queries);
        } else {
            confs = new Configuration<>(schema);
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-bd": // set base path
                        confs.setBasePath(args[++i]);
                        break;
                    case "-idx": // folder for index
                        confs.setIndexPath(Paths.get(args[++i]));
                        break;
                    case "-ps": // enable porter stemmer
                        confs.enablePorterStemmer();
                        break;
                    case "-sw": // enable stopwords
                        confs.enableStopwords(Paths.get(args[++i]));
                        break;
                    case "-cp": // path to corpus
                        confs.setCorpusPath(Paths.get(args[++i]));
                        break;
                    default:
                        logger.info("Unknown option: " + args[i]);
                }
            }
            confs.write(confs.getConfigurationFile());
            logger.info("Configuration {}", confs);
            return new StreamIndexBehaviour<>(confs);
        }
    }

    private static void printUsage() {
        final String[][] usage = {{"Search", "-x <conf file>", "Configuration file"},
                {"", "-q <query>", "Query to search for (allows multiple)(for proximity search use p(query)~d)"},
                {"Index", "-bd <basepath>",
                        "Folder to store results (Default: " + Configuration.PP_BASE_PATH_DEFAULT + ")"},
                {"", "-ps", "Enable Stemming"}, {"", "-sw <path to stopwords file>", "Enable stopwords"},
                {"", "-cp <path to corpus>", "Path to corpus (Default: " + Configuration.PP_CORPUS_PATH_DEFAULT + ")"}};

        logger.info("Usage:");
        logger.info("Should choose one (and only one) of the following options (Index/Search):");
        for (String[] usage1 : usage) {
            logger.info("{} : {} : {}", usage1[0], usage1[1], usage1[2]);
        }
    }
}
