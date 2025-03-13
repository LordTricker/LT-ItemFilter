package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.BeamSettings;

public class BeamSettingsScreen extends Screen {

    private final Screen parent;
    private PublicSliderWidget heightSlider;
    private PublicSliderWidget radiusSlider;
    private PublicSliderWidget verticalOffsetSlider;
    private PublicSliderWidget alphaSlider;
    private PublicSliderWidget redSlider;
    private PublicSliderWidget greenSlider;
    private PublicSliderWidget blueSlider;
    private ButtonWidget cancelButton;
    private ButtonWidget saveButton;

    public BeamSettingsScreen(Screen parent) {
        super(Text.literal("Beam Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BeamSettings beam = LtifilterClient.serversConfig.beamSettings;

        int centerX = this.width / 2;
        int startY = 40;
        int sliderWidth = 170;
        int sliderHeight = 20;
        int spacing = 25;
        int extraMargin = 10;

        heightSlider = new PublicSliderWidget(centerX - sliderWidth / 2, startY, sliderWidth, sliderHeight,
                Text.literal("Height: "), 0.0) {
            @Override
            protected void updateMessage() {
                double val = this.value * 2.0;
                this.setMessage(Text.literal("Height: " + String.format("%.2f", val)));
            }
            @Override
            protected void applyValue() {
                beam.height = (float)(this.value * 2.0);
            }
        };
        heightSlider.setSliderValue(beam.height / 2.0);
        addDrawableChild(heightSlider);

        radiusSlider = new PublicSliderWidget(centerX - sliderWidth / 2, startY + spacing, sliderWidth, sliderHeight,
                Text.literal("Radius: "), 0.0) {
            @Override
            protected void updateMessage() {
                double val = this.value * 0.2;
                this.setMessage(Text.literal("Radius: " + String.format("%.2f", val)));
            }
            @Override
            protected void applyValue() {
                beam.radius = (float)(this.value * 0.2);
            }
        };
        radiusSlider.setSliderValue(beam.radius / 0.2);
        addDrawableChild(radiusSlider);

        verticalOffsetSlider = new PublicSliderWidget(centerX - sliderWidth / 2, startY + 2 * spacing, sliderWidth, sliderHeight,
                Text.literal("Vertical Offset: "), 0.0) {
            @Override
            protected void updateMessage() {
                double val = (this.value * 2.0) - 1.0;
                this.setMessage(Text.literal("Vertical Offset: " + String.format("%.2f", val)));
            }
            @Override
            protected void applyValue() {
                beam.verticalOffset = (float)((this.value * 2.0) - 1.0);
            }
        };
        verticalOffsetSlider.setSliderValue((beam.verticalOffset + 1.0) / 2.0);
        addDrawableChild(verticalOffsetSlider);

        int group2StartY = startY + 3 * spacing + extraMargin;
        alphaSlider = new PublicSliderWidget(centerX - sliderWidth / 2, group2StartY, sliderWidth, sliderHeight,
                Text.literal("Alpha: "), 0.0) {
            @Override
            protected void updateMessage() {
                double val = this.value;
                int rgb = ((beam.red & 0xFF) << 16) | ((beam.green & 0xFF) << 8) | (beam.blue & 0xFF);
                this.setMessage(Text.literal("Alpha: " + String.format("%.2f", val))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
            }
            @Override
            protected void applyValue() {
                beam.alpha = (float)this.value;
            }
        };
        alphaSlider.setSliderValue(beam.alpha);
        addDrawableChild(alphaSlider);

        redSlider = new PublicSliderWidget(centerX - sliderWidth / 2, group2StartY + spacing, sliderWidth, sliderHeight,
                Text.literal("Red: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 255);
                this.setMessage(Text.literal("Red: " + val));
            }
            @Override
            protected void applyValue() {
                beam.red = (int)(this.value * 255);
            }
        };
        redSlider.setSliderValue((double) beam.red / 255.0);
        addDrawableChild(redSlider);

        greenSlider = new PublicSliderWidget(centerX - sliderWidth / 2, group2StartY + 2 * spacing, sliderWidth, sliderHeight,
                Text.literal("Green: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 255);
                this.setMessage(Text.literal("Green: " + val));
            }
            @Override
            protected void applyValue() {
                beam.green = (int)(this.value * 255);
            }
        };
        greenSlider.setSliderValue((double) beam.green / 255.0);
        addDrawableChild(greenSlider);

        blueSlider = new PublicSliderWidget(centerX - sliderWidth / 2, group2StartY + 3 * spacing, sliderWidth, sliderHeight,
                Text.literal("Blue: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 255);
                this.setMessage(Text.literal("Blue: " + val));
            }
            @Override
            protected void applyValue() {
                beam.blue = (int)(this.value * 255);
            }
        };
        blueSlider.setSliderValue((double) beam.blue / 255.0);
        addDrawableChild(blueSlider);

        int btnWidth = 80;
        int btnHeight = 20;
        cancelButton = new ButtonWidget(
                centerX - btnWidth - 5,
                this.height - btnHeight - 20,
                btnWidth,
                btnHeight,
                Text.literal("Cancel"),
                button -> this.client.setScreen(parent)
        );
        addDrawableChild(cancelButton);

        saveButton = new ButtonWidget(
                centerX + 5,
                this.height - btnHeight - 20,
                btnWidth,
                btnHeight,
                Text.literal("Save"),
                button -> {
                    alphaSlider.forceApplyValue();
                    heightSlider.forceApplyValue();
                    radiusSlider.forceApplyValue();
                    verticalOffsetSlider.forceApplyValue();
                    redSlider.forceApplyValue();
                    greenSlider.forceApplyValue();
                    blueSlider.forceApplyValue();
                    beam.hexColor = String.format("#%02X%02X%02X", beam.red, beam.green, beam.blue);
                    this.client.setScreen(parent);
                }
        );
        addDrawableChild(saveButton);
    }

    protected void renderCenteredText(MatrixStack matrices, net.minecraft.client.font.TextRenderer textRenderer, Text text, int x, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        textRenderer.draw(matrices, text, x - textWidth / 2, y, color);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int rgb = ((LtifilterClient.serversConfig.beamSettings.red & 0xFF) << 16)
                | ((LtifilterClient.serversConfig.beamSettings.green & 0xFF) << 8)
                | (LtifilterClient.serversConfig.beamSettings.blue & 0xFF);
        double alphaVal = alphaSlider.getSliderValue(); // wartość alpha w zakresie 0-1
        alphaSlider.setMessage(
                Text.literal("Alpha: " + String.format("%.2f", alphaVal))
                        .setStyle(alphaSlider.getMessage().getStyle().withColor(net.minecraft.text.TextColor.fromRgb(rgb)))
        );

        super.render(matrices, mouseX, mouseY, delta);

        renderCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}