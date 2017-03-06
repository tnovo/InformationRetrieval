/* 
 * Copyright (C) 2015 Tiago
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
package pt.ua.ri.config;

import com.google.common.base.MoreObjects;
import pt.ua.ri.document.DocumentSchema;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author Tiago
 */
public final class Configuration<S extends DocumentSchema<S>> {
    public final static String DOCS_FILE = "docs.txt";
    public final static String PP_BASE_PATH_DEFAULT = System.getProperty("user.dir");
    public final static String PP_CORPUS_PATH_DEFAULT = Paths.get(PP_BASE_PATH_DEFAULT, "Europarl-v7-en").toString();
    private final static String PP_BASE_PATH = "BASE_PATH";
    private final static String PP_CONF_FILE_PATH = "CONF_FILE";
    private final static String PP_CONF_FILE_PATH_DEFAULT = Paths.get(PP_BASE_PATH_DEFAULT, "config.xml").toString();
    private final static String PP_CORPUS_PATH = "CORPUS_PATH";
    private final static String PP_INDEX_PATH = "INDEX_FOLDER";
    private final static String PP_INDEX_PATH_DEFAULT = Paths.get(PP_BASE_PATH_DEFAULT, "index").toString();
    private final static String PP_SW_FILE_PATH = "SW_FILE";
    private final static String PP_SW_FILE_PATH_DEFAULT = Paths.get(PP_BASE_PATH_DEFAULT, "stopwords.txt").toString();
    private final static String PP_USE_PS = "USE_PS";
    private final static String PP_USE_SW = "USE_SW";

    private static Properties propertiesFromFile(Path confPath) throws IOException {
        Properties p = new Properties();
        p.loadFromXML(Files.newInputStream(confPath));
        return p;
    }

    private final S schema;
    private Path basePath;
    private Path corpusPath;
    private Path indexPath;
    private Path configPath;
    private Path stopWordsFile;
    private boolean usingStopwords;
    private boolean usingPorterstemmer;

    public Configuration(final S schema) throws IOException {
        this(schema, new Properties());
    }

    public Configuration(final S schema, String confFilename) throws IOException {
        this(schema, Paths.get(confFilename));
    }

    private Configuration(final S schema, Path config) throws IOException {
        this(schema, propertiesFromFile(config));
    }

    private Configuration(final S schema, Properties configs) throws IOException {
        this.schema = schema;
        setBasePath(Paths.get(configs.getProperty(PP_BASE_PATH, PP_BASE_PATH_DEFAULT)));
        setIndexPath(Paths.get(configs.getProperty(PP_INDEX_PATH, PP_INDEX_PATH_DEFAULT)));
        this.configPath = getBasePath().relativize(
                Paths.get(configs.getProperty(PP_CONF_FILE_PATH, PP_CONF_FILE_PATH_DEFAULT)));
        this.usingStopwords = Boolean.parseBoolean(configs.getProperty(PP_USE_SW, Boolean.toString(false)));
        this.stopWordsFile = (this.usingStopwords) ?
                             Paths.get(configs.getProperty(PP_SW_FILE_PATH, PP_SW_FILE_PATH_DEFAULT)) :
                             null;
        this.usingPorterstemmer = Boolean.parseBoolean(configs.getProperty(PP_USE_PS, Boolean.toString(false)));
        this.corpusPath = Paths.get(configs.getProperty(PP_CORPUS_PATH, PP_CORPUS_PATH_DEFAULT));
    }

    public void enablePorterStemmer() {
        this.usingPorterstemmer = true;
    }

    public void enableStopwords(Path swFile) throws IOException {

        if (swFile != null) {
            this.stopWordsFile = swFile.getFileName();
            Files.copy(swFile, getStopwordsFile(), COPY_ATTRIBUTES, REPLACE_EXISTING);
            this.usingStopwords = true;
        } else {
            this.stopWordsFile = null;
            this.usingStopwords = false;
        }
    }

    public Path getConfigurationFile() throws IOException {
        Path c = getBasePath().resolve(configPath);

        try {
            Files.createFile(c);
        } catch (FileAlreadyExistsException ex) {
            // ignore
        }
        return c;
    }

    public Path getCorpusPath() {
        return corpusPath.toAbsolutePath();
    }

    public void setCorpusPath(Path corpusPath) {
        this.corpusPath = corpusPath;
    }

    public Path getIndexPath() {
        return getBasePath().resolve(indexPath);
    }

    public void setIndexPath(Path indexFolder) {
        this.indexPath = getBasePath().relativize(indexFolder);
    }

    public S getSchema() {
        return schema;
    }

    public Path getStopwordsFile() {
        return basePath.resolve(stopWordsFile);
    }

    public boolean isFieldEnabled() {
        return false;
    }

    public boolean isUsingPorterStemmer() {
        return usingPorterstemmer;
    }

    public boolean isUsingStopwords() {
        return usingStopwords;
    }

    public void setBasePath(String bp) throws IOException {
        setBasePath(Paths.get(bp));
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("basePath", basePath)
                .add("corpusPath", corpusPath)
                .add("indexPath", indexPath)
                .add("configPath", configPath)
                .add("stopWordsFile", stopWordsFile)
                .add("usingStopwords", usingStopwords)
                .add("usingPorterstemmer", usingPorterstemmer)
                .toString();
    }

    public boolean usePositions() {
        return true;
    }

    public void write(Path path) throws IOException {
        Properties p = new Properties();
        p.setProperty(PP_BASE_PATH, getBasePath().toString());
        p.setProperty(PP_CONF_FILE_PATH, path.getFileName().toString());
        p.setProperty(PP_INDEX_PATH, getBasePath().relativize(getIndexPath()).toString());
        p.setProperty(PP_USE_PS, Boolean.toString(usingPorterstemmer));
        p.setProperty(PP_USE_SW, Boolean.toString(usingStopwords));
        if (usingStopwords) {
            p.setProperty(PP_SW_FILE_PATH, getStopwordsFile().toString());
        }
        p.setProperty(PP_CORPUS_PATH, getCorpusPath().toString());

        p.storeToXML(Files.newOutputStream(path, WRITE, TRUNCATE_EXISTING, CREATE), null);
    }

    private Path getBasePath() {
        return basePath != null ? basePath : Paths.get(System.getProperty("user.dir"));
    }

    private void setBasePath(Path bp) throws IOException {
        this.basePath = bp;
        if (!Files.exists(bp)) {
            Files.createDirectories(bp);
        }
    }

}
