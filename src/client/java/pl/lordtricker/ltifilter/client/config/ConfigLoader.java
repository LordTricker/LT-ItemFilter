package pl.lordtricker.ltifilter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MAIN_CONFIG_FILE_NAME = "ltitemfilter-config.json";
    private static final Path MOD_CONFIG_DIR;

    static {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        MOD_CONFIG_DIR = configDir.resolve("LT-Mods").resolve("LT-ItemFilter");
        try {
            if (!Files.exists(MOD_CONFIG_DIR)) {
                Files.createDirectories(MOD_CONFIG_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServersConfig loadConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(MAIN_CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            ServersConfig defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            ServersConfig loadedConfig = GSON.fromJson(reader, ServersConfig.class);
            fixNullFields(loadedConfig);
            return loadedConfig;
        } catch (IOException e) {
            e.printStackTrace();
            return new ServersConfig();
        }
    }

    public static void saveConfig(ServersConfig config) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(MAIN_CONFIG_FILE_NAME);

        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fixNullFields(ServersConfig config) {
        if (config.servers == null) return;
        for (ServerEntry serverEntry : config.servers) {
            if (serverEntry.filters == null) continue;
            for (FilterEntry fe : serverEntry.filters) {
                if (fe.baseName == null)   fe.baseName = "";
                if (fe.lore == null)       fe.lore = "";
                if (fe.material == null)   fe.material = "";
                if (fe.enchants == null)   fe.enchants = "";
            }
        }
    }

    private static ServersConfig createDefaultConfig() {
        ServersConfig cfg = new ServersConfig();
        cfg.defaultProfile = "default";

        cfg.beamSettings.hexColor = "#80ccff";
        cfg.beamSettings.alpha = 1.0f;
        cfg.beamSettings.height = 1.0f;
        cfg.beamSettings.radius = 0.05f;
        cfg.beamSettings.verticalOffset = 0.0f;

        cfg.cleanerSettings.throwIntervalTicks = 1;
        cfg.cleanerSettings.blockDurationMs = 200;
        cfg.cleanerSettings.doNotCleanSlots = List.of(1);

        ServerEntry server1 = new ServerEntry();
        server1.domains = List.of("minestar.pl", "anarchia.gg", "tryhc.net", "rapy.pl");
        server1.profileName = "pvp";

        server1.filters.add(new FilterEntry("diamond pickaxe", "", "minecraft:diamond_pickaxe", "", -1));
        server1.filters.add(new FilterEntry("diamond axe", "", "minecraft:diamond_axe", "", -1));
        server1.filters.add(new FilterEntry("diamond sword", "", "minecraft:diamond_sword", "", -1));
        server1.filters.add(new FilterEntry("diamond helmet", "", "minecraft:diamond_helmet", "", -1));
        server1.filters.add(new FilterEntry("diamond chestplate", "", "minecraft:diamond_chestplate", "", -1));
        server1.filters.add(new FilterEntry("diamond leggings", "", "minecraft:diamond_leggings", "", -1));
        server1.filters.add(new FilterEntry("diamond boots", "", "minecraft:diamond_boots", "", -1));
        server1.filters.add(new FilterEntry("netherite pickaxe", "", "minecraft:netherite_pickaxe", "", -1));
        server1.filters.add(new FilterEntry("netherite axe", "", "minecraft:netherite_axe", "", -1));
        server1.filters.add(new FilterEntry("netherite sword", "", "minecraft:netherite_sword", "", -1));
        server1.filters.add(new FilterEntry("netherite helmet", "", "minecraft:netherite_helmet", "", -1));
        server1.filters.add(new FilterEntry("netherite chestplate", "", "minecraft:netherite_chestplate", "", -1));
        server1.filters.add(new FilterEntry("netherite leggings", "", "minecraft:netherite_leggings", "", -1));
        server1.filters.add(new FilterEntry("netherite boots", "", "minecraft:netherite_boots", "", -1));
        server1.filters.add(new FilterEntry("golden apple", "", "minecraft:golden_apple", "", -1));
        server1.filters.add(new FilterEntry("enchanted golden apple", "", "minecraft:enchanted_golden_apple", "", -1));
        server1.filters.add(new FilterEntry("ender pearl", "", "minecraft:ender_pearl", "", -1));
        server1.filters.add(new FilterEntry("enchanted book", "", "minecraft:enchanted_book", "", -1));
        server1.filters.add(new FilterEntry("bow", "", "minecraft:bow", "", -1));
        server1.filters.add(new FilterEntry("crossbow", "", "minecraft:crossbow", "", -1));
        server1.filters.add(new FilterEntry("golden helmet", "", "minecraft:golden_helmet", "", -1));
        server1.filters.add(new FilterEntry("fireworks", "", "minecraft:fireworks", "", 4));
        server1.filters.add(new FilterEntry("elytra", "", "minecraft:elytra", "", -1));
        server1.filters.add(new FilterEntry("trident", "", "minecraft:trident", "", -1));
        server1.filters.add(new FilterEntry("shulker box", "", "minecraft:shulker_box", "", -1));
        server1.filters.add(new FilterEntry("snowball", "", "minecraft:snowball", "", 4));
        server1.filters.add(new FilterEntry("red dye", "", "minecraft:red_dye", "", -1));
        server1.filters.add(new FilterEntry("arrow", "", "minecraft:arrow", "", -1));
        server1.filters.add(new FilterEntry("chorus fruit", "", "minecraft:chorus_fruit", "", -1));
        server1.filters.add(new FilterEntry("slime block", "", "minecraft:slime_block", "", 2));
        server1.filters.add(new FilterEntry("obsidian", "", "minecraft:obsidian", "", 2));

        cfg.servers.add(server1);

        ServerEntry server2 = new ServerEntry();
        server2.domains = List.of("kokscraft.pl");
        server2.profileName = "bedwars";
        server2.filters.add(new FilterEntry("netherite sword", "", "minecraft:netherite_sword", "", -1));

        cfg.servers.add(server2);

        return cfg;
    }
}
