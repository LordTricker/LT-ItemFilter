package pl.lordtricker.ltifilter.client.filter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.util.CompositeKeyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterCommandHandler {

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

    private static final Pattern NEWER_PATTERN = Pattern.compile(
            "ResourceKey\\[\\s*minecraft:enchantment\\s*/\\s*minecraft:([^\\]]+)\\]\\s*=.*?=>\\s*(\\d+)"
    );
    private static final Pattern OLDER_PATTERN = Pattern.compile(
            "\\{id:\"([^\"]+)\",lvl:(\\d+)s\\}"
    );

    public static CommandResult handleAdd(String rawArgs, PlayerEntity player) {
        if (player == null) {
            return new CommandResult("command.error.playerOnly");
        }

        // Nowa logika parsowania: jeśli pierwszy token nie jest liczbą, traktujemy cały ciąg jako target i maxCount = -1.
        String[] split = rawArgs.trim().split("\\s+", 2);
        int maxCount = -1;
        String target = "";
        if (split.length == 1) {
            target = split[0];
        } else {
            try {
                maxCount = Integer.parseInt(split[0]);
                target = split[1];
            } catch (NumberFormatException e) {
                // Pierwszy token nie jest liczbą – traktujemy cały ciąg jako target, ustawiamy unlimited
                target = rawArgs.trim();
                maxCount = -1;
            }
        }

        if (target.isEmpty()) {
            return new CommandResult("command.add.syntaxError");
        }

        String activeProfile = ClientFilterManager.getActiveProfile();

        if ("hand".equalsIgnoreCase(target)) {
            ItemStack handStack = player.getMainHandStack();
            if (handStack.isEmpty()) {
                return new CommandResult("command.add.hand.empty");
            }
            String materialId = Registries.ITEM.getId(handStack.getItem()).toString();

            String customName;
            if (handStack.getName() != null) {
                customName = handStack.getName().getString();
            } else {
                customName = materialId;
            }

            String rawEnchants = handStack.getEnchantments().toString();
            StringBuilder enchantBuilder = new StringBuilder();
            boolean foundAny = false;
            Pattern newPattern = Pattern.compile("ResourceKey\\[\\s*minecraft:enchantment\\s*/\\s*minecraft:([^\\]]+)\\]\\s*=.*?=>\\s*(\\d+)");
            Matcher matcherNew = newPattern.matcher(rawEnchants);
            while (matcherNew.find()) {
                foundAny = true;
                String enchId = matcherNew.group(1).trim();
                String levelStr = matcherNew.group(2).trim();
                String shortEnchant = enchId + levelStr;
                String mappedEnchant = pl.lordtricker.ltifilter.client.util.EnchantMapper.mapEnchant(shortEnchant, true);
                if (enchantBuilder.length() > 0) {
                    enchantBuilder.append(",");
                }
                enchantBuilder.append(mappedEnchant);
            }
            if (!foundAny) {
                Pattern oldPattern = Pattern.compile("\\{id:\"([^\"]+)\",lvl:(\\d+)s\\}");
                Matcher matcherOld = oldPattern.matcher(rawEnchants);
                while (matcherOld.find()) {
                    String enchId = matcherOld.group(1).trim();
                    String levelStr = matcherOld.group(2).trim();
                    if (enchId.startsWith("minecraft:")) {
                        enchId = enchId.substring("minecraft:".length());
                    }
                    String shortEnchant = enchId + levelStr;
                    String mappedEnchant = pl.lordtricker.ltifilter.client.util.EnchantMapper.mapEnchant(shortEnchant, false);
                    if (enchantBuilder.length() > 0) {
                        enchantBuilder.append(",");
                    }
                    enchantBuilder.append(mappedEnchant);
                }
            }
            String enchantmentsString = enchantBuilder.toString();

            String baseNameToUse = customName;
            if (customName.equalsIgnoreCase(materialId)) {
                baseNameToUse = materialId;
            }

            FilterEntry entry = new FilterEntry(baseNameToUse, "", materialId, enchantmentsString, maxCount);
            ClientFilterManager.addItem(entry);

            if (maxCount > -1) {
                return new CommandResult(
                        "command.add.hand.quantity.success",
                        Map.of("item", entry.toString(), "quantity", maxCount, "profile", ClientFilterManager.getActiveProfile())
                );
            } else {
                return new CommandResult(
                        "command.add.hand.noQuantity.success",
                        Map.of("item", entry.toString(), "profile", ClientFilterManager.getActiveProfile())
                );
            }
        } else if ("eq".equalsIgnoreCase(target)) {
            Map<String, Integer> slotCounts = new HashMap<>();
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String materialId = Registries.ITEM.getId(stack.getItem()).toString();
                    FilterEntry entry = new FilterEntry(materialId, "", materialId, "", -1);
                    String key = entry.baseName + "|" + entry.lore + "|" + entry.material + "|" + entry.enchants;
                    slotCounts.put(key, slotCounts.getOrDefault(key, 0) + 1);
                }
            }
            int countAdded = 0;
            for (Map.Entry<String, Integer> entrySet : slotCounts.entrySet()) {
                String composite = entrySet.getKey();
                int count = entrySet.getValue();
                String[] parts = composite.split("\\|", -1);
                FilterEntry entry = new FilterEntry(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : "", count);
                ClientFilterManager.addItem(entry);
                countAdded++;
            }
            return new CommandResult(
                    "command.add.eq.count.success",
                    Map.of("count", countAdded, "profile", activeProfile)
            );
        } else {
            FilterEntry entry = CompositeKeyUtil.parseFilterEntry(target, maxCount);
            ClientFilterManager.addItem(entry);

            if (maxCount > -1) {
                return new CommandResult(
                        "command.add.quantity.success",
                        Map.of("item", entry.toString(), "quantity", maxCount, "profile", activeProfile)
                );
            } else {
                return new CommandResult(
                        "command.add.noQuantity.success",
                        Map.of("item", entry.toString(), "profile", activeProfile)
                );
            }
        }
    }

    public static CommandResult handleRemove(String rawArgs, PlayerEntity player) {
        if (player == null) {
            return new CommandResult("command.error.playerOnly");
        }

        String target = rawArgs.trim();
        String activeProfile = ClientFilterManager.getActiveProfile();

        if ("hand".equalsIgnoreCase(target)) {
            ItemStack handStack = player.getMainHandStack();
            if (handStack.isEmpty()) {
                return new CommandResult("command.remove.hand.empty");
            }
            String materialId = Registries.ITEM.getId(handStack.getItem()).toString();
            FilterEntry entry = new FilterEntry(materialId, "", materialId, "", -1);
            ClientFilterManager.removeItem(entry);

            return new CommandResult(
                    "command.remove.success",
                    Map.of("item", entry.toString(), "profile", activeProfile)
            );
        } else if ("eq".equalsIgnoreCase(target)) {
            java.util.Set<String> distinctKeys = new java.util.HashSet<>();
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String materialId = Registries.ITEM.getId(stack.getItem()).toString();
                    FilterEntry entry = new FilterEntry(materialId, "", materialId, "", -1);
                    String key = entry.baseName + "|" + entry.lore + "|" + entry.material + "|" + entry.enchants;
                    distinctKeys.add(key);
                }
            }
            int countRemoved = 0;
            for (String composite : distinctKeys) {
                String[] parts = composite.split("\\|", -1);
                FilterEntry entry = new FilterEntry(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : "", -1);
                if (ClientFilterManager.hasItem(activeProfile, entry)) {
                    ClientFilterManager.removeItem(entry);
                    countRemoved++;
                }
            }
            return new CommandResult(
                    "command.remove.eq.success",
                    Map.of("count", countRemoved, "profile", activeProfile)
            );
        } else {
            FilterEntry entry = CompositeKeyUtil.parseFilterEntry(target, -1);
            ClientFilterManager.removeItem(entry);
            return new CommandResult(
                    "command.remove.success",
                    Map.of("item", entry.toString(), "profile", activeProfile)
            );
        }
    }
}
