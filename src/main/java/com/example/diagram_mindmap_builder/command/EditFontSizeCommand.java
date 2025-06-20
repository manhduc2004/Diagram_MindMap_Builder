package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;

public class EditFontSizeCommand implements Command {
    private final NodeModel node;
    private final double oldSize;
    private final double newSize;

    public EditFontSizeCommand(NodeModel node, double oldSize, double newSize) {
        this.node = node;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    @Override
    public void execute() {
        node.setFontSize(newSize);
    }

    @Override
    public void undo() {
        node.setFontSize(oldSize);
    }

    @Override
    public String getName() {
        return "Edit Font Size";
    }
}
