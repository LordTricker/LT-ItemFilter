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
        Path configFile = MOD_CONFIG_DIR.resolve(MAIN_CONFIG_FILE_NAME);

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
        Path configFile = MOD_CONFIG_DIR.resolve(MAIN_CONFIG_FILE_NAME);

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

        cfg.cleanerSettings.throwIntervalTicks = 2;
        cfg.cleanerSettings.movementDelayTicks = 20;
        cfg.cleanerSettings.doNotCleanSlots = List.of(1);

        ServerEntry server1 = new ServerEntry();
        server1.domains = List.of("minestar.pl", "tryhc.net", "rapy.pl");
        server1.profileName = "pvp";

        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_pickaxe", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_axe", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_sword", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_helmet", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_chestplate", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_leggings", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:diamond_boots", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_pickaxe", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_axe", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_sword", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_helmet", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_chestplate", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_leggings", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:netherite_boots", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:golden_apple", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:enchanted_golden_apple", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:ender_pearl", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:enchanted_book", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:bow", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:crossbow", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:golden_helmet", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:fireworks", "", 4));
        server1.filters.add(new FilterEntry("", "", "minecraft:elytra", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:trident", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:shulker_box", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:snowball", "", 4));
        server1.filters.add(new FilterEntry("", "", "minecraft:red_dye", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:arrow", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:chorus_fruit", "", -1));
        server1.filters.add(new FilterEntry("", "", "minecraft:slime_block", "", 2));
        server1.filters.add(new FilterEntry("", "", "minecraft:obsidian", "", 2));

        cfg.servers.add(server1);


        ServerEntry server2 = new ServerEntry();
        server2.domains = List.of("anarchia.gg");
        server2.profileName = "anarchia";

        server2.filters.add(new FilterEntry("anarchiczne serce", "to serce możesz użyć poza limitem 30 serc.", "red_dye", "", -1));
        server2.filters.add(new FilterEntry("dodatkowe serce", "Kliknij PRAWYM, aby", "red_dye", "", -1));
        server2.filters.add(new FilterEntry("spleśniała kanapka", "po uderzeniu przeciwnika zarazisz go chorobą", "poisonous_potato", "", -1));
        server2.filters.add(new FilterEntry("parawan", "po użyciu tego cudownego przedmiotu odpycha wszystkich przeciwników w okolicy!", "feather", "", -1));
        server2.filters.add(new FilterEntry("piernik", "po zjedzeniu piernika otrzymujesz", "cookie", "", -1));
        server2.filters.add(new FilterEntry("wampirze jabłko", "po zjedzeniu otrzymujesz na krótki czas efekt siły", "enchanted_golden_apple", "", -1));
        server2.filters.add(new FilterEntry("ciepłe mleko", "po wypiciu usuwa wszystkie negatywne efekty", "bowl", "", -1));
        server2.filters.add(new FilterEntry("lewe jajko", "dzięki niemu możesz wyrzucić przeciwnika", "egg", "", -1));
        server2.filters.add(new FilterEntry("śnieżka", "po trafieniu zamieniasz się", "snowball", "", -1));
        server2.filters.add(new FilterEntry("krew wampira", "po kliknięciu uleczy cie", "beetroot_soup", "", -1));
        server2.filters.add(new FilterEntry("kostka Rubika", "uderz gracza, aby jego ekwipunek został przemieszany", "player_head", "", -1));
        server2.filters.add(new FilterEntry("rozgotowana kukurydza", "wystrzeliwuje magiczny pocisk", "blaze_rod", "", -1));
        server2.filters.add(new FilterEntry("łopata grincha", "zostaje on ogłuszony przez co rotacja zostaje wylosowna", "diamond_shovel", "", -1));
        server2.filters.add(new FilterEntry("marchewkowa kusza", "ta niezwykła kusza zapewnia ci zdumiewającą zdolność przyciągania", "crossbow", "", -1));
        server2.filters.add(new FilterEntry("arcus magnus", "łuk, który pozwala na niszczycielskie combo", "bow", "", -1));
        server2.filters.add(new FilterEntry("kosa", "twoje uderzenie może co minute przestraszyć przeciwnika", "netherite_hoe", "", -1));
        server2.filters.add(new FilterEntry("zatruty ołówek", "po uderzeniu przeciwnika otrzymuje on trujący efekt", "candle{Color:5}", "", -1));
        server2.filters.add(new FilterEntry("boski topór", "aktywujesz potężna falę uderzeniową", "iron_axe", "", -1));
        server2.filters.add(new FilterEntry("łuk kupidyna", "że trafiony przez ciebie gracz zostanie oślepiony", "bow", "", -1));
        server2.filters.add(new FilterEntry("lizak", "trzymaj go w ręce aby otrzymać:", "allium", "", -1));
        server2.filters.add(new FilterEntry("rózga", "odrzuca z potężna siła niegrzecznych graczy", "stick", "", -1));
        server2.filters.add(new FilterEntry("marchewkowy miecz", "dzięki niemu możesz zamrozić przeciwnika", "golden_sword", "", -1));
        server2.filters.add(new FilterEntry("króliczy miecz", "po uderzeniu przeciwnika blokuje możliwość skakania na 4 sekundy", "netherite_sword", "", -1));
        server2.filters.add(new FilterEntry("siekiera grincha", "po uderzeniu strzela piorun w przeciwnika zabierając mu 30% jego życia", "golden_axe", "", -1));
        server2.filters.add(new FilterEntry("wędka surferka", "niezwykła wędka zapewnia ci zdumiewającą zdolność", "fishing_rod", "", -1));
        server2.filters.add(new FilterEntry("wędka nielotka", "po złapaniu gracza na haczyk nie może on odlecieć", "fishing_rod", "", -1));
        server2.filters.add(new FilterEntry("excalibur", "zapełnienie paska zapewnia ci 12 punktów obrażeń", "netherite_sword", "", -1));
        server2.filters.add(new FilterEntry("anarchiczny hełm", "", "netherite_helmet", "prot 5", -1));
        server2.filters.add(new FilterEntry("anarchiczna klata", "", "netherite_chestplate", "prot 5", -1));
        server2.filters.add(new FilterEntry("anarchiczne spodnie", "", "netherite_leggings", "prot 5", -1));
        server2.filters.add(new FilterEntry("anarchiczne buty", "", "netherite_boots", "prot 5", -1));
        server2.filters.add(new FilterEntry("anarchiczny hełm II", "", "netherite_helmet", "prot 6", -1));
        server2.filters.add(new FilterEntry("anarchiczna klata II", "", "netherite_chestplate", "prot 6", -1));
        server2.filters.add(new FilterEntry("anarchiczne spodnie II", "", "netherite_leggings", "prot 6", -1));
        server2.filters.add(new FilterEntry("anarchiczne buty II", "", "netherite_boots", "prot 6", -1));
        server2.filters.add(new FilterEntry("anarchiczny miecz", "", "netherite_sword", "sharp 6", -1));
        server2.filters.add(new FilterEntry("", "", "minecraft:golden_apple", "", 3));
        server2.filters.add(new FilterEntry("", "", "minecraft:enchanted_golden_apple", "", 3));
        server2.filters.add(new FilterEntry("", "", "minecraft:ender_pearl", "", 3));
        server2.filters.add(new FilterEntry("", "", "minecraft:crossbow", "", -1));
        server2.filters.add(new FilterEntry("", "", "minecraft:elytra", "", -1));
        server2.filters.add(new FilterEntry("", "", "minecraft:trident", "", -1));
        server2.filters.add(new FilterEntry("", "", "minecraft:shulker_box", "", -1));
        server2.filters.add(new FilterEntry("", "", "minecraft:obsidian", "", 2));
        server2.filters.add(new FilterEntry("", "", "minecraft:water_bucket", "", 2));
        server2.filters.add(new FilterEntry("", "", "minecraft:arrow", "", 1));

        cfg.servers.add(server2);

        return cfg;
    }
}