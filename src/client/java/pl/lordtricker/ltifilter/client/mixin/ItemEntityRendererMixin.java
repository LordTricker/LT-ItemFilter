package pl.lordtricker.ltifilter.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.lordtricker.ltifilter.client.beam.BeamRenderer;
import pl.lordtricker.ltifilter.client.beam.ItemStackEntityRenderBeamState;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(ItemEntity itemEntity, float yaw, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null || !MinecraftClient.getInstance().player.isSneaking()) {
            return;
        }

        double distSq = MinecraftClient.getInstance().player.squaredDistanceTo(
                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ()
        );
        double maxDist = 24.0;
        if (distSq <= maxDist * maxDist && itemEntity instanceof ItemStackEntityRenderBeamState beamState && beamState.getShouldRenderBeam()) {
            long time = 0;
            BeamRenderer.renderBeam(matrices, vertexConsumers, yaw, time);
        }
    }
}