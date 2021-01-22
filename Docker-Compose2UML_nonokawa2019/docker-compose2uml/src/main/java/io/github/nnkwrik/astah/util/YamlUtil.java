package io.github.nnkwrik.astah.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.util.Map;

/**
 * YAML util,
 *
 * @author Reika Nonokawa
 *
 */
public class YamlUtil {
    private static final ObjectMapper jsonMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper yamlMapper = new YAMLMapper();

    public static String mapToJson(Map<String, String> map) {
        String json = null;
        try {
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JsonNode readTree(String raw){
        JsonNode node = null;
        try {
            node = yamlMapper.readTree(raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    public static <T> T readValue(String content, JavaType type){
        try {
            return jsonMapper.readValue(content,type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode valueToTree(Object node){
        return jsonMapper.valueToTree(node);
    }


    public static String getRawText(Object node)  {
        String raw = null;
        try {
            raw = yamlMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //上の方法でRaw Stringを取ると、なぜか1行目"---"になる
        int nextLineStart = raw.indexOf('\n') + 1;
        if (raw.substring(0, nextLineStart).contains("---")) {
            raw = raw.substring(nextLineStart);
        }
        return raw;
    }



}
