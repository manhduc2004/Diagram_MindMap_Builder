package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.geometry.Point2D;
import java.util.Map;

public class BatchMoveCommand implements Command {
    private final Map<NodeModel, Point2D> oldPositions;
    private final Map<NodeModel, Point2D> newPositions;
    public BatchMoveCommand(Map<NodeModel, Point2D> oldPositions, Map<NodeModel, Point2D> newPositions) {
        this.oldPositions = oldPositions;
        this.newPositions = newPositions;
    }
    @Override
    public void execute() {
        for (Map.Entry<NodeModel, Point2D> e : newPositions.entrySet()) {
            NodeModel node = e.getKey();
            Point2D p = e.getValue();
            node.setX(p.getX()); node.setY(p.getY());
        }
    }
    @Override
    public void undo() {
        for (Map.Entry<NodeModel, Point2D> e : oldPositions.entrySet()) {
            NodeModel node = e.getKey();
            Point2D p = e.getValue();
            node.setX(p.getX());
            node.setY(p.getY());
        }
    }
    @Override public String getName() {
        return "Batch Move Layout";
    }
}
