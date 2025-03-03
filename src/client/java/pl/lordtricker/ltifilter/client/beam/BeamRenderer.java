package pl.lordtricker.ltifilter.client.beam;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

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
        float beamAlpha = 0.7f;      // alfa (0-1)
        float beamHeight = 0.65f;     // wysokość słupa
        float radius = 0.04f;      // promień beamu

        // Kolor – jasno niebieski
        float red = 0.5f, green = 0.8f, blue = 1.0f;

        stack.push();
        // Przesuń beam wyżej – tutaj dodajemy przesunięcie w górę o 1 jednostkę, dostosuj według potrzeb
        stack.translate(0.0, 0.55, 0.0);

        long currentTime = System.currentTimeMillis();
        float rotationDegrees = ((currentTime % 10000) / 10000.0f) * 360.0f;
        rotationDegrees += pticks;

        // Obrót wokół osi Y
        stack.multiply(new Quaternion(0.0F, rotationDegrees, 0.0F, true));

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
     * Dodaje wierzchołek do bufora. Kończy wywołaniem next().
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
                .normal(entry.getNormalMatrix(), 0.0F, 1.0F, 0.0F)
                .next();
    }

    /**
     * Tworzy niestandardowy RenderLayer dla beamu.
     */
    private static RenderLayer createBeamLayer() {
        return RenderLayer.getEntityTranslucent(BEAM_TEXTURE, false);
    }
}
