package com.example.diagram_mindmap_builder.builder;

import com.example.diagram_mindmap_builder.builder.NodeBuilder;
import com.example.diagram_mindmap_builder.factory.NodeFactory;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class NodeDirector {
    public static NodeModel makeDefault(NodeType type){
        NodeBuilder builder = NodeFactory.createBuilder(type);
        return  builder
                .withText(type.name())
                .withFill(Color.LIGHTBLUE)
                .withStroke(Color.DARKBLUE, 2.0)
                .withFontSize(12)
                .withPosition(0, 0)
                .build();
    }
}
