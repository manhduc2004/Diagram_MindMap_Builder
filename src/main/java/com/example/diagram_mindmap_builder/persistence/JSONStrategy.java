package com.example.diagram_mindmap_builder.persistence;

import com.example.diagram_mindmap_builder.model.dto.GraphDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class JSONStrategy implements Serializer {
    private final ObjectMapper mapper;
    public JSONStrategy() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void save(GraphDTO graphDTO, File file) throws IOException {
        mapper.writeValue(file, graphDTO);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public GraphDTO load(File file) throws IOException {
        return mapper.readValue(file, GraphDTO.class);
    }
}
