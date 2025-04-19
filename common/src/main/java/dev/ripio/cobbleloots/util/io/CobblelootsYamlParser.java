package dev.ripio.cobbleloots.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CobblelootsYamlParser {
    /**
     * Parses a YAML file into a nested Map<String, Object> structure.
     * Supports mappings and sequences with indentation-based nesting.
     * @param path Path to the YAML file
     * @return Nested Map representing the YAML structure
     * @throws IOException if file reading fails
     */
    public static Map<String, Object> parse(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return parseLines(lines);
        }
    }

    // Internal recursive parser
    private static Map<String, Object> parseLines(List<String> lines) {
        return parseBlock(lines, 0, 0).map;
    }

    // Helper class to return both the parsed map and the next line index
    private static class ParseResult {
        Map<String, Object> map;
        int nextLine;
        ParseResult(Map<String, Object> map, int nextLine) {
            this.map = map;
            this.nextLine = nextLine;
        }
    }

    // Parses a block of lines at a given indentation level
    private static ParseResult parseBlock(List<String> lines, int start, int indent) {
        Map<String, Object> map = new LinkedHashMap<>();
        int i = start;
        while (i < lines.size()) {
            String raw = lines.get(i);
            String line = raw.replaceAll("\\t", "    "); // tabs to spaces
            int leading = countLeadingSpaces(line);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++;
                continue;
            }
            if (leading < indent) {
                break;
            }
            if (trimmed.startsWith("- ")) {
                // Sequence
                String key = null;
                List<Object> list = new ArrayList<>();
                while (i < lines.size()) {
                    String seqRaw = lines.get(i);
                    String seqLine = seqRaw.replaceAll("\\t", "    ");
                    int seqLeading = countLeadingSpaces(seqLine);
                    String seqTrimmed = seqLine.trim();
                    if (seqTrimmed.isEmpty() || seqTrimmed.startsWith("#")) {
                        i++;
                        continue;
                    }
                    if (seqLeading != indent || !seqTrimmed.startsWith("- ")) {
                        break;
                    }
                    String value = seqTrimmed.substring(2).trim();
                    if (!value.isEmpty()) {
                        list.add(value);
                        i++;
                    } else {
                        // Nested block in sequence
                        ParseResult nested = parseBlock(lines, i + 1, indent + 2);
                        list.add(nested.map);
                        i = nested.nextLine;
                    }
                }
                // Anonymous list (no key)
                map.put(UUID.randomUUID().toString(), list);
                continue;
            }
            int colon = trimmed.indexOf(":");
            if (colon > 0) {
                String key = trimmed.substring(0, colon).trim();
                String value = trimmed.substring(colon + 1).trim();
                if (!value.isEmpty()) {
                    map.put(key, value);
                    i++;
                } else {
                    // Nested block
                    ParseResult nested = parseBlock(lines, i + 1, indent + 2);
                    map.put(key, nested.map);
                    i = nested.nextLine;
                }
            } else {
                i++;
            }
        }
        return new ParseResult(map, i);
    }

    private static int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') count++;
        return count;
    }
}
