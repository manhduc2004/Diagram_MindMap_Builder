package com.example.diagram_mindmap_builder.model;

public class CircleNodeModel extends NodeModel{
    public CircleNodeModel(){
        heightProperty().bind(widthProperty());
    }

    @Override
    public NodeType getType(){
        return NodeType.Circle;
    }
}
