package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.CleanerSettings;
@Mixin(HandledScreen.class)
public abstract class SlotOverlayMixin {

    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!LtifilterClient.slotSettingsActive) return;

        CleanerSettings settings = LtifilterClient.serversConfig.cleanerSettings;
        DefaultedList<Slot> slots = ((ScreenHandlerAccessor)((HandledScreen<?>)(Object)this).getScreenHandler()).getSlots();
        if (slots == null) return;
        for (Slot slot : slots) {
            if (slot.id < 9 || slot.id > 35) continue;
            int color = settings.doNotCleanSlots.contains(slot.id) ? 0x8000FF00 : 0x80FF0000;
            int realX = this.x + slot.x;
            int realY = this.y + slot.y;
            DrawableHelper.fill(matrices, realX, realY, realX + 16, realY + 16, color);
        }
    }
}
