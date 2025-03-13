package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import pl.lordtricker.ltifilter.client.LtifilterClient;

public class SlotSettingsInventoryScreen extends InventoryScreen {

    public SlotSettingsInventoryScreen() {
        super(MinecraftClient.getInstance().player);
    }

    @Override
    public void removed() {
        super.removed();
        LtifilterClient.slotSettingsActive = false;
        System.out.println("[DEBUG] SlotSettingsInventoryScreen removed – slotSettingsActive set to false");
    }
}