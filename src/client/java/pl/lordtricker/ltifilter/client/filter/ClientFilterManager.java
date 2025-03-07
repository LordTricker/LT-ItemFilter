package pl.lordtricker.ltifilter.client.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.config.ServerEntry;
import pl.lordtricker.ltifilter.client.config.ServersConfig;

import java.util.*;

public class ClientFilterManager {
    private static String activeProfile = null;
    private static final Map<String, List<FilterEntry>> allProfiles = new HashMap<>();

    /**
     * Wczytuje dane z configu (ServersConfig) do allProfiles.
     * Wywoływane np. przy starcie gry albo przy /ltf config reload.
     */
    public static void loadFromConfig(ServersConfig serversConfig) {
        clearAllProfiles();
        for (ServerEntry entry : serversConfig.servers) {
            String profileName = entry.profileName;
            allProfiles.putIfAbsent(profileName, new ArrayList<>());
            for (FilterEntry fe : entry.filters) {
                if (fe.material != null && !fe.material.isEmpty()) {
                    allProfiles.get(profileName).add(fe);
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
        allProfiles.putIfAbsent(profile, new ArrayList<>());
    }

    /**
     * Dodaje przedmiot bez limitu.
     */
    public static void addItem(String material) {
        addItem(material, -1);
    }

    /**
     * Dodaje przedmiot do aktywnego profilu wraz z limitem.
     * Jeśli maxCount == -1, to oznacza brak limitu.
     */
    public static void addItem(String material, int maxCount) {
        List<FilterEntry> items = allProfiles.get(activeProfile);
        if (items == null) {
            items = new ArrayList<>();
            allProfiles.put(activeProfile, items);
        }
        items.add(new FilterEntry(material, maxCount));
    }

    /**
     * Usuwa przedmiot z aktywnego profilu.
     */
    public static void removeItem(String material) {
        List<FilterEntry> items = allProfiles.get(activeProfile);
        if (items != null) {
            items.removeIf(fe -> fe.material.equalsIgnoreCase(material));
        }
    }

    /**
     * Zwraca listę filtrów (FilterEntry) dla danego profilu.
     */
    public static List<FilterEntry> getItems(String profile) {
        return allProfiles.getOrDefault(profile, Collections.emptyList());
    }

    public static Map<String, List<FilterEntry>> getAllProfiles() {
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
     * Czyści wszystkie profile.
     */
    public static void clearAllProfiles() {
        allProfiles.clear();
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

    /**
     * Reinicjalizuje profile na podstawie configu.
     * Czyści stare profile, ładuje nowe filtry oraz ustawia aktywny profil
     * w zależności od adresu serwera lub profilu domyślnego.
     */
    public static void reinitProfilesFromConfig(ServersConfig serversConfig) {
        loadFromConfig(serversConfig);
        String address = LtifilterClient.getServerAddress();
        ServerEntry serverEntry = findServerEntryByAddress(serversConfig, address);
        if (serverEntry != null) {
            setActiveProfile(serverEntry.profileName);
        } else {
            setActiveProfile(serversConfig.defaultProfile);
        }
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
        List<FilterEntry> allowed = getItems(getActiveProfile());
        for (FilterEntry fe : allowed) {
            if (fe.material.equalsIgnoreCase(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza, czy przedmiot o danym identyfikatorze znajduje się w filtrach danego profilu.
     */
    public static boolean hasItem(String profile, String material) {
        List<FilterEntry> items = allProfiles.get(profile);
        if (items == null) return false;
        for (FilterEntry fe : items) {
            if (fe.material.equalsIgnoreCase(material)) {
                return true;
            }
        }
        return false;
    }
}
