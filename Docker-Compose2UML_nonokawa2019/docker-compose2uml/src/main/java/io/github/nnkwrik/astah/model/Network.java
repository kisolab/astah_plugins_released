package io.github.nnkwrik.astah.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Network extends Element {

    public static final Network defaultNetwork = new Network("DEFAULT");

    public Network(String name) {
        super(name);
    }

}
