package pt.ua.ri.reader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.ri.document.Document;
import pt.ua.ri.document.DocumentProperties;
import pt.ua.ri.document.EuroParlSchema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tiago.novo
 */
public class EuroParlFileParser extends AbstractFileParser<EuroParlSchema> implements FileParser<EuroParlSchema> {
    private final static String CHAPTER_TAG_START = "<CHAPTER";
    private final static String SPEAKER_TAG_START = "<SPEAKER";
    private final static Logger logger = LoggerFactory.getLogger(EuroParlFileParser.class);
    private final Pattern regex = Pattern.compile("([A-Z]+)=(?:(?:\"(.*?)\")|([^ \"]+))[ >]");

    public EuroParlFileParser(Path path) throws IOException {
        super(path, EuroParlSchema.instance());
        properties.put("FILENAME", path.getFileName().toString());
        properties.put("DATE", properties.get("FILENAME").toString().substring(3, 11));
        fetchNextDocument();
    }

    @Override protected void fetchNextDocument() {
        logger.debug("Fetching next document from {}", properties.get("FILENAME"));
        nextDocument = null;

        while (lines.hasNext()) {
            String line = lines.peek();
            logger.debug("Checking: {}", line);
            if (!line.isEmpty() && line.startsWith("<")) {
                int idxOfSpace = line.indexOf(' ');
                if (idxOfSpace >= 0) {
                    String tagName = line.substring(1, idxOfSpace);
                    if ("CHAPTER".equals(tagName)) {
                        parseChapterHeader();
                    } else if ("SPEAKER".equals(tagName)) {
                        parseDocumentBody();
                        break;
                    }
                }
            }
            if (lines.hasNext()) {
                lines.next();
            }

        }
    }

    private void parseChapterHeader() {
        String line = lines.next();
        logger.debug("Parsing header starting at {}", line);

        int start = 3 + line.indexOf("ID=", CHAPTER_TAG_START.length());
        int end = line.length() - 2;
        while (line.charAt(start) == '\"') {
            start++;
        }
        while (line.charAt(end) == '\"') {
            end--;
        }


        final String chapterId = line.substring(start, end + 1);
        final StringBuilder title = new StringBuilder();
        while (lines.hasNext()) {
            String nextLine = lines.peek();
            if (nextLine.startsWith("<") &&
                (nextLine.startsWith(SPEAKER_TAG_START) || nextLine.startsWith(CHAPTER_TAG_START)))
            {
                logger.debug("Chapter title ended at: {} ", nextLine);
                break;
            } else {
                title.append(lines.next()).append(' ');
            }
        }
        //      logger.debug(chapterId);
        //     logger.debug(title);

        properties.put("CHAPTER_ID", chapterId);
        properties.put("CHAPTER_TITLE", title.toString());

    }

    private void parseDocumentBody() {
        final String line = lines.next();
        final int idxOfSpace = line.indexOf(' ', SPEAKER_TAG_START.length());
        logger.debug("Parsing document body starting at {} ", line);
        final Matcher m = regex.matcher(line.substring(idxOfSpace, line.length() - 1));

        if (m.find()) {
            do {
                try {
                    String key = m.group(1).trim();
                    String value = (m.group(2) != null ? m.group(2) : m.group(3)).trim();
                    logger.debug("SPEAKER_{}: {}", key.toUpperCase(), value);
                    properties.put("SPEAKER_" + key.toUpperCase(), value.trim().toUpperCase());
                } catch (Exception ex) {
                    logger.warn("Error at: {} on {}", m.group(0), line, ex);

                    for (int i = 0; i <= m.groupCount(); i++) {
                        logger.warn("GROUP[{}]={}", i, m.group(i));
                    }
                    throw ex;
                }
            } while (m.find());
        } else {
            logger.warn("No groups found on line {}", line);
        }

        final ImmutableList.Builder<String> builder = ImmutableList.builder();


        while (lines.hasNext()) {
            String nextLine = lines.next();
            if (!nextLine.startsWith("<")) {
                builder.add(nextLine);
            } else if ((nextLine.startsWith(SPEAKER_TAG_START) || nextLine.startsWith(CHAPTER_TAG_START))) {
                logger.debug("Speaker intervention ended at {}", nextLine);
                break;
            } else {
                logger.debug("Skipping {}", nextLine);
            }

        }
        final List<String> strings = builder.build();
        logger.debug("Document Properties={} , lines={}", properties, strings);
        nextDocument = new Document<>(new DocumentProperties<>(getSchema(), ImmutableMap.copyOf(properties)), strings);
        logger.debug("Created Document {}", nextDocument);
        properties.keySet().removeIf(key -> key.startsWith("SPEAKER_"));
    }
}
