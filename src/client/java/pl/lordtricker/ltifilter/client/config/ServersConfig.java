package pl.lordtricker.ltifilter.client.config;

import java.util.ArrayList;
import java.util.List;

public class ServersConfig {
    public String defaultProfile = "default";
    public List<ServerEntry> servers = new ArrayList<>();
    public BeamSettings beamSettings = new BeamSettings();
    public CleanerSettings cleanerSettings = new CleanerSettings();
}
