package pl.lordtricker.ltifilter.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;
import pl.lordtricker.ltifilter.client.config.ConfigLoader;
import pl.lordtricker.ltifilter.client.filter.FilterCommandHandler;
import pl.lordtricker.ltifilter.client.gui.MainSettingsScreen;
import pl.lordtricker.ltifilter.client.keybinding.ToggleFilter;
import pl.lordtricker.ltifilter.client.util.ColorUtils;
import pl.lordtricker.ltifilter.client.util.CompositeKeyUtil;
import pl.lordtricker.ltifilter.client.util.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientCommandRegistration {

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistration::registerLtFilterCommand);
    }

    private static void registerLtFilterCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess
    ) {
        dispatcher.register(
                ClientCommandManager.literal("ltf")
                        // /ltf – podstawowe info
                        .executes(ctx -> {
                            String activeProfile = ClientFilterManager.getActiveProfile();
                            String message = Messages.format("mod.info", Map.of("profile", activeProfile));
                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(message));
                            return 1;
                        })
                        // /ltb settings – otwarcie GUI ustawień
                        .then(ClientCommandManager.literal("settings")
                                .executes(ctx -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    client.setScreen(null);
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        client.execute(() -> client.setScreen(new MainSettingsScreen()));
                                    }).start();
                                    return 1;
                                })
                        )
                        // /ltf filter – toggle
                        .then(ClientCommandManager.literal("filter")
                                .executes(ctx -> {
                                    ToggleFilter.filterEnabled = !ToggleFilter.filterEnabled;
                                    String msgKey = ToggleFilter.filterEnabled
                                            ? "command.filter.toggle.on"
                                            : "command.filter.toggle.off";
                                    String msg = Messages.get(msgKey);
                                    ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                    return 1;
                                })
                        )
                        // /ltf profiles – lista profili
                        .then(ClientCommandManager.literal("profiles")
                                .executes(ctx -> {
                                    String allProfiles = ClientFilterManager.listProfiles();
                                    String[] profiles = allProfiles.split(",\\s*");

                                    String headerStr = Messages.get("command.profiles.header");
                                    MutableText finalText = (MutableText) ColorUtils.translateColorCodes(headerStr);
                                    finalText.append(Text.literal("\n"));

                                    String activeProfile = ClientFilterManager.getActiveProfile();
                                    for (String profile : profiles) {
                                        String trimmedProfile = profile.trim();
                                        String lineTemplate;
                                        if (trimmedProfile.equals(activeProfile)) {
                                            lineTemplate = Messages.format("profile.picked.line", Map.of("profile", trimmedProfile));
                                        } else {
                                            lineTemplate = Messages.format("profile.available.line", Map.of("profile", trimmedProfile));
                                        }
                                        MutableText lineText = (MutableText) ColorUtils.translateColorCodes(lineTemplate);

                                        if (!trimmedProfile.equals(activeProfile)) {
                                            Style clickableStyle = Style.EMPTY
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ltf profile " + trimmedProfile))
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                            Text.literal("Kliknij, aby zmienić profil na " + trimmedProfile)));
                                            lineText.setStyle(clickableStyle);
                                        }

                                        finalText.append(lineText).append(Text.literal("\n"));
                                    }
                                    ctx.getSource().sendFeedback(finalText);
                                    return 1;
                                })
                        )
                        // /ltf profile <nazwa>
                        .then(ClientCommandManager.literal("profile")
                                .then(ClientCommandManager.argument("profile", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String profile = StringArgumentType.getString(ctx, "profile");
                                            ClientFilterManager.setActiveProfile(profile);
                                            String msg = Messages.format("command.profile.change", Map.of("profile", profile));
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                            return 1;
                                        })
                                )
                        )
                        // /ltf add <args>
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            String remaining = builder.getRemaining();
                                            int lastSpace = remaining.lastIndexOf(' ');
                                            String prefixBefore;
                                            String prefix;
                                            if (lastSpace == -1) {
                                                prefixBefore = "";
                                                prefix = remaining;
                                            } else {
                                                prefixBefore = remaining.substring(0, lastSpace + 1);
                                                prefix = remaining.substring(lastSpace + 1);
                                            }
                                            prefix = prefix.toLowerCase();
                                            if (prefix.startsWith("minecraft:")) {
                                                var allItemIds = Registries.ITEM.getIds();
                                                for (var itemId : allItemIds) {
                                                    String asString = itemId.toString();
                                                    if (asString.toLowerCase().startsWith(prefix)) {
                                                        builder.suggest(prefixBefore + asString);
                                                    }
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            var player = ctx.getSource().getPlayer();
                                            String rawArgs = StringArgumentType.getString(ctx, "args");

                                            FilterCommandHandler.CommandResult result = FilterCommandHandler.handleAdd(rawArgs, player);
                                            Map<String, String> placeholders = new HashMap<>();
                                            result.placeholders.forEach((key, value) -> placeholders.put(key, String.valueOf(value)));
                                            String message = Messages.format(result.messageKey, placeholders);
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(message));
                                            return 1;
                                        })
                                )
                        )
                        // /ltf remove <args>
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            String remaining = builder.getRemaining().toLowerCase();
                                            if (remaining.contains("minecraft:")) {
                                                var allItemIds = Registries.ITEM.getIds();
                                                for (var itemId : allItemIds) {
                                                    String asString = itemId.toString();
                                                    if (asString.startsWith(remaining)) {
                                                        builder.suggest(asString);
                                                    }
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            var player = ctx.getSource().getPlayer();
                                            String rawArgs = StringArgumentType.getString(ctx, "args");

                                            FilterCommandHandler.CommandResult result = FilterCommandHandler.handleRemove(rawArgs, player);
                                            Map<String, String> placeholders = new HashMap<>();
                                            result.placeholders.forEach((key, value) -> placeholders.put(key, String.valueOf(value)));
                                            String message = Messages.format(result.messageKey, placeholders);
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(message));
                                            return 1;
                                        })
                                )
                        )
                        // /ltf list – wyświetlanie listy wpisów z przyciskami usuwania i edycji
                        .then(ClientCommandManager.literal("list")
                                .executes(ctx -> {
                                    String activeProfile = ClientFilterManager.getActiveProfile();
                                    List<FilterEntry> items = ClientFilterManager.getItems(activeProfile);

                                    String msgHeader = Messages.format("command.list.header", Map.of("profile", activeProfile));
                                    MutableText header = (MutableText) ColorUtils.translateColorCodes(msgHeader);

                                    MutableText finalText = Text.empty();
                                    for (FilterEntry entry : items) {
                                        String friendlyName = entry.toString();

                                        String editCommand = "/ltf add " + entry.maxCount + " " + entry.baseName;
                                        if (entry.enchants != null && !entry.enchants.isEmpty()) {
                                            editCommand += " {\"" + entry.enchants + "\"}";
                                        }
                                        if (entry.material != null && !entry.material.isEmpty() &&
                                                !entry.baseName.equalsIgnoreCase(entry.material)) {
                                            String displayMaterial = entry.material.toLowerCase().startsWith("minecraft:")
                                                    ? entry.material.substring("minecraft:".length())
                                                    : entry.material;
                                            editCommand += " [\"" + displayMaterial + "\"]";
                                        }
                                        if (entry.lore != null && !entry.lore.isEmpty()) {
                                            editCommand += " (\"" + entry.lore + "\")";
                                        }

                                        MutableText editIcon = (MutableText) ColorUtils.translateColorCodes(Messages.get("list.icon.edit"));
                                        editIcon.setStyle(
                                                Style.EMPTY.withClickEvent(new ClickEvent(
                                                                ClickEvent.Action.SUGGEST_COMMAND, editCommand))
                                                        .withHoverEvent(new HoverEvent(
                                                                HoverEvent.Action.SHOW_TEXT, Text.literal("Kliknij, aby edytować " + friendlyName)))
                                        );
                                        String removeIconStr = Messages.get("list.icon.remove");
                                        String removeIconHover = Messages.get("list.icon.remove.hover");
                                        MutableText removeIcon = (MutableText) ColorUtils.translateColorCodes(removeIconStr);
                                        String removeCommand = "/ltf remove " + CompositeKeyUtil.buildCommand(entry);
                                        removeIcon.setStyle(
                                                Style.EMPTY.withClickEvent(new ClickEvent(
                                                                ClickEvent.Action.RUN_COMMAND, removeCommand))
                                                        .withHoverEvent(new HoverEvent(
                                                                HoverEvent.Action.SHOW_TEXT, Text.literal(removeIconHover + " " + friendlyName)))
                                        );

                                        String itemLineStr = Messages.format("list.item.line", Map.of("item", friendlyName));
                                        MutableText itemLine = (MutableText) ColorUtils.translateColorCodes(itemLineStr);
                                        MutableText lineText = Text.empty()
                                                .append(editIcon).append(Text.literal(" "))
                                                .append(removeIcon).append(Text.literal(" "))
                                                .append(itemLine).append(Text.literal("\n"));
                                        finalText.append(lineText);
                                    }

                                    ctx.getSource().sendFeedback(header);
                                    ctx.getSource().sendFeedback(finalText);
                                    return 1;
                                })
                        )
                        // /ltf pomoc
                        .then(ClientCommandManager.literal("pomoc")
                                .executes(ctx -> {
                                    String msg = Messages.get("command.help");
                                    ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                    return 1;
                                })
                        )
                        // /ltf config save / reload
                        .then(ClientCommandManager.literal("config")
                                .then(ClientCommandManager.literal("save")
                                        .executes(ctx -> {
                                            ClientFilterManager.saveToConfig(LtifilterClient.serversConfig);
                                            ConfigLoader.saveConfig(LtifilterClient.serversConfig);
                                            String msg = Messages.get("command.config.save.success");
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                            return 1;
                                        })
                                )
                                .then(ClientCommandManager.literal("reload")
                                        .executes(ctx -> {
                                            LtifilterClient.serversConfig = ConfigLoader.loadConfig();
                                            ClientFilterManager.reinitProfilesFromConfig(LtifilterClient.serversConfig);
                                            String address = LtifilterClient.getServerAddress();
                                            var entry = LtifilterClient.findServerEntry(address);
                                            if (entry != null) {
                                                ClientFilterManager.setActiveProfile(entry.profileName);
                                            } else {
                                                ClientFilterManager.setActiveProfile(LtifilterClient.serversConfig.defaultProfile);
                                            }
                                            String msg = Messages.get("command.config.reload.success");
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
