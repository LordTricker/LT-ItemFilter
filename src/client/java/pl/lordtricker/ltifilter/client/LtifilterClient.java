package pl.lordtricker.ltifilter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import pl.lordtricker.ltifilter.client.command.ClientCommandRegistration;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;
import pl.lordtricker.ltifilter.client.config.ConfigLoader;
import pl.lordtricker.ltifilter.client.config.ServerEntry;
import pl.lordtricker.ltifilter.client.config.ServersConfig;
import pl.lordtricker.ltifilter.client.keybinding.ToggleFilter;
import pl.lordtricker.ltifilter.client.util.ColorUtils;
import pl.lordtricker.ltifilter.client.util.Messages;

import java.util.Map;

public class LtifilterClient implements ClientModInitializer {
	public static ServersConfig serversConfig;
	public static boolean slotSettingsActive = false;

	@Override
	public void onInitializeClient() {
		ToggleFilter.init();

		serversConfig = ConfigLoader.loadConfig();
		ClientFilterManager.loadFromConfig(serversConfig);
		ClientFilterManager.setActiveProfile(serversConfig.defaultProfile);


		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String address = getServerAddress();
			ServerEntry entry = findServerEntry(address);
			if (entry != null) {
				ClientFilterManager.setActiveProfile(entry.profileName);
				if (client.player != null) {
					String welcomeMsg = Messages.format("player.join",
							Map.of("profile", entry.profileName));
					client.player.sendMessage(ColorUtils.translateColorCodes(welcomeMsg), false);
				}
			} else {
				String def = serversConfig.defaultProfile;
				ClientFilterManager.setActiveProfile(def);
				if (client.player != null) {
					String welcomeMsg = Messages.format("player.join", Map.of("profile", def));
					client.player.sendMessage(ColorUtils.translateColorCodes(welcomeMsg), false);
				}
			}
		});

		ClientCommandRegistration.registerCommands();
	}

	public static String getServerAddress() {
		if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			return MinecraftClient.getInstance().getCurrentServerEntry().address;
		}
		return "singleplayer";
	}

	public static ServerEntry findServerEntry(String address) {
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