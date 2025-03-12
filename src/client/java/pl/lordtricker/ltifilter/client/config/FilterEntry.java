package pl.lordtricker.ltifilter.client.config;

public class FilterEntry {
    public String baseName;
    public String lore;
    public String material;
    public String enchants;
    public int maxCount = -1;

    public FilterEntry(String baseName, String lore, String material, String enchants, int maxCount) {
        this.baseName = baseName == null ? "" : baseName;
        this.lore = lore == null ? "" : lore;
        this.material = material == null ? "" : material;
        this.enchants = enchants == null ? "" : enchants;
        this.maxCount = maxCount;
    }

    public FilterEntry(String material, int maxCount) {
        this(material, "", material, "", maxCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseName);
        if (enchants != null && !enchants.isEmpty()) {
            sb.append(" {\"").append(enchants).append("\"}");
        }
        if (material != null && !material.isEmpty() && !baseName.equalsIgnoreCase(material)) {
            String displayMaterial = material.toLowerCase().startsWith("minecraft:")
                    ? material.substring("minecraft:".length())
                    : material;
            sb.append(" [\"").append(displayMaterial).append("\"]");
        }
        if (lore != null && !lore.isEmpty()) {
            sb.append(" (\"").append(lore).append("\")");
        }
        if (maxCount <= 0) {
            sb.append(" (bez limitu)");
        } else {
            sb.append(" (max: ").append(maxCount).append(")");
        }
        return sb.toString().trim();
    }
}