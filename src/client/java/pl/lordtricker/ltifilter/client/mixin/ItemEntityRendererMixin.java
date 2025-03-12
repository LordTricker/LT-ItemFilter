package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.lordtricker.ltifilter.client.beam.BeamRenderer;
import pl.lordtricker.ltifilter.client.beam.ItemStackEntityRenderBeamState;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void onRenderEntity(
            ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci
    ) {
        // Sprawdź, czy gracz istnieje oraz czy przytrzymuje shift
        if (MinecraftClient.getInstance().player == null || !MinecraftClient.getInstance().player.isSneaking()) {
            return;
        }

        if (!state.itemRenderState.isEmpty() || MinecraftClient.getInstance().player != null) {
            double distSq = MinecraftClient.getInstance().player.squaredDistanceTo(state.x, state.y, state.z);
            double maxDist = 24.0;
            if (distSq <= maxDist * maxDist
                    && state instanceof ItemStackEntityRenderBeamState beamState
                    && beamState.getShouldRenderBeam()) {
                long time = 0;
                BeamRenderer.renderBeam(matrices, vertexConsumers, state.uniqueOffset, time);
            }
        }
    }
}