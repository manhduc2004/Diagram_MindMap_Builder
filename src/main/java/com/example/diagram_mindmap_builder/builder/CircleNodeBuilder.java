package com.example.diagram_mindmap_builder.builder;

import com.example.diagram_mindmap_builder.model.CircleNodeModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.scene.paint.Color;

public class CircleNodeBuilder implements NodeBuilder{
    private final CircleNodeModel model = new CircleNodeModel();
    @Override
    public NodeBuilder withText(String text) {
        model.setText(text);
        return this;
    }
    @Override
    public NodeBuilder withPosition(double x, double y) {
        model.setX(x);
        model.setY(y);
        return this;
    }
    @Override
    public NodeBuilder withSize(double width, double height){
        model.setWidth(width);
        model.setHeight(height);
        return this;
    }
    @Override
    public NodeBuilder withFill(Color fillColor) {
        model.setFillColor(fillColor);
        return this;
    }
    @Override
    public NodeBuilder withStroke(Color strokeColor, double strokeWidth) {
        model.setStrokeColor(strokeColor);
        model.setStrokeWidth(strokeWidth);
        return this;
    }
    @Override
    public NodeBuilder withFontSize(double fontSize) {
        model.setFontSize(fontSize);
        return this;
    }
    @Override
    public NodeModel build(){
        return model;
    }
    @Override
    public NodeType getType() {
        return NodeType.Circle;
    }

}
