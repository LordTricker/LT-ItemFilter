package pl.lordtricker.ltifilter.client.cleaner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.CleanerSettings;
import pl.lordtricker.ltifilter.client.config.FilterEntry;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;

import java.util.*;

public class InventoryCleaner {

    private static int tickCounter = 0;
    private static int currentSlot = 9;
    private static boolean throwBlocked = false;
    private static long blockEndTime = 0;

    private static int movementDelayCounter = 0;

    /**
     * Metoda czyszcząca ekwipunek.
     * - Jeśli ustawiony throwIntervalTicks > 0, usuwamy tylko jeden przedmiot co throwIntervalTicks ticków.
     *   Jeśli ustawiony na 0, usuwamy wszystko jednocześnie.
     * - Delay ruchu jest obsługiwany przez lokalny licznik, który jest resetowany do wartości z konfiguracji,
     *   ale nie zmienia samej konfiguracji.
     */
    public static void cleanInventory(MinecraftClient client) {
        if (client.player == null) return;
        CleanerSettings settings = LtifilterClient.serversConfig.cleanerSettings;

        if (throwBlocked && System.currentTimeMillis() < blockEndTime) {
            return;
        } else {
            throwBlocked = false;
        }

        PlayerEntity player = client.player;
        var inventory = player.getInventory();

        if (client.options.forwardKey.isPressed() || client.options.backKey.isPressed() ||
                client.options.leftKey.isPressed() || client.options.rightKey.isPressed()) {
            movementDelayCounter = settings.movementDelayTicks;
            return;
        } else if (movementDelayCounter > 0) {
            movementDelayCounter--;
            return;
        }

        if (settings.throwIntervalTicks > 0) {
            tickCounter++;
            if (tickCounter % settings.throwIntervalTicks != 0) {
                return;
            }
        }
        List<FilterEntry> filters = ClientFilterManager.getItems(ClientFilterManager.getActiveProfile());
        Map<FilterEntry, List<Integer>> filterMatches = new HashMap<>();
        List<Integer> unmatchedSlots = new ArrayList<>();

        int firstSlot = 9;
        int lastSlot = 35;
        Set<Integer> excludedSlots = new HashSet<>();
        if (settings.doNotCleanSlots != null) {
            excludedSlots.addAll(settings.doNotCleanSlots);
        }

        for (int slot = firstSlot; slot <= lastSlot; slot++) {
            if (excludedSlots.contains(slot)) continue;
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) continue;
            boolean matched = false;
            for (FilterEntry filter : filters) {
                if (ClientFilterManager.matchesFilter(filter, stack)) {
                    filterMatches.computeIfAbsent(filter, k -> new ArrayList<>()).add(slot);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                unmatchedSlots.add(slot);
            }
        }

        if (!unmatchedSlots.isEmpty()) {
            if (settings.throwIntervalTicks == 0) {
                for (int slot : unmatchedSlots) {
                    removeItemStack(client, player, slot);
                }
            } else {
                removeItemStack(client, player, unmatchedSlots.get(0));
            }
            return;
        }

        for (Map.Entry<FilterEntry, List<Integer>> entry : filterMatches.entrySet()) {
            FilterEntry filter = entry.getKey();
            List<Integer> slots = entry.getValue();
            if (filter.maxCount > 0 && slots.size() > filter.maxCount) {
                slots.sort(Integer::compareTo);
                if (settings.throwIntervalTicks == 0) {
                    for (int i = filter.maxCount; i < slots.size(); i++) {
                        removeItemStack(client, player, slots.get(i));
                    }
                } else {
                    removeItemStack(client, player, slots.get(filter.maxCount));
                }
                return;
            }
        }
    }

    private static void removeItemStack(MinecraftClient client, PlayerEntity player, int slot) {
        var inventory = player.getInventory();
        ItemStack stack = inventory.getStack(slot);
        if (stack.isEmpty()) return;
        if (player.currentScreenHandler != null && client.interactionManager != null) {
            client.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    slot,
                    1,
                    SlotActionType.THROW,
                    player
            );
        } else {
            player.dropItem(stack.copy(), true, false);
        }
        inventory.setStack(slot, ItemStack.EMPTY);
        if (player.currentScreenHandler != null) {
            player.currentScreenHandler.onContentChanged(inventory);
        }
    }
}
