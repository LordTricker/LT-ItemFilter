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
    private static final String CONFIG_FILE_NAME = "ltitemfilter-config.json";

    public static ServersConfig loadConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            ServersConfig defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            return GSON.fromJson(reader, ServersConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServersConfig();
        }
    }

    public static void saveConfig(ServersConfig config) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
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
        cfg.cleanerSettings.doNotCleanSlots = List.of(12, 15, 20);

        ServerEntry server1 = new ServerEntry();
        server1.domains = List.of("minestar.pl", "anarchia.gg", "tryhc.net", "rapy.pl");
        server1.profileName = "pvp";

        server1.filters.add(new FilterEntry("minecraft:diamond_pickaxe", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_axe", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_sword", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_helmet", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_chestplate", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_leggings", -1));
        server1.filters.add(new FilterEntry("minecraft:diamond_boots", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_pickaxe", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_axe", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_sword", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_helmet", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_chestplate", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_leggings", -1));
        server1.filters.add(new FilterEntry("minecraft:netherite_boots", -1));
        server1.filters.add(new FilterEntry("minecraft:golden_apple", -1));
        server1.filters.add(new FilterEntry("minecraft:enchanted_golden_apple", -1));
        server1.filters.add(new FilterEntry("minecraft:ender_pearl", -1));
        server1.filters.add(new FilterEntry("minecraft:enchanted_book", -1));
        server1.filters.add(new FilterEntry("minecraft:bow", -1));
        server1.filters.add(new FilterEntry("minecraft:crossbow", -1));
        server1.filters.add(new FilterEntry("minecraft:golden_helmet", -1));
        server1.filters.add(new FilterEntry("minecraft:fireworks", 4));
        server1.filters.add(new FilterEntry("minecraft:elytra", -1));
        server1.filters.add(new FilterEntry("minecraft:trident", -1));
        server1.filters.add(new FilterEntry("minecraft:shulker_box", -1));
        server1.filters.add(new FilterEntry("minecraft:snowball", 4));
        server1.filters.add(new FilterEntry("minecraft:red_dye", -1));
        server1.filters.add(new FilterEntry("minecraft:arrow", -1));
        server1.filters.add(new FilterEntry("minecraft:chorus_fruit", -1));
        server1.filters.add(new FilterEntry("minecraft:slime_block", 2));
        server1.filters.add(new FilterEntry("minecraft:obsidian", 2));

        cfg.servers.add(server1);

        ServerEntry server2 = new ServerEntry();
        server2.domains = List.of("kokscraft.pl");
        server2.profileName = "bedwars";
        server2.filters.add(new FilterEntry("minecraft:netherite_sword"));

        cfg.servers.add(server2);

        return cfg;
    }
}
