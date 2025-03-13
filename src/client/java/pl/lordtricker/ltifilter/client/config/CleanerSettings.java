package pl.lordtricker.ltifilter.client.config;

import java.util.ArrayList;
import java.util.List;

public class CleanerSettings {
    public int throwIntervalTicks = 2;
    public int movementDelayTicks = 0;

    public List<Integer> doNotCleanSlots = new ArrayList<>();
}
