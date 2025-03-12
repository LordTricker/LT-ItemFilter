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
    private static int movementDelayTicks = 0;

    /**
     * Metoda czyszcząca ekwipunek:
     * - Dla każdego slotu (9-35, poza wykluczonymi) sprawdzamy, czy przedmiot pasuje do któregoś filtra.
     * - Jeśli nie pasuje do żadnego, usuwamy go.
     * - Dla każdego filtra, jeśli liczba przedmiotów przekracza dozwoloną liczbę (maxCount > 0),
     *   usuwamy dodatkowe sloty.
     * - Jeśli gracz się porusza, odraczamy czyszczenie.
     */
    public static void cleanInventory(MinecraftClient client) {
        if (client.player == null) return;
        CleanerSettings cleanerSettings = LtifilterClient.serversConfig.cleanerSettings;
        if (throwBlocked && System.currentTimeMillis() < blockEndTime) {
            return;
        } else {
            throwBlocked = false;
        }
        PlayerEntity player = client.player;
        var inventory = player.getInventory();

        if (client.options.forwardKey.isPressed() || client.options.backKey.isPressed() ||
                client.options.leftKey.isPressed() || client.options.rightKey.isPressed()) {
            movementDelayTicks = 10;
            return;
        } else if (movementDelayTicks > 0) {
            movementDelayTicks--;
            return;
        }

        List<FilterEntry> filters = ClientFilterManager.getItems(ClientFilterManager.getActiveProfile());
        Map<FilterEntry, List<Integer>> filterMatches = new HashMap<>();
        List<Integer> unmatchedSlots = new ArrayList<>();

        int firstSlot = 9;
        int lastSlot = 35;
        Set<Integer> excludedSlots = new HashSet<>();
        if (cleanerSettings.doNotCleanSlots != null) {
            excludedSlots.addAll(cleanerSettings.doNotCleanSlots);
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

        for (int slot : unmatchedSlots) {
            removeItemStack(client, player, slot);
        }

        for (Map.Entry<FilterEntry, List<Integer>> entry : filterMatches.entrySet()) {
            FilterEntry filter = entry.getKey();
            List<Integer> slots = entry.getValue();
            if (filter.maxCount > 0 && slots.size() > filter.maxCount) {
                slots.sort(Integer::compareTo);
                for (int i = filter.maxCount; i < slots.size(); i++) {
                    removeItemStack(client, player, slots.get(i));
                }
            }
        }
    }

    public static void onHotbarSlotChanged() {
        CleanerSettings cleanerSettings = LtifilterClient.serversConfig.cleanerSettings;
        throwBlocked = true;
        blockEndTime = System.currentTimeMillis() + cleanerSettings.blockDurationMs;
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