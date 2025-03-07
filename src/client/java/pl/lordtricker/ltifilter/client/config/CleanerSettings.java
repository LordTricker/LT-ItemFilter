package pl.lordtricker.ltifilter.client.config;

import java.util.ArrayList;
import java.util.List;

public class CleanerSettings {
    public int throwIntervalTicks = 2;
    public long blockDurationMs = 200;

    //    public String pickupSound = "minecraft:entity.experience_orb.pickup";
    //    public int pickupSoundDelayTicks = 2;

    public List<Integer> doNotCleanSlots = new ArrayList<>();
}