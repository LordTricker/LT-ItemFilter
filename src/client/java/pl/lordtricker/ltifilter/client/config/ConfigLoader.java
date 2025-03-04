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
            ServersConfig config = GSON.fromJson(reader, ServersConfig.class);
            return config;
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

        // Ustawienia beama
        cfg.beamSettings.hexColor = "#80ccff";
        cfg.beamSettings.alpha = 0.7f;
        cfg.beamSettings.height = 0.65f;
        cfg.beamSettings.radius = 0.04f;
        cfg.beamSettings.verticalOffset = 0.55f;

        // Ustawienia czyszczenia ekwipunku (cleanera)
        cfg.cleanerSettings.throwIntervalTicks = 2;
        cfg.cleanerSettings.blockDurationMs = 200;
        // cfg.cleanerSettings.pickupSound = "minecraft:entity.experience_orb.pickup";
        // cfg.cleanerSettings.pickupSoundDelayTicks = 2;

        // Przykładowe wpisy serwerowe
        ServerEntry server1 = new ServerEntry();
        server1.domains = List.of("minestar.pl", "anarchia.gg", "tryhc.net", "rapy.pl");
        server1.profileName = "pvp";

        server1.filters.add(new FilterEntry("minecraft:diamond_pickaxe"));
        server1.filters.add(new FilterEntry("minecraft:diamond_axe"));
        server1.filters.add(new FilterEntry("minecraft:diamond_sword"));
        server1.filters.add(new FilterEntry("minecraft:diamond_helmet"));
        server1.filters.add(new FilterEntry("minecraft:diamond_chestplate"));
        server1.filters.add(new FilterEntry("minecraft:diamond_leggings"));
        server1.filters.add(new FilterEntry("minecraft:diamond_boots"));
        server1.filters.add(new FilterEntry("minecraft:netherite_pickaxe"));
        server1.filters.add(new FilterEntry("minecraft:netherite_axe"));
        server1.filters.add(new FilterEntry("minecraft:netherite_sword"));
        server1.filters.add(new FilterEntry("minecraft:netherite_helmet"));
        server1.filters.add(new FilterEntry("minecraft:netherite_chestplate"));
        server1.filters.add(new FilterEntry("minecraft:netherite_leggings"));
        server1.filters.add(new FilterEntry("minecraft:netherite_boots"));
        server1.filters.add(new FilterEntry("minecraft:golden_apple"));
        server1.filters.add(new FilterEntry("minecraft:enchanted_golden_apple"));
        server1.filters.add(new FilterEntry("minecraft:ender_pearl"));
        server1.filters.add(new FilterEntry("minecraft:enchanted_book"));
        server1.filters.add(new FilterEntry("minecraft:bow"));
        server1.filters.add(new FilterEntry("minecraft:crossbow"));
        server1.filters.add(new FilterEntry("minecraft:golden_helmet"));
        server1.filters.add(new FilterEntry("minecraft:fireworks"));
        server1.filters.add(new FilterEntry("minecraft:elytra"));
        server1.filters.add(new FilterEntry("minecraft:trident"));
        server1.filters.add(new FilterEntry("minecraft:shulker_box"));
        server1.filters.add(new FilterEntry("minecraft:snowball"));
        server1.filters.add(new FilterEntry("minecraft:red_dye"));
        server1.filters.add(new FilterEntry("minecraft:arrow"));
        server1.filters.add(new FilterEntry("minecraft:chorus_fruit"));
        server1.filters.add(new FilterEntry("minecraft:slime_block"));

        cfg.servers.add(server1);

        ServerEntry server2 = new ServerEntry();
        server2.domains = List.of("kokscraft.pl");
        server2.profileName = "bedwars";

        server2.filters.add(new FilterEntry("minecraft:netherite_sword"));

        cfg.servers.add(server2);

        return cfg;
    }
}