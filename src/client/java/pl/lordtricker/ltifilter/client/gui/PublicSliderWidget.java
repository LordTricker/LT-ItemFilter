package pl.lordtricker.ltifilter.client.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class PublicSliderWidget extends SliderWidget {
    public PublicSliderWidget(int x, int y, int width, int height, Text message, double value) {
        super(x, y, width, height, message, value);
    }

    @Override
    protected void updateMessage() {
        // Ta metoda będzie nadpisywana w anonimowych klasach.
    }

    @Override
    protected void applyValue() {
        // Ta metoda będzie nadpisywana w anonimowych klasach.
    }

    public double getSliderValue() {
        return this.value;
    }

    public void forceApplyValue() {
        this.applyValue();
    }

    public void setSliderValue(double newValue) {
        this.value = newValue;
        this.updateMessage();
    }
}