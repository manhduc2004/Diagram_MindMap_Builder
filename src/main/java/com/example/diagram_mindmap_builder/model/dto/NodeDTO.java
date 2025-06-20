package com.example.diagram_mindmap_builder.model.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NodeDTO {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    public String id;
    @JacksonXmlProperty(localName = "Type")
    public String type;
    @JacksonXmlProperty(localName = "X")
    public double x;
    @JacksonXmlProperty(localName = "Y")
    public double y;
    @JacksonXmlProperty(localName = "Width")
    public double width;
    @JacksonXmlProperty(localName = "Height")
    public Double height; // null nếu Circle
    @JacksonXmlProperty(localName = "Text")
    public String text;
    @JacksonXmlProperty(localName = "FillColor")
    public String fillColor;
    @JacksonXmlProperty(localName = "StrokeColor")
    public String strokeColor;
    @JacksonXmlProperty(localName = "StrokeWidth")
    public double strokeWidth;
    @JacksonXmlProperty(localName = "FontSize")
    public double fontSize;
}
