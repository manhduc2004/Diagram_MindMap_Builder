package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;

/**
 * Command để thay đổi stroke width của NodeModel, hỗ trợ undo/redo.
 */
public class EditStrokeWidthCommand implements Command {
    private final NodeModel node;
    private final double oldWidth;
    private final double newWidth;

    public EditStrokeWidthCommand(NodeModel node, double oldWidth, double newWidth) {
        this.node = node;
        this.oldWidth = oldWidth;
        this.newWidth = newWidth;
    }

    @Override
    public void execute() {
        node.setStrokeWidth(newWidth);
    }

    @Override
    public void undo() {
        node.setStrokeWidth(oldWidth);
    }

    @Override
    public String getName() {
        return "Edit Stroke Width";
    }
}
