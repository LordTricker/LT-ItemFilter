package pl.lordtricker.ltifilter.client.util;

import pl.lordtricker.ltifilter.client.config.FilterEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompositeKeyUtil {

    public static FilterEntry parseFilterEntry(String rawInput, int maxCount) {
        String normalized = rawInput.trim();
        if (normalized.toLowerCase().startsWith("minecraft:")
                && !normalized.contains("[") && !normalized.contains("{") && !normalized.contains("(")) {
            return new FilterEntry(normalized, maxCount);
        }

        Pattern squarePattern = Pattern.compile("\\[\\s*\"([^\"]+)\"\\s*\\]");
        Matcher squareMatcher = squarePattern.matcher(normalized);
        String material = "";
        if (squareMatcher.find()) {
            material = squareMatcher.group(1).trim();
            if (!material.toLowerCase().startsWith("minecraft:")) {
                material = "minecraft:" + material;
            }
        }

        Pattern curlyPattern = Pattern.compile("\\{\\s*\"([^\"]+)\"\\s*\\}");
        Matcher curlyMatcher = curlyPattern.matcher(normalized);
        String enchants = "";
        if (curlyMatcher.find()) {
            enchants = curlyMatcher.group(1).trim();
        }

        Pattern parenPattern = Pattern.compile("\\(\\s*\"([^\"]+)\"\\s*\\)");
        Matcher parenMatcher = parenPattern.matcher(normalized);
        String lore = "";
        if (parenMatcher.find()) {
            lore = parenMatcher.group(1).trim();
        }

        String baseName = normalized;
        baseName = baseName.replaceAll("\\(\\s*\"[^\"]+\"\\s*\\)", "");
        baseName = baseName.replaceAll("\\[\\s*\"[^\"]+\"\\s*\\]", "");
        baseName = baseName.replaceAll("\\{\\s*\"[^\"]+\"\\s*\\}", "");
        baseName = baseName.trim();

        if (material.isEmpty() && baseName.toLowerCase().startsWith("minecraft:")) {
            material = baseName;
        }

        if (baseName == null)   baseName = "";
        if (lore == null)       lore = "";
        if (material == null)   material = "";
        if (enchants == null)   enchants = "";

        return new FilterEntry(baseName, lore, material, enchants, maxCount);
    }

    public static String buildCommand(FilterEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.baseName);
        if (entry.enchants != null && !entry.enchants.isEmpty()) {
            sb.append(" {\"").append(entry.enchants).append("\"}");
        }
        if (entry.material != null && !entry.material.isEmpty()) {
            String mat = entry.material;
            if (mat.toLowerCase().startsWith("minecraft:")) {
                mat = mat.substring("minecraft:".length());
            }
            sb.append(" [\"").append(mat).append("\"]");
        }
        if (entry.lore != null && !entry.lore.isEmpty()) {
            sb.append(" (\"").append(entry.lore).append("\")");
        }
        return sb.toString().trim();
    }

}
