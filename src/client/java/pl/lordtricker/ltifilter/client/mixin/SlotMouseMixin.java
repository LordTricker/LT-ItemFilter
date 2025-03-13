package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.CleanerSettings;

@Mixin(HandledScreen.class)
public abstract class SlotMouseMixin {

    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!LtifilterClient.slotSettingsActive) return;
        if (button == 2) {
            HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
            if (screen.getScreenHandler() == null) return;
            DefaultedList<Slot> slots = ((ScreenHandlerAccessor) screen.getScreenHandler()).getSlots();
            CleanerSettings settings = LtifilterClient.serversConfig.cleanerSettings;
            if (slots == null) return;
            for (Slot slot : slots) {
                int realX = this.x + slot.x;
                int realY = this.y + slot.y;
                if (mouseX >= realX && mouseX < realX + 16 &&
                        mouseY >= realY && mouseY < realY + 16) {
                    if (settings.doNotCleanSlots.contains(slot.id)) {
                        settings.doNotCleanSlots.remove(Integer.valueOf(slot.id));
                    } else {
                        settings.doNotCleanSlots.add(slot.id);
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}