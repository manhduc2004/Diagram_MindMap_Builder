package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;

public class CreateEdgeCommand implements Command {
    private final GraphModel graphModel;
    private final NodeModel source, target;
    private EdgeModel edge;

    public CreateEdgeCommand(GraphModel graphModel, NodeModel source, NodeModel target) {
        this.graphModel = graphModel;
        this.source = source;
        this.target = target;
    }

    public void execute() {
        if(edge == null) {
            edge = new EdgeModel(source, target);
        }
        graphModel.addEdge(edge);
    }

    public void undo() {
        graphModel.removeEdge(edge);
    }

    public String getName() {
        return "Create Edge Command" ;
    }
}
