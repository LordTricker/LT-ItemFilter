package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.LtifilterClient;

public class ThrowSettingsScreen extends Screen {

    private final Screen parent;
    private PublicSliderWidget throwIntervalSlider;
    private PublicSliderWidget movementDelaySlider;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    public ThrowSettingsScreen(Screen parent) {
        super(Text.literal("Throw Settings"));
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
                Text.literal("Item throw delay: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 200);
                this.setMessage(Text.literal("Item throw delay: " + val));
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
                Text.literal("Delay after movement: "), 0.0) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 200);
                this.setMessage(Text.literal("Delay after movement: " + val));
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

        cancelButton = ButtonWidget.builder(Text.literal("Cancel"), btn -> this.client.setScreen(parent))
                .dimensions(centerX + 5, this.height - btnHeight - 20, btnWidth, btnHeight).build();
        addDrawableChild(cancelButton);

        saveButton = ButtonWidget.builder(Text.literal("Save"), btn -> {
            throwIntervalSlider.forceApplyValue();
            movementDelaySlider.forceApplyValue();
            this.client.setScreen(parent);
        }).dimensions(centerX - btnWidth - 5, this.height - btnHeight - 20, btnWidth, btnHeight).build();
        addDrawableChild(saveButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    protected void drawCenteredText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, Text text, int x, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, x - textWidth / 2, y, color, false);
    }
}