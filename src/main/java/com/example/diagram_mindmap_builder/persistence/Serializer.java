package com.example.diagram_mindmap_builder.persistence;

import com.example.diagram_mindmap_builder.model.dto.GraphDTO;

import java.io.File;
import java.io.IOException;

public interface Serializer {
    void save(GraphDTO graphDTO, File file) throws IOException;
    GraphDTO load(File file) throws IOException;
}
