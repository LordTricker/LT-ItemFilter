package pl.lordtricker.ltifilter.client.cleaner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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
     * Metoda czyszcząca ekwipunek. Dla każdego slotu (domyślnie 9-35, poza slotami wykluczonymi z configu):
     *
     * 1. Zbiera, w których slotach znajduje się każdy przedmiot (mapa itemId -> lista slotów).
     * 2. Usuwa w całości przedmioty spoza filtra.
     * 3. Dla przedmiotów w filtrze z maxCount > 0 usuwa nadmiarowe sloty (jeśli liczba slotów z danym itemem > maxCount).
     *
     * Gdy throwIntervalTicks > 0, usuwa tylko jeden slot na wywołanie (co X ticków).
     *
     * Dodatkowo – jeśli gracz się porusza, wyrzucanie zostaje odroczone o 20 ticków.
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
        }
        else if (movementDelayTicks > 0) {
            movementDelayTicks--;
            return;
        }

        List<FilterEntry> allowedEntries = ClientFilterManager.getItems(ClientFilterManager.getActiveProfile());
        Map<String, FilterEntry> allowedMap = new HashMap<>();
        for (FilterEntry fe : allowedEntries) {
            allowedMap.put(fe.material.toLowerCase(), fe);
        }

        int firstSlot = 9;
        int lastSlot = 35;

        Set<Integer> excludedSlots = new HashSet<>();
        if (cleanerSettings.doNotCleanSlots != null) {
            excludedSlots.addAll(cleanerSettings.doNotCleanSlots);
        }

        Map<String, List<Integer>> cleaningSlots = new HashMap<>();
        for (int slot = firstSlot; slot <= lastSlot; slot++) {
            if (excludedSlots.contains(slot)) continue;
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();
                cleaningSlots.computeIfAbsent(id, k -> new ArrayList<>()).add(slot);
            }
        }

        if (cleanerSettings.throwIntervalTicks <= 0) {
            for (Map.Entry<String, List<Integer>> entry : cleaningSlots.entrySet()) {
                String itemId = entry.getKey();
                List<Integer> slots = entry.getValue();
                if (!allowedMap.containsKey(itemId)) {
                    for (int slot : slots) {
                        removeItemStack(client, player, slot);
                    }
                }
            }

            for (Map.Entry<String, FilterEntry> entry : allowedMap.entrySet()) {
                String itemId = entry.getKey();
                FilterEntry fe = entry.getValue();
                if (fe.maxCount > 0) {
                    List<Integer> slots = cleaningSlots.get(itemId);
                    if (slots != null && slots.size() > fe.maxCount) {
                        for (int i = fe.maxCount; i < slots.size(); i++) {
                            removeItemStack(client, player, slots.get(i));
                        }
                    }
                }
            }

        } else {
            tickCounter++;
            if (tickCounter % cleanerSettings.throwIntervalTicks != 0) {
                return;
            }

            int throwableSlotsCount = lastSlot - firstSlot + 1;
            if (currentSlot < firstSlot || currentSlot > lastSlot) {
                currentSlot = firstSlot;
            }

            Map<String, Integer> usedSlotsCount = new HashMap<>();

            for (int j = 0; j < throwableSlotsCount; j++) {
                int slot = firstSlot + ((currentSlot - firstSlot + j) % throwableSlotsCount);
                if (excludedSlots.contains(slot)) continue;

                ItemStack stack = inventory.getStack(slot);
                if (!stack.isEmpty()) {
                    String id = Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();
                    FilterEntry fe = allowedMap.get(id);

                    if (fe == null) {
                        removeItemStack(client, player, slot);
                        currentSlot = slot + 1;
                        if (currentSlot > lastSlot) currentSlot = firstSlot;
                        break;
                    }
                    if (fe.maxCount > 0) {
                        int used = usedSlotsCount.getOrDefault(id, 0);
                        if (used >= fe.maxCount) {
                            removeItemStack(client, player, slot);
                            currentSlot = slot + 1;
                            if (currentSlot > lastSlot) currentSlot = firstSlot;
                            break;
                        } else {
                            usedSlotsCount.put(id, used + 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Metoda wywoływana przy zmianie slotu hotbara – ustawia tymczasową blokadę wyrzucania.
     */
    public static void onHotbarSlotChanged() {
        CleanerSettings cleanerSettings = LtifilterClient.serversConfig.cleanerSettings;
        throwBlocked = true;
        blockEndTime = System.currentTimeMillis() + cleanerSettings.blockDurationMs;
    }

    /**
     * Usuwa (wyrzuca) cały stack z ekwipunku (dany slot).
     */
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
