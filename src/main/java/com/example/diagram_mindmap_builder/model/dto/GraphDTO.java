package com.example.diagram_mindmap_builder.model.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "Graph")
public class GraphDTO {
    @JacksonXmlElementWrapper(localName = "Nodes")
    @JacksonXmlProperty(localName = "Node")
    public List<NodeDTO> nodes;

    @JacksonXmlElementWrapper(localName = "Edges")
    @JacksonXmlProperty(localName = "Edge")
    public List<EdgeDTO> edges;
}
