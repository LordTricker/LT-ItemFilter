package pl.lordtricker.ltifilter.client.cleaner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.registry.Registry;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.CleanerSettings;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;

public class InventoryCleaner {

    private static int tickCounter = 0;
    private static int currentSlot = 9;
    private static boolean throwBlocked = false;
    private static long blockEndTime = 0;

    /**
     * Metoda wywoływana np. z ToggleFilter, która usuwa przedmioty z głównej części ekwipunku (sloty 9-35),
     * które nie znajdują się na liście dozwolonych.
     * Jeśli w konfiguracji throwIntervalTicks jest ustawione na 0, usuwa wszystkie niepożądane przedmioty
     * od razu bez opóźnienia. W przeciwnym razie, usuwany jest jeden przedmiot na określoną liczbę ticków.
     */
    public static void cleanInventory(MinecraftClient client) {
        if (client.player == null) return;

        CleanerSettings cleanerSettings = LtifilterClient.serversConfig.cleanerSettings;

        if (throwBlocked && System.currentTimeMillis() < blockEndTime) {
            return;
        } else {
            throwBlocked = false;
        }

        var player = client.player;
        var inventory = player.getInventory();
        var itemsToKeep = ClientFilterManager.getItems(ClientFilterManager.getActiveProfile());

        int firstThrowSlot = 9;
        int lastThrowSlot = 35;

        if (cleanerSettings.throwIntervalTicks <= 0) {
            for (int slot = firstThrowSlot; slot <= lastThrowSlot; slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (!stack.isEmpty()) {
                    String id = Registry.ITEM.getId(stack.getItem()).toString();
                    if (!itemsToKeep.contains(id)) {
                        if (player.currentScreenHandler != null) {
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
            }
        } else {
            // Standardowa logika usuwania jednego przedmiotu na określoną liczbę ticków
            tickCounter++;
            if (tickCounter % cleanerSettings.throwIntervalTicks != 0) return;

            int throwableSlotsCount = lastThrowSlot - firstThrowSlot + 1;
            if (currentSlot < firstThrowSlot || currentSlot > lastThrowSlot) {
                currentSlot = firstThrowSlot;
            }

            for (int j = 0; j < throwableSlotsCount; j++) {
                int slot = firstThrowSlot + ((currentSlot - firstThrowSlot + j) % throwableSlotsCount);
                ItemStack stack = inventory.getStack(slot);
                if (!stack.isEmpty()) {
                    String id = Registry.ITEM.getId(stack.getItem()).toString();
                    if (itemsToKeep.contains(id)) {
                        continue;
                    }

                    if (player.currentScreenHandler != null) {
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

                    currentSlot = slot + 1;
                    if (currentSlot > lastThrowSlot) {
                        currentSlot = firstThrowSlot;
                    }
                    break;
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
}