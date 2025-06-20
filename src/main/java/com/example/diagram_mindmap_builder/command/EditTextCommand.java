package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.NodeModel;

public class EditTextCommand  implements Command {
    private final NodeModel node;
    private final String oldText;
    private final String newText;

    public EditTextCommand(NodeModel node, String oldText, String newText) {
        this.node = node;
        this.oldText = oldText;
        this.newText = newText;
    }

    public void execute() {
        node.setText(newText);
    }

    public void undo() {
        node.setText(oldText);
    }

    public String getName(){
        return "Delete Edge Command" ;
    }
}
