package pl.lordtricker.ltifilter.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.util.registry.Registry;
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
import pl.lordtricker.ltifilter.client.keybinding.ToggleFilter;
import pl.lordtricker.ltifilter.client.util.ColorUtils;
import pl.lordtricker.ltifilter.client.util.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientCommandRegistration {

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("ltf")
                        // /ltf -> podstawowe info
                        .executes(ctx -> {
                            String activeProfile = ClientFilterManager.getActiveProfile();
                            // Wyświetlamy np. "Aktualny profil: <profile>"
                            String message = Messages.format("mod.info", Map.of("profile", activeProfile));
                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(message));
                            return 1;
                        })
                        // /ltf filter -> toggle
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
                        // /ltf profiles -> lista profili, aktywny profil podświetlony kolorem &b (AQUA)
                        .then(ClientCommandManager.literal("profiles")
                                .executes(ctx -> {
                                    String allProfiles = ClientFilterManager.listProfiles();
                                    String[] profiles = allProfiles.split(",\\s*");

                                    String headerStr = Messages.get("command.profiles.header");
                                    MutableText finalText = (MutableText) ColorUtils.translateColorCodes(headerStr);
                                    finalText.append(Text.of("\n"));

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
                                                            Text.of("Kliknij, aby zmienić profil na " + trimmedProfile)));
                                            lineText.setStyle(clickableStyle);
                                        }

                                        finalText.append(lineText).append(Text.of("\n"));
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
                                            String msg = Messages.format("command.profile.change",
                                                    Map.of("profile", profile));
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                            return 1;
                                        })
                                )
                        )
                        // /ltf add <args> z podpowiedziami
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            String remaining = builder.getRemaining();
                                            // Znajdź ostatnią spację by oddzielić część liczbową od itemId
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
                                            // Podpowiadamy tylko, gdy ostatni token zaczyna się od "minecraft:"
                                            if (prefix.startsWith("minecraft:")) {
                                                var allItemIds = Registry.ITEM.getIds();
                                                for (var itemId : allItemIds) {
                                                    String asString = itemId.toString();
                                                    if (asString.toLowerCase().startsWith(prefix)) {
                                                        // Sugestia zawiera część liczbową oraz spację, a następnie pełną nazwę itemu
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
                                                var allItemIds = Registry.ITEM.getIds();
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
                        // /ltf list
                        .then(ClientCommandManager.literal("list")
                                .executes(ctx -> {
                                    String activeProfile = ClientFilterManager.getActiveProfile();
                                    List<FilterEntry> items = ClientFilterManager.getItems(activeProfile);

                                    String msgHeader = Messages.format("command.list.header", Map.of("profile", activeProfile));
                                    MutableText header = (MutableText) ColorUtils.translateColorCodes(msgHeader);

                                    MutableText finalText = (MutableText) Text.of("");
                                    for (FilterEntry item : items) {
                                        String removeIconStr = Messages.get("list.icon.remove");
                                        String removeIconHover = Messages.get("list.icon.remove.hover");
                                        MutableText removeIcon = (MutableText) ColorUtils.translateColorCodes(removeIconStr);
                                        removeIcon.setStyle(
                                                Style.EMPTY.withClickEvent(new ClickEvent(
                                                                ClickEvent.Action.RUN_COMMAND, "/ltf remove " + item))
                                                        .withHoverEvent(new HoverEvent(
                                                                HoverEvent.Action.SHOW_TEXT, Text.of(removeIconHover + item)))
                                        );

                                        String itemLineStr = Messages.format("list.item.line", Map.of("item", item.toString()));
                                        MutableText itemLine = (MutableText) ColorUtils.translateColorCodes(itemLineStr);

                                        MutableText lineText = ((MutableText) Text.of(""))
                                                .append(removeIcon).append(Text.of(" "))
                                                .append(itemLine).append(Text.of("\n"));
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
                                            // Zapis do configu
                                            ClientFilterManager.saveToConfig(LtifilterClient.serversConfig);
                                            ConfigLoader.saveConfig(LtifilterClient.serversConfig);
                                            String msg = Messages.get("command.config.save.success");
                                            ctx.getSource().sendFeedback(ColorUtils.translateColorCodes(msg));
                                            return 1;
                                        })
                                )
                                .then(ClientCommandManager.literal("reload")
                                        .executes(ctx -> {
                                            // Przeładuj config
                                            LtifilterClient.serversConfig = ConfigLoader.loadConfig();
                                            ClientFilterManager.reinitProfilesFromConfig(LtifilterClient.serversConfig);

                                            // Po przeładowaniu sprawdzamy serwer
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