package io.github.nnkwrik.astah.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
public class Compose extends Element {

    private String version;

    private List<Service> services;

    private List<Network> networks;

    public Compose(String name) {
        super(name);
    }

}
