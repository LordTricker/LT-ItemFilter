package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.LtifilterClient;

public class ThrowSettingsScreen extends Screen {

    private final Screen parent;
    private PublicSliderWidget throwIntervalSlider;
    private PublicSliderWidget movementDelaySlider;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    public ThrowSettingsScreen(Screen parent) {
        super(Text.of("Throw Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = 40;
        int sliderWidth = 170;
        int sliderHeight = 20;
        int spacing = 25;

        throwIntervalSlider = new PublicSliderWidget(centerX - sliderWidth / 2, startY, sliderWidth, sliderHeight,
                Text.of("Item throw delay: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 200);
                this.setMessage(Text.of("Item throw delay: " + val));
            }
            @Override
            protected void applyValue() {
                int newVal = (int)(this.value * 200);
                LtifilterClient.serversConfig.cleanerSettings.throwIntervalTicks = newVal;
            }
        };
        throwIntervalSlider.setSliderValue(LtifilterClient.serversConfig.cleanerSettings.throwIntervalTicks / 200.0);
        addDrawableChild(throwIntervalSlider);

        movementDelaySlider = new PublicSliderWidget(centerX - sliderWidth / 2, startY + spacing, sliderWidth, sliderHeight,
                Text.of("Delay after movement: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 200);
                this.setMessage(Text.of("Delay after movement: " + val));
            }
            @Override
            protected void applyValue() {
                int newVal = (int)(this.value * 200);
                LtifilterClient.serversConfig.cleanerSettings.movementDelayTicks = newVal;
            }
        };
        movementDelaySlider.setSliderValue(LtifilterClient.serversConfig.cleanerSettings.movementDelayTicks / 200.0);
        addDrawableChild(movementDelaySlider);

        int btnWidth = 80;
        int btnHeight = 20;

        cancelButton = new ButtonWidget(
                centerX + 5,
                this.height - btnHeight - 20,
                btnWidth,
                btnHeight,
                Text.of("Cancel"),
                button -> this.client.setScreen(parent)
        );
        addDrawableChild(cancelButton);

        saveButton = new ButtonWidget(
                centerX - btnWidth - 5,
                this.height - btnHeight - 20,
                btnWidth,
                btnHeight,
                Text.of("Save"),
                button -> {
                    throwIntervalSlider.forceApplyValue();
                    movementDelaySlider.forceApplyValue();
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
        super.render(matrices, mouseX, mouseY, delta);
        renderCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}