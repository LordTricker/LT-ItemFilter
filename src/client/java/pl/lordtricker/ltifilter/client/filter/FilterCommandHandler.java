package pl.lordtricker.ltifilter.client.filter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa pomocnicza do obsługi logiki komend /ltr add ... i /ltr remove ...
 * Zwraca obiekt CommandResult, który zawiera:
 *   - klucz wiadomości z messages.json
 *   - mapę placeholderów (np. {item}, {profile}, {count}, {quantity}, itp.)
 */
public class FilterCommandHandler {

    /**
     * Obiekt, który pozwala zwrócić:
     *  - klucz wiadomości z pliku messages.json,
     *  - placeholdery do formatowania.
     */
    public static class CommandResult {
        public String messageKey;
        public Map<String, Object> placeholders = new HashMap<>();

        public CommandResult(String key) {
            this.messageKey = key;
        }
        public CommandResult(String key, Map<String, Object> placeholders) {
            this.messageKey = key;
            if (placeholders != null) {
                this.placeholders.putAll(placeholders);
            }
        }
    }

    /**
     * Obsługa komendy /ltr add <...>
     *
     * @param rawArgs  – np. "hand", "eq", "64 hand", "minecraft:obsidian", itp.
     * @param player   – gracz, który wywołał komendę
     * @return CommandResult zawierający klucz wiadomości oraz placeholdery.
     */
    public static CommandResult handleAdd(String rawArgs, PlayerEntity player) {
        if (player == null) {
            return new CommandResult("command.error.playerOnly");
        }

        String[] split = rawArgs.trim().split("\\s+");
        int maxCount = -1;
        String target = null;

        if (split.length == 1) {
            target = split[0];
        } else if (split.length >= 2) {
            try {
                maxCount = Integer.parseInt(split[0]);
                target = split[1];
            } catch (NumberFormatException e) {
                return new CommandResult("command.add.syntaxError");
            }
        }

        if (target == null) {
            return new CommandResult("command.add.syntaxError");
        }

        String activeProfile = ClientFilterManager.getActiveProfile();

        if ("hand".equalsIgnoreCase(target)) {
            ItemStack handStack = player.getMainHandStack();
            if (handStack.isEmpty()) {
                return new CommandResult("command.add.hand.empty");
            }
            String itemId = Registries.ITEM.getId(handStack.getItem()).toString();
            ClientFilterManager.addItem(itemId, maxCount);

            if (maxCount > -1) {
                return new CommandResult(
                        "command.add.hand.quantity.success",
                        Map.of("item", itemId, "quantity", maxCount, "profile", activeProfile)
                );
            } else {
                return new CommandResult(
                        "command.add.hand.noQuantity.success",
                        Map.of("item", itemId, "profile", activeProfile)
                );
            }

        } else if ("eq".equalsIgnoreCase(target)) {
            int countAdded = 0;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    ClientFilterManager.addItem(itemId, maxCount);
                    countAdded++;
                }
            }
            if (maxCount > -1) {
                return new CommandResult(
                        "command.add.eq.quantity.success",
                        Map.of("count", countAdded, "quantity", maxCount, "profile", activeProfile)
                );
            } else {
                return new CommandResult(
                        "command.add.eq.noQuantity.success",
                        Map.of("count", countAdded, "profile", activeProfile)
                );
            }

        } else {
            ClientFilterManager.addItem(target, maxCount);

            if (maxCount > -1) {
                return new CommandResult(
                        "command.add.quantity.success",
                        Map.of("item", target, "quantity", maxCount, "profile", activeProfile)
                );
            } else {
                return new CommandResult(
                        "command.add.noQuantity.success",
                        Map.of("item", target, "profile", activeProfile)
                );
            }
        }
    }

    /**
     * Obsługa komendy /ltr remove <...>
     * Przykładowo: "hand", "eq", "minecraft:obsidian"
     */
    public static CommandResult handleRemove(String rawArgs, PlayerEntity player) {
        if (player == null) {
            return new CommandResult("command.error.playerOnly");
        }

        String[] split = rawArgs.trim().split("\\s+");
        if (split.length == 0) {
            return new CommandResult("command.remove.syntaxError");
        }
        String target = split[0];
        String activeProfile = ClientFilterManager.getActiveProfile();

        if ("hand".equalsIgnoreCase(target)) {
            ItemStack handStack = player.getMainHandStack();
            if (handStack.isEmpty()) {
                return new CommandResult("command.remove.hand.empty");
            }
            String itemId = Registries.ITEM.getId(handStack.getItem()).toString();
            ClientFilterManager.removeItem(itemId);

            return new CommandResult(
                    "command.remove.success",
                    Map.of("item", itemId, "profile", activeProfile)
            );

        } else if ("eq".equalsIgnoreCase(target)) {
            int countRemoved = 0;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    if (ClientFilterManager.hasItem(activeProfile, itemId)) {
                        ClientFilterManager.removeItem(itemId);
                        countRemoved++;
                    }
                }
            }
            return new CommandResult(
                    "command.remove.eq.success",
                    Map.of("count", countRemoved, "profile", activeProfile)
            );

        } else {
            ClientFilterManager.removeItem(target);
            return new CommandResult(
                    "command.remove.success",
                    Map.of("item", target, "profile", activeProfile)
            );
        }
    }
}
