package pl.lordtricker.ltifilter.client.cleaner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.screen.slot.SlotActionType;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;

public class InventoryCleaner {

    private static int tickCounter = 0;
    private static int currentSlot = 9;

    private static boolean throwBlocked = false;
    private static long blockEndTime = 0;
    private static final long BLOCK_DURATION_MS = 200;

    /**
     * Metoda wywoływana np. z ToggleFilter, która co 2 ticki próbuje wyrzucić jeden przedmiot
     * z głównej części EQ (sloty 9-35), który nie znajduje się na liście dozwolonych.
     */
    public static void cleanInventory(MinecraftClient client) {
        if (client.player == null) return;

        if (throwBlocked && System.currentTimeMillis() < blockEndTime) {
            return;
        } else {
            throwBlocked = false;
        }

        tickCounter++;
        if (tickCounter % 2 != 0) return;

        var player = client.player;
        var inventory = player.getInventory();
        var itemsToKeep = ClientFilterManager.getItems(ClientFilterManager.getActiveProfile());

        int firstThrowSlot = 9;
        int lastThrowSlot = 35;
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

    /**
     * Metoda wywoływana przy zmianie slotu hotbara – ustawia tymczasową blokadę wyrzucania.
     */
    public static void onHotbarSlotChanged() {
        throwBlocked = true;
        blockEndTime = System.currentTimeMillis() + BLOCK_DURATION_MS;
    }
}
