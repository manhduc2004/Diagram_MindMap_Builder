package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;

public class MoveNodeCommand implements Command {
    private final NodeModel node;
    private final double oldX, oldY;
    private final double newX, newY;

    public MoveNodeCommand(NodeModel node, double oldX, double oldY, double newX, double newY) {
        this.node = node;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    public void execute() {
        node.setX(newX);
        node.setY(newY);
    }

    public void undo() {
        node.setX(oldX);
        node.setY(oldY);
    }

    public String getName() {
        return "MoveNodeCommand";
    }
}
