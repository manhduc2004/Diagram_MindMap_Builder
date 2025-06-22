package com.example.diagram_mindmap_builder.persistence;

import com.example.diagram_mindmap_builder.model.dto.GraphDTO;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;

public class XMLStrategy  implements Serializer {
    private final XmlMapper xmlMapper;
    public XMLStrategy() {
        xmlMapper = new XmlMapper();
        xmlMapper.writerWithDefaultPrettyPrinter();
        xmlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public void save(GraphDTO graphDTO, File file) throws IOException {
        xmlMapper.writeValue(file, graphDTO);
        xmlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }
    public GraphDTO load(File file) throws IOException {
        return xmlMapper.readValue(file, GraphDTO.class);
    }
}