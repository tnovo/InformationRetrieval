package pt.ua.ri.utils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.*;

/**
 * @author tiago.novo
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void deleteDirectory(final Path dir) {
        Preconditions.checkNotNull(dir);
        logger.info("Deleting: {}", dir);
        if (!exists(dir)) {
            logger.warn("Does not exist {}", dir);
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
            logger.error("Error deleting {}", dir, ignored);
        }

    }

    public static void copyDirectory(final Path fromDir, final Path toDir) {
        logger.info("Copying from [{}] to [{}] ", fromDir, toDir);

        if (!exists(fromDir) || !isDirectory(fromDir)) {
            logger.warn("File does not exist OR is not a directory: {}", fromDir);
            return;
        }
        try {
            if (!exists(toDir) || !isDirectory(toDir)) {
                logger.info("Creating {}", toDir);
                createDirectories(toDir);
            }
            walk(fromDir).filter(path -> isRegularFile(path)).map(fromDir::relativize).forEach(path -> {
                try {
                    copy(fromDir.resolve(path), toDir.resolve(path), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                    logger.warn("Problem creating or copying.", ignored);
                }
            });
        } catch (IOException ignored) {
            logger.warn("Error copying", ignored);

        }
    }

    public static BufferedReader newBufferedReader(final Path path) {
        try {
            return Files.newBufferedReader(path);
        } catch (IOException e) {
            logger.warn("Could not create reader for {}", path);
            return null;
        }
    }
}
