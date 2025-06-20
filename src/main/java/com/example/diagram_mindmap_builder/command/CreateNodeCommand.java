package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.builder.NodeDirector;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;

public class CreateNodeCommand implements Command {
    public final GraphModel graphModel;
    public final NodeType type;
    private NodeModel node;
    private int addedIndex;

    public CreateNodeCommand(GraphModel graphModel, NodeType type) {
        this.graphModel = graphModel;
        this.type = type;
    }

    public void execute() {
        if(node==null){
            node = NodeDirector.makeDefault(type);
        }
        addedIndex = graphModel.getNodes().size();
        graphModel.addNode(node);
    }

    public void undo(){
        graphModel.removeNode(node);
    }

    public String getName() {
        return "Create Node" + type.name();
    }

    public NodeModel getNode() {
        return node;
    }
}
