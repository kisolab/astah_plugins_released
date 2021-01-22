package io.github.nnkwrik.astah.model;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
public class Service extends Element {

    private List<Element> ports;

    private Element build;

    private Element image;

    private List<String> dependsOn;

    private List<String> networks;

    private final Map<Service, List<Network>> dependsOrLinksService = new HashMap<>();

    public Service(String name) {
        super(name);
    }

    public void addDependsOrLinksService(Service dependsService, List<Network> networks) {
        dependsOrLinksService.put(dependsService, networks);
    }

    public void removeDependsOrLinksService(Service dependsService) {
        dependsOrLinksService.remove(dependsService);
    }
}
