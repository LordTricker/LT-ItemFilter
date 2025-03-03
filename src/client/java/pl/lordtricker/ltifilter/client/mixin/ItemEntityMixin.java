package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.lordtricker.ltifilter.client.beam.ItemStackEntityRenderBeamState;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements ItemStackEntityRenderBeamState {

    @Unique
    private boolean shouldRenderBeam = false;

    @Override
    public boolean getShouldRenderBeam() {
        return shouldRenderBeam;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        shouldRenderBeam = ClientFilterManager.shouldRenderItemBeam(stack);
    }
}