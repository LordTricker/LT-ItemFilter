package pl.lordtricker.ltifilter.client.beam;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public abstract class BeamRenderer extends RenderLayer {
    private static final Identifier BEAM_TEXTURE = Identifier.tryParse("ltifilter:textures/loot_beam.png");
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
        float beamAlpha = 1f;   // stała wartość opacity
        float beamHeight = 0.95f;  // wysokość słupa
        float radius = 0.05f;     // promień beamu

        // Kolor – jasno niebieski
        float red = 0.5f, green = 0.8f, blue = 1.0f;

        stack.push();
        stack.translate(0, 0, 0);
        float rotation = (worldTime % 60) + pticks;
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation * 3.0f));
        VertexConsumer consumer = buffer.getBuffer(BEAM_LAYER);

        renderSide(stack, consumer, -radius, -radius,  radius, -radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer,  radius, -radius,  radius,  radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer,  radius,  radius, -radius,  radius, red, green, blue, beamAlpha, beamHeight);
        renderSide(stack, consumer, -radius,  radius, -radius, -radius, red, green, blue, beamAlpha, beamHeight);

        stack.pop();
    }


    /**
     * Rysuje jedną pionową ściankę (quad) beamu od y=0 do y=beamHeight.
     * Parametry (x1, z1) i (x2, z2) określają dolną krawędź.
     */
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


    /**
     * Dodaje wierzchołek do bufora. Używamy metody vertex(...) i na końcu wywołujemy next().
     */
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

    /**
     * Tworzy niestandardowy RenderLayer dla beamu.
     */
    private static RenderLayer createBeamLayer() {
        RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(BEAM_TEXTURE, TriState.FALSE, false))
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
