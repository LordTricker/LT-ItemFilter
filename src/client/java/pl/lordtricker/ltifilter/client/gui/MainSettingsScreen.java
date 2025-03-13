package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import pl.lordtricker.ltifilter.client.LtifilterClient;
import pl.lordtricker.ltifilter.client.config.ConfigLoader;

public class MainSettingsScreen extends Screen {

    private ButtonWidget throwSettingsButton;
    private ButtonWidget beamSettingsButton;
    private ButtonWidget slotSettingsButton;
    private ButtonWidget saveButton;

    public MainSettingsScreen() {
        super(Text.of("LT-ItemFilter Settings"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int btnWidth = 170;
        int btnWidthS = 170;
        int btnHeight = 20;
        int spacing = 5;

        ButtonWidget throwSettingsButton = new ButtonWidget(
                centerX - btnWidth / 2,
                startY,
                btnWidth,
                btnHeight,
                Text.of("Throw items settings"),
                button -> this.client.setScreen(new ThrowSettingsScreen(this))
        );
        addDrawableChild(throwSettingsButton);

        ButtonWidget beamSettingsButton = new ButtonWidget(
                centerX - btnWidth / 2,
                startY + btnHeight + spacing,
                btnWidth,
                btnHeight,
                Text.of("Beam render settings"),
                button -> this.client.setScreen(new BeamSettingsScreen(this))
        );
        addDrawableChild(beamSettingsButton);

        ButtonWidget slotSettingsButton = new ButtonWidget(
                centerX - btnWidth / 2,
                startY + 2 * (btnHeight + spacing),
                btnWidth,
                btnHeight,
                Text.of("Slots settings"),
                button -> {
                    LtifilterClient.slotSettingsActive = true;
                    this.client.setScreen(new SlotSettingsInventoryScreen());
                }
        );
        addDrawableChild(slotSettingsButton);

        ButtonWidget saveButton = new ButtonWidget(
                centerX - (btnWidthS / 2),
                this.height - btnHeight - 20,
                btnWidthS,
                btnHeight,
                Text.of("Save and close"),
                button -> {
                    ConfigLoader.saveConfig(LtifilterClient.serversConfig);
                    this.client.setScreen(null);
                }
        );
        addDrawableChild(saveButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        renderCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    protected void renderCenteredText(MatrixStack matrices, net.minecraft.client.font.TextRenderer textRenderer, Text text, int x, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        textRenderer.draw(matrices, text, x - textWidth / 2, y, color);
    }
}