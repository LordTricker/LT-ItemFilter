package pl.lordtricker.ltifilter.client.config;

import java.util.ArrayList;
import java.util.List;

public class ServerEntry {
    public List<String> domains = new ArrayList<>();
    public String profileName;

    public List<FilterEntry> filters = new ArrayList<>();
}
