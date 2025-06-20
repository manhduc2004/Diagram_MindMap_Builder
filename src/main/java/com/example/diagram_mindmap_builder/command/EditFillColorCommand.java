package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;
import javafx.scene.paint.Color;

public class EditFillColorCommand implements Command {
    private final NodeModel node;
    private final Color oldColor;
    private final Color newColor;

    public EditFillColorCommand(NodeModel node, Color oldColor, Color newColor) {
        this.node = node;
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        node.setFillColor(newColor);
    }

    @Override
    public void undo() {
        node.setFillColor(oldColor);
    }

    @Override
    public String getName() {
        return "Edit Fill Color";
    }
}
