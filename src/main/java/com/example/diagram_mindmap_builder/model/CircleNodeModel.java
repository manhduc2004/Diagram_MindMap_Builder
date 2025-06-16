package com.example.diagram_mindmap_builder.model;

public class CircleNodeModel extends NodeModel{
    public CircleNodeModel(){
        super();

        heightProperty().bind(widthProperty());
    }

    public NodeType getType(){
        return NodeType.Circle;
    }
}
