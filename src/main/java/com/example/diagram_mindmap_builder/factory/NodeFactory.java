package com.example.diagram_mindmap_builder.factory;

import com.example.diagram_mindmap_builder.builder.CircleNodeBuilder;
import com.example.diagram_mindmap_builder.builder.NodeBuilder;
import com.example.diagram_mindmap_builder.builder.RectangleNodeBuilder;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;

public class NodeFactory {
    public static NodeBuilder createBuilder(NodeType type){
        switch (type){
            case Circle:
                return new CircleNodeBuilder();
            case Rectangle:
                return new RectangleNodeBuilder();
            default:
                throw new IllegalArgumentException("Loại node chưa hỗ trợ: " + type);
        }
    }
}
