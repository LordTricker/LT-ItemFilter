package pl.lordtricker.ltifilter.client.beam;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.BeamSettings;

public abstract class BeamRenderer extends RenderLayer {
    private static final Identifier BEAM_TEXTURE = Identifier.tryParse("ltifilter:textures/lt-beam.png");
    private static final RenderLayer BEAM_LAYER = createBeamLayer();

    protected BeamRenderer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode mode, int expectedBufferSize,
                           boolean translucent, boolean writeMask, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, mode, expectedBufferSize, translucent, writeMask, startAction, endAction);
    }

    /**
     * Renderuje pionowy beam nad przedmiotem.
     * @param stack MatrixStack przekazywany z metody renderowania.
     * @param buffer VertexConsumerProvider.
     * @param pticks Partial ticks dla animacji.
     * @param worldTime Aktualny czas świata.
     */
    public static void renderBeam(MatrixStack stack, VertexConsumerProvider buffer, float pticks, long worldTime) {
        BeamSettings settings = LtifilterClient.serversConfig.beamSettings;
        float[] rgb = hexToRgb(settings.hexColor);
        float red = rgb[0], green = rgb[1], blue = rgb[2];
        float beamAlpha = settings.alpha;
        float beamHeight = settings.height;
        float radius = settings.radius;
        float verticalOffset = settings.verticalOffset;

        stack.push();
        // Podniesienie beama o zadaną wartość offsetu
        stack.translate(0, verticalOffset, 0);
        long currentTime = System.currentTimeMillis();
        float rotationDegrees = ((currentTime % 10000) / 10000.0f) * 360.0f;
        rotationDegrees += pticks;
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));

        VertexConsumer consumer = buffer.getBuffer(BEAM_LAYER);
        renderSide(stack, consumer, -radius, -radius,  radius, -radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer,  radius, -radius,  radius,  radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer,  radius,  radius, -radius,  radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer, -radius,  radius, -radius, -radius, red, green, blue, beamAlpha, beamHeight);
        stack.pop();
    }

    /**
     * Konwertuje wartość hex (np. "#80ccff") na tablicę float z wartościami RGB (w zakresie 0.0-1.0).
     */
    private static float[] hexToRgb(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int color = Integer.parseInt(hex, 16);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    private static void renderSide(MatrixStack stack, VertexConsumer consumer,
                                   float x1, float z1, float x2, float z2,
                                   float r, float g, float b, float alpha,
                                   float height) {
        MatrixStack.Entry entry = stack.peek();
        Matrix4f pose = entry.getPositionMatrix();
        addVertex(consumer, entry, pose, x1, 0, z1, r, g, b, alpha, 0f, 0f);
        addVertex(consumer, entry, pose, x2, 0, z2, r, g, b, alpha, 1f, 0f);
        addVertex(consumer, entry, pose, x2, height, z2, r, g, b, alpha, 1f, 1f);
        addVertex(consumer, entry, pose, x1, height, z1, r, g, b, alpha, 0f, 1f);
    }

    private static void addVertex(VertexConsumer consumer, MatrixStack.Entry entry, Matrix4f pose,
                                  float x, float y, float z,
                                  float r, float g, float b, float a,
                                  float u, float v) {
        consumer.vertex(pose, x, y, z)
                .color(r, g, b, a)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(15728880)
                .normal(entry, 0.0F, 1.0F, 0.0F);
    }

    private static RenderLayer createBeamLayer() {
        MultiPhaseParameters params = MultiPhaseParameters.builder()
                .texture(new Texture(BEAM_TEXTURE, false, false))
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                .program(RenderLayer.TRANSLUCENT_PROGRAM)
                .overlay(RenderPhase.DISABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                .writeMaskState(RenderLayer.COLOR_MASK)
                .build(false);
        try {
            return RenderLayer.of(
                    "ltfilter_beam",
                    VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS,
                    256,
                    false,
                    true,
                    params
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RenderLayer.getEntityTranslucent(BEAM_TEXTURE, false);
    }
}
