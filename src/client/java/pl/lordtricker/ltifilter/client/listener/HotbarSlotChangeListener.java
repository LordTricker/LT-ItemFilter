package pl.lordtricker.ltifilter.client.listener;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import pl.lordtricker.ltifilter.client.cleaner.InventoryCleaner;

public class HotbarSlotChangeListener {

    private static int lastSelectedSlot = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                int currentSlot = client.player.getInventory().selectedSlot;
                // Jeżeli zmiana slotu i nie jest to pierwszy tick, ustaw blokadę
                if (lastSelectedSlot != -1 && currentSlot != lastSelectedSlot) {
                    InventoryCleaner.onHotbarSlotChanged();
                }
                lastSelectedSlot = currentSlot;
            }
        });
    }
}
