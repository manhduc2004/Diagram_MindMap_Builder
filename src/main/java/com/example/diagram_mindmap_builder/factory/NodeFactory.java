package com.example.diagram_mindmap_builder.factory;

import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;

public class NodeFactory {
    public static NodeModel createNode(NodeType type){
        switch (type){
            case Circle:
                return new com.example.diagram_mindmap_builder.model.CircleNodeModel();
            case Rectangle:
                return new com.example.diagram_mindmap_builder.model.RectangleNodeModel();
            default:
                throw new IllegalArgumentException("Loại node chưa hỗ trợ: " + type);
        }
    }
}
