package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.lordtricker.ltifilter.client.beam.ItemStackEntityRenderBeamState;
import pl.lordtricker.ltifilter.client.filter.ClientFilterManager;

@Mixin(ItemStackEntityRenderState.class)
public class ItemStackEntityRenderStateMixin implements ItemStackEntityRenderBeamState {

    @Unique
    public boolean shouldRenderBeam = false;

    @Inject(at = @At("HEAD"), method = "update")
    public void update(Entity entity, ItemStack stack, ItemModelManager itemModelManager, CallbackInfo ci) {
        this.shouldRenderBeam = ClientFilterManager.shouldRenderItemBeam(stack);
        System.out.println("update shouldRenderBeam: " + shouldRenderBeam + " item: " + stack);
    }

    @Override
    public boolean getShouldRenderBeam() {
        return shouldRenderBeam;
    }
}

