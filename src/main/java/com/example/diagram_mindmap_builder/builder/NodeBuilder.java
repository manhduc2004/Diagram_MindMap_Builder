package com.example.diagram_mindmap_builder.builder;

import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.scene.paint.Color;

public interface NodeBuilder {
    NodeBuilder withText(String text);
    NodeBuilder withPosition(double x, double y);
    NodeBuilder withSize(double width, double height);
    NodeBuilder withFill(Color fillColor);
    NodeBuilder withStroke(Color strokeColor, double strokeWidth);
    NodeBuilder withFontSize(double fontSize);
    NodeModel build();
    NodeType getType();
}