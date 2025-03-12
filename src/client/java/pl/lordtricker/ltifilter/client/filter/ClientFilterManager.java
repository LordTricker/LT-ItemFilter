package pl.lordtricker.ltifilter.client.filter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.config.ServerEntry;
import pl.lordtricker.ltifilter.client.config.ServersConfig;
import pl.lordtricker.ltifilter.client.util.EnchantMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientFilterManager {
    private static String activeProfile = null;
    private static final Map<String, List<FilterEntry>> allProfiles = new HashMap<>();

    public static void loadFromConfig(ServersConfig serversConfig) {
        clearAllProfiles();
        for (ServerEntry entry : serversConfig.servers) {
            String profileName = entry.profileName;
            allProfiles.putIfAbsent(profileName, new java.util.ArrayList<>());
            for (FilterEntry fe : entry.filters) {
                allProfiles.get(profileName).add(fe);
            }
        }
        if (activeProfile == null) {
            activeProfile = serversConfig.defaultProfile;
        }
    }

    public static void saveToConfig(ServersConfig serversConfig) {
        for (ServerEntry entry : serversConfig.servers) {
            entry.filters.clear();
        }
        for (Map.Entry<String, List<FilterEntry>> profEntry : allProfiles.entrySet()) {
            String profileName = profEntry.getKey();
            List<FilterEntry> items = profEntry.getValue();
            ServerEntry serverEntry = findServerEntryByProfile(serversConfig, profileName);
            if (serverEntry != null) {
                serverEntry.filters.addAll(items);
            }
        }
    }

    public static String getActiveProfile() {
        return activeProfile;
    }

    public static void setActiveProfile(String profile) {
        activeProfile = profile;
        allProfiles.putIfAbsent(profile, new java.util.ArrayList<>());
    }

    public static void addItem(FilterEntry entry) {
        List<FilterEntry> items = allProfiles.get(activeProfile);
        if (items == null) {
            items = new java.util.ArrayList<>();
            allProfiles.put(activeProfile, items);
        }
        items.removeIf(fe -> entriesEqual(fe, entry));
        items.add(entry);
    }

    public static void removeItem(FilterEntry entry) {
        List<FilterEntry> items = allProfiles.get(activeProfile);
        if (items != null) {
            items.removeIf(fe -> entriesEqual(fe, entry));
        }
    }

    public static List<FilterEntry> getItems(String profile) {
        List<FilterEntry> list = allProfiles.getOrDefault(profile, Collections.emptyList());
        List<FilterEntry> result = new ArrayList<>();
        for (FilterEntry fe : list) {
            if (fe != null) {
                result.add(fe);
            }
        }
        return result;
    }

    public static boolean hasItem(String profile, FilterEntry entry) {
        List<FilterEntry> items = allProfiles.get(profile);
        if (items == null) return false;
        for (FilterEntry fe : items) {
            if (entriesEqual(fe, entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean entriesEqual(FilterEntry a, FilterEntry b) {
        return safeEqualsIgnoreCase(a.baseName, b.baseName)
                && safeEqualsIgnoreCase(a.lore, b.lore)
                && safeEqualsIgnoreCase(a.material, b.material)
                && safeEqualsIgnoreCase(a.enchants, b.enchants);
    }

    private static boolean safeEqualsIgnoreCase(String x, String y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return x.equalsIgnoreCase(y);
    }

    public static String listProfiles() {
        return String.join(", ", allProfiles.keySet());
    }

    public static void clearAllProfiles() {
        allProfiles.clear();
    }

    private static ServerEntry findServerEntryByProfile(ServersConfig serversConfig, String profileName) {
        if (serversConfig.servers == null) return null;
        for (ServerEntry entry : serversConfig.servers) {
            if (entry.profileName.equalsIgnoreCase(profileName)) {
                return entry;
            }
        }
        return null;
    }

    private static final Pattern NEWER_PATTERN = Pattern.compile(
            "ResourceKey\\[\\s*minecraft:enchantment\\s*/\\s*minecraft:([^\\]]+)\\]\\s*=.*?=>\\s*(\\d+)"
    );
    private static final Pattern OLDER_PATTERN = Pattern.compile(
            "\\{id:\"([^\"]+)\",lvl:(\\d+)s\\}"
    );

    /**
     * Metoda sprawdzająca, czy dany przedmiot (ItemStack) spełnia wszystkie kryteria filtra.
     */
    public static boolean matchesFilter(FilterEntry filter, ItemStack stack) {
        if (filter == null || stack == null || stack.isEmpty()) return false;

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        if (!filter.material.isEmpty() && !itemId.equalsIgnoreCase(filter.material)) {
            return false;
        }

        String customName = stack.getName().getString().toLowerCase();
        if (!filter.baseName.isEmpty() && !filter.baseName.equalsIgnoreCase(filter.material)) {
            String expectedName = filter.baseName.toLowerCase();
            if (expectedName.startsWith("minecraft:")) {
                expectedName = expectedName.substring("minecraft:".length());
            }
            if (!customName.contains(expectedName)) {
                return false;
            }
        }

        List<Text> tooltipLines = stack.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC);
        StringBuilder tooltipBuilder = new StringBuilder();
        for (Text line : tooltipLines) {
            String plain = line.getString();
            String noColor = pl.lordtricker.ltifilter.client.util.ColorStripUtils.stripAllColorsAndFormats(plain);
            tooltipBuilder.append(noColor).append(" ");
        }
        String tooltip = tooltipBuilder.toString().toLowerCase();
        String normTooltip = tooltip.replaceAll("\\s+", " ").trim();
        String normLore = filter.lore.toLowerCase().replaceAll("\\s+", " ").trim();
        if (!normLore.isEmpty() && !normTooltip.contains(normLore)) {
            return false;
        }

        String rawEnchants = stack.getEnchantments().toString();
        StringBuilder enchantBuilder = new StringBuilder();
        boolean foundAny = false;
        Matcher matcherNew = NEWER_PATTERN.matcher(rawEnchants);
        while (matcherNew.find()) {
            foundAny = true;
            String enchId = matcherNew.group(1).trim();
            String levelStr = matcherNew.group(2).trim();
            String shortEnchant = enchId + levelStr;
            String mappedEnchant = EnchantMapper.mapEnchant(shortEnchant, true);
            if (enchantBuilder.length() > 0) {
                enchantBuilder.append(",");
            }
            enchantBuilder.append(mappedEnchant);
        }
        if (!foundAny) {
            Matcher matcherOld = OLDER_PATTERN.matcher(rawEnchants);
            while (matcherOld.find()) {
                String enchId = matcherOld.group(1).trim();
                String levelStr = matcherOld.group(2).trim();
                if (enchId.startsWith("minecraft:")) {
                    enchId = enchId.substring("minecraft:".length());
                }
                String shortEnchant = enchId + levelStr;
                String mappedEnchant = EnchantMapper.mapEnchant(shortEnchant, false);
                if (enchantBuilder.length() > 0) {
                    enchantBuilder.append(",");
                }
                enchantBuilder.append(mappedEnchant);
            }
        }
        String enchantmentsString = enchantBuilder.toString().toLowerCase();
        if (!filter.enchants.isEmpty() && !enchantmentsString.contains(filter.enchants.toLowerCase())) {
            return false;
        }

        return true;
    }


    /**
     * Sprawdza, czy dany przedmiot powinien być wyrenderowany z efektem beam.
     * Dla czyszczenia ekwipunku użyjemy oddzielnej logiki (patrz InventoryCleaner).
     */
    public static boolean shouldRenderItemBeam(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        List<FilterEntry> filters = getItems(getActiveProfile());
        for (FilterEntry fe : filters) {
            if (matchesFilter(fe, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void reinitProfilesFromConfig(ServersConfig serversConfig) {
        loadFromConfig(serversConfig);
        String address = pl.lordtricker.ltifilter.client.LtifilterClient.getServerAddress();
        ServerEntry serverEntry = findServerEntryByAddress(serversConfig, address);
        if (serverEntry != null) {
            setActiveProfile(serverEntry.profileName);
        } else {
            setActiveProfile(serversConfig.defaultProfile);
        }
    }

    private static ServerEntry findServerEntryByAddress(ServersConfig serversConfig, String address) {
        if (serversConfig == null || serversConfig.servers == null)
            return null;
        for (ServerEntry entry : serversConfig.servers) {
            for (String domain : entry.domains) {
                if (address.equalsIgnoreCase(domain) ||
                        address.toLowerCase().endsWith("." + domain.toLowerCase())) {
                    return entry;
                }
            }
        }
        return null;
    }
}