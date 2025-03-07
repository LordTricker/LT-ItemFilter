package pl.lordtricker.ltifilter.client.config;

public class FilterEntry {
    public String material;
    public int maxCount = -1;

    public FilterEntry(String material) {
        this.material = material;
    }

    public FilterEntry(String material, int maxCount) {
        this.material = material;
        this.maxCount = maxCount;
    }

    @Override
    public String toString() {
        if (maxCount <= 0) {
            return material + " (bez limitu)";
        } else {
            return material + " (max: " + maxCount + ")";
        }
    }
}