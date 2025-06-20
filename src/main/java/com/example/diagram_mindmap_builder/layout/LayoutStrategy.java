package com.example.diagram_mindmap_builder.layout;

import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.geometry.Point2D;

import java.util.Map;

public interface LayoutStrategy {
    Map<NodeModel, Point2D> applyLayout(GraphModel graphModel);
}
