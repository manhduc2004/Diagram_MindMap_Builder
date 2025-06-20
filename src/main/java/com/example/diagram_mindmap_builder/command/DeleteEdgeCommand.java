package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.GraphModel;

public class DeleteEdgeCommand implements Command {
    private final GraphModel graphModel;
    private final EdgeModel edge;
    private int removedIndex;

    public DeleteEdgeCommand(GraphModel graphModel, EdgeModel edge) {
        this.graphModel = graphModel;
        this.edge = edge;
    }

    public void execute() {
        removedIndex = graphModel.getEdges().indexOf(edge);
        graphModel.removeEdge(edge);
    }

    public void undo() {
        if (removedIndex >= 0 && removedIndex <= graphModel.getEdges().size()) {
            graphModel.getEdges().add(removedIndex, edge);
        }
        else{
            graphModel.addEdge(edge);
        }
    }

    public String getName() {
        return "Delete Edge Command" ;
    }
}
