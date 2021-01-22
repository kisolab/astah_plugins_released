package io.github.nnkwrik.astah.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
public class Element implements Serializable {

    private String name;

    private String raw;

    private LinkedHashMap<String, String> detailInfo;

    public Element(String name) {
        this.name = name;
    }

}
