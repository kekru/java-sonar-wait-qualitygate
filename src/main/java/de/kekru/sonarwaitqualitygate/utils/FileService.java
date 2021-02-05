package de.kekru.sonarwaitqualitygate.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileService {
  private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
  private static final String PROPERTY_FILE_DELIMITER = "=";

  public static List<String> readFromFile(final String filename) {
    try (final BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
      return reader.lines().collect(Collectors.toList());

    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + filename, e);
    }
  }

  public static Map<String, String> readFromFileToMap(final String filename) {
    final List<String> lines = readFromFile(filename);
    final Map<String, String> result = new HashMap<>();

    for (final String line : lines) {
      if (line.contains(PROPERTY_FILE_DELIMITER)) {

        final int indexDelimiter = line.indexOf(PROPERTY_FILE_DELIMITER);
        final String key = line.substring(0, indexDelimiter);
        final String value = line.substring(indexDelimiter + PROPERTY_FILE_DELIMITER.length());
        result.put(key, value);

      } else {
        LOG.debug("Could not parse line to Java Map: " + line);
      }
    }

    return result;
  }
}
