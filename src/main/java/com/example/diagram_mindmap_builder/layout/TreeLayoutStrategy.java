package com.example.diagram_mindmap_builder.layout;

import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.geometry.Point2D;

import java.util.Map;

public class TreeLayoutStrategy implements LayoutStrategy {
    @Override
    public Map<NodeModel, Point2D> applyLayout(GraphModel graphModel) {
        Map<NodeModel, Point2D> result = new java.util.HashMap<>();
        double x0 = 100, y0 = 100, dx = 150, dy = 100;
        int i=0;
        for (NodeModel node : graphModel.getNodes()) {
            double nx = x0 + (i % 5) * dx;
            double ny = y0 + (i / 5) * dy;
            result.put(node, new Point2D(nx, ny)); i++;
        }
        return result;
    }
}