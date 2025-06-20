package com.example.diagram_mindmap_builder.layout;

import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.geometry.Point2D;

import java.util.Map;

public class RadialLayoutStrategy implements LayoutStrategy {
    @Override
    public Map<NodeModel, Point2D> applyLayout(GraphModel graphModel) {
        Map<NodeModel, Point2D> result = new java.util.HashMap<>();
        int n = graphModel.getNodes().size(); if (n==0) return result;
        double centerX = 400, centerY = 300, radius = 200;
        int i=0;
        for (NodeModel node : graphModel.getNodes()) {
            double angle = 2 * Math.PI * i / n;
            double nx = centerX + radius * Math.cos(angle);
            double ny = centerY + radius * Math.sin(angle);
            result.put(node, new Point2D(nx, ny));
            i++;
        }
        return result;
    }
}
