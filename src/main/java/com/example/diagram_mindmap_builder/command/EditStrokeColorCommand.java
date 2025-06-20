package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.scene.paint.Color;

/**
 * Command để thay đổi stroke color của NodeModel, hỗ trợ undo/redo.
 */
public class EditStrokeColorCommand implements Command {
    private final NodeModel node;
    private final Color oldColor;
    private final Color newColor;

    public EditStrokeColorCommand(NodeModel node, Color oldColor, Color newColor) {
        this.node = node;
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        node.setStrokeColor(newColor);
    }

    @Override
    public void undo() {
        node.setStrokeColor(oldColor);
    }

    @Override
    public String getName() {
        return "Edit Stroke Color";
    }
}
