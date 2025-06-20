package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;

import java.util.ArrayList;
import java.util.List;

public class DeleteNodeCommand implements Command {
    private final NodeModel node;
    private final GraphModel graphModel;
    private int removedIndex;
    private final List<EdgeModel> removedEdges = new ArrayList<>();

    public DeleteNodeCommand(GraphModel graphModel, NodeModel node) {
        this.node = node;
        this.graphModel = graphModel;
    }

    public void execute() {
        removedIndex = graphModel.indexOfNode(node);
        for(EdgeModel edge: graphModel.getEdges()){
            if(edge.getSource() == node || edge.getTarget() == node){
                removedEdges.add(edge);
            }
        }
        graphModel.removeNode(node);
        for(EdgeModel edge: removedEdges){
            graphModel.removeEdge(edge);
        }
    }

    public void undo() {
        if(removedIndex >= 0 && removedIndex <= graphModel.getNodes().size()){
            graphModel.addNodeAt(removedIndex, node);
        }
        else{
            graphModel.addNode(node);
        }
        for(EdgeModel edge: removedEdges){
            graphModel.addEdge(edge);
        }
    }

    public String getName(){
        return "DeleteNodeCommand";
    }

}
