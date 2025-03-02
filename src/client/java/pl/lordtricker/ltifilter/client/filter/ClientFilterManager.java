package pl.lordtricker.ltifilter.client.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.config.ServerEntry;
import pl.lordtricker.ltifilter.client.config.ServersConfig;

import java.util.*;

public class ClientFilterManager {
    private static String activeProfile = null;
    private static final Map<String, List<String>> allProfiles = new HashMap<>();

    /**
     * Wczytuje dane z configu (ServersConfig) do allProfiles.
     * Wywoływane np. przy starcie gry albo przy /lts config reload.
     */
    public static void loadFromConfig(ServersConfig serversConfig) {
        allProfiles.clear();
        for (ServerEntry entry : serversConfig.servers) {
            String profileName = entry.profileName;
            allProfiles.putIfAbsent(profileName, new ArrayList<>());
            for (FilterEntry fe : entry.filters) {
                if (fe.material != null && !fe.material.isEmpty()) {
                    allProfiles.get(profileName).add(fe.material);
                }
            }
        }
        if (activeProfile == null) {
            activeProfile = serversConfig.defaultProfile;
        }
    }

    /**
     * Zapisuje aktualne dane (allProfiles) do configu (w polach filters).
     */
    public static void saveToConfig(ServersConfig serversConfig) {
        for (ServerEntry entry : serversConfig.servers) {
            entry.filters.clear();
        }
        for (Map.Entry<String, List<String>> profEntry : allProfiles.entrySet()) {
            String profileName = profEntry.getKey();
            List<String> items = profEntry.getValue();
            ServerEntry serverEntry = findServerEntryByProfile(serversConfig, profileName);
            if (serverEntry != null) {
                for (String material : items) {
                    serverEntry.filters.add(new FilterEntry(material));
                }
            }
        }
    }

    public static String getActiveProfile() {
        return activeProfile;
    }

    public static void setActiveProfile(String profile) {
        activeProfile = profile;
        allProfiles.putIfAbsent(profile, new ArrayList<>());
    }

    public static void addItem(String material) {
        List<String> items = allProfiles.get(activeProfile);
        if (items == null) {
            items = new ArrayList<>();
            allProfiles.put(activeProfile, items);
        }
        items.add(material);
    }

    public static void removeItem(String material) {
        List<String> items = allProfiles.get(activeProfile);
        if (items != null) {
            items.removeIf(s -> s.equalsIgnoreCase(material));
        }
    }

    public static List<String> getItems(String profile) {
        return allProfiles.getOrDefault(profile, Collections.emptyList());
    }

    public static Map<String, List<String>> getAllProfiles() {
        return allProfiles;
    }

    public static String listProfiles() {
        return String.join(", ", allProfiles.keySet());
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

    /**
     * Sprawdza, czy dany przedmiot (ItemStack) powinien być wyrenderowany z efektem beam.
     * Porównuje identyfikator przedmiotu (np. "minecraft:netherite_sword") z listą filtrów dla aktywnego profilu.
     *
     * @param stack przedmiot do sprawdzenia
     * @return true, jeśli przedmiot jest na liście filtrów, false w przeciwnym razie.
     */
    public static boolean shouldRenderItemBeam(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        List<String> allowed = getItems(getActiveProfile());
        return allowed.contains(itemId);
    }
}
