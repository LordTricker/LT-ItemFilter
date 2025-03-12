package pl.lordtricker.ltifilter.client.config;

import java.util.ArrayList;
import java.util.List;

public class ServerEntry {
    public String profileName;
    public List<String> domains = new ArrayList<>();
    public List<FilterEntry> filters = new ArrayList<>();
}