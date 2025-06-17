package dev.ripio.cobbleloots.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import dev.ripio.cobbleloots.Cobbleloots;

public class CobblelootsYamlParser {
    /**
     * Parses a YAML file into a nested Map<String, Object> structure.
     * Supports mappings and sequences with indentation-based nesting.
     * 
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
            Map<String, Object> result = new LinkedHashMap<>();
            Object parsed = parseYaml(lines, 0);
            if (parsed instanceof Map) {
                result.putAll((Map<String, Object>) parsed);
                return result;
            } else if (parsed instanceof List) {
                Cobbleloots.LOGGER.warn("Parsed YAML is a List, expected Map: " + parsed);
            } else {
                Cobbleloots.LOGGER.warn("Parsed YAML is not a Map or List: " + parsed);
            }
            return Collections.emptyMap();
        }
    }

    public static Object parseYaml(List<String> lines, int indent) {
        Object result = null;
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).replaceAll("\\t", "  ");
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                i++;
                continue;
            }
            int currentIndent = line.indexOf(line.trim());
            if (currentIndent < indent)
                break;

            line = line.trim();
            if (line.startsWith("- ")) { // List item
                if (!(result instanceof List))
                    result = new ArrayList<>();
                String item = line.substring(2).trim();
                if (item.isEmpty()) {
                    List<String> subLines = new ArrayList<>();
                    i++;
                    while (i < lines.size()) {
                        String nextLine = lines.get(i).replaceAll("\\t", "  ");
                        int nextIndent = nextLine.indexOf(nextLine.trim());
                        if (nextIndent <= currentIndent)
                            break;
                        subLines.add(nextLine);
                        i++;
                    }
                    ((List<Object>) result).add(parseYaml(subLines, currentIndent + 2));
                    continue;
                } else {
                    ((List<Object>) result).add(parseValue(item));
                }
            } else if (line.contains(":")) { // Map entry
                if (!(result instanceof Map))
                    result = new LinkedHashMap<>();
                String[] parts = line.split(":", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (value.isEmpty()) {
                    List<String> subLines = new ArrayList<>();
                    i++;
                    while (i < lines.size()) {
                        String nextLine = lines.get(i);
                        int nextIndent = nextLine.indexOf(nextLine.trim());
                        if (nextIndent <= currentIndent)
                            break;
                        subLines.add(nextLine);
                        i++;
                    }
                    ((Map<String, Object>) result).put(key, parseYaml(subLines, currentIndent + 2));
                    continue;
                } else {
                    ((Map<String, Object>) result).put(key, parseValue(value));
                }
            } else {
                result = parseValue(line);
            }
            i++;
        }
        return result;
    }

    private static Object parseValue(String value) {
        // If the value is a inline list (e.g., "[item1, item2]"), parse it as a list
        if (value.startsWith("[") && value.endsWith("]")) {
            List<String> list = new ArrayList<>();
            String content = value.substring(1, value.length() - 1).trim();
            if (!content.isEmpty()) {
                String[] items = content.split(",");
                for (String item : items) {
                    list.add(item.trim());
                }
            }
            return list;
        }
        return value;
    }

}
