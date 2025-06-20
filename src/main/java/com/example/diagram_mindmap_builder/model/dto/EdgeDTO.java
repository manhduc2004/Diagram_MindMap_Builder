package com.example.diagram_mindmap_builder.model.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class EdgeDTO {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    public String id;
    @JacksonXmlProperty(localName = "SourceId")
    public String sourceId;
    @JacksonXmlProperty(localName = "TargetId")
    public String targetId;
}
