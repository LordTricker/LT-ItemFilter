package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.ConfigLoader;

public class MainSettingsScreen extends Screen {

    private ButtonWidget throwSettingsButton;
    private ButtonWidget beamSettingsButton;
    private ButtonWidget slotSettingsButton;
    private ButtonWidget saveButton;

    public MainSettingsScreen() {
        super(Text.literal("LT-ItemFilter Settings"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int btnWidth = 170;
        int btnWidthS = 170;
        int btnHeight = 20;
        int spacing = 5;

        throwSettingsButton = ButtonWidget.builder(
                Text.literal("Throw items settings"),
                btn -> {
                    this.client.setScreen(new ThrowSettingsScreen(this));
                }
        ).dimensions(centerX - btnWidth/2, startY, btnWidth, btnHeight).build();
        addDrawableChild(throwSettingsButton);

        beamSettingsButton = ButtonWidget.builder(
                Text.literal("Beam render settings"),
                btn -> {
                    this.client.setScreen(new BeamSettingsScreen(this));
                }
        ).dimensions(centerX - btnWidth/2, startY + btnHeight + spacing, btnWidth, btnHeight).build();
        addDrawableChild(beamSettingsButton);

        slotSettingsButton = ButtonWidget.builder(
                Text.literal("Slots settings"),
                btn -> {
                    LtifilterClient.slotSettingsActive = true;
                    this.client.setScreen(new SlotSettingsInventoryScreen());
                }
        ).dimensions(centerX - btnWidth/2, startY + 2*(btnHeight + spacing), btnWidth, btnHeight).build();
        addDrawableChild(slotSettingsButton);

        saveButton = ButtonWidget.builder(
                Text.literal("Save and close"),
                btn -> {
                    ConfigLoader.saveConfig(LtifilterClient.serversConfig);
                    this.client.setScreen(null);
                }
        ).dimensions(centerX - (btnWidthS / 2), this.height - btnHeight - 20, btnWidthS, btnHeight).build();
        addDrawableChild(saveButton);
    }

    protected void drawCenteredText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, Text text, int x, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, x - textWidth / 2, y, color, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
