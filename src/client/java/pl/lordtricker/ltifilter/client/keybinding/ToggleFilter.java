package pl.lordtricker.ltifilter.client.keybinding;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import pl.lordtricker.ltifilter.client.cleaner.InventoryCleaner;
import pl.lordtricker.ltifilter.client.util.ColorUtils;
import pl.lordtricker.ltifilter.client.util.Messages;

public class ToggleFilter {
    public static KeyBinding toggleFilterKey;
    public static boolean filterEnabled = false;

    public static void init() {
        toggleFilterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Włączenie filtracji itemów",
                GLFW.GLFW_KEY_G,
                "LT-Mods binds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Obsługa wciśnięcia klawisza (CTRL+G)
            while (toggleFilterKey.wasPressed()) {
                long window = client.getWindow().getHandle();
                boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
                if (!ctrlPressed) {
                    continue;
                }
                filterEnabled = !filterEnabled;
                String msgKey = filterEnabled ? "command.filter.toggle.on" : "command.filter.toggle.off";
                String msg = Messages.get(msgKey);
                if (client.player != null) {
                    client.player.sendMessage(ColorUtils.translateColorCodes(msg), false);
                }
            }

            if (filterEnabled && client.player != null) {
                InventoryCleaner.cleanInventory(client);
            }
        });
    }
}
