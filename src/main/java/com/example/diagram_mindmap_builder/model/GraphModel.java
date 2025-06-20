package com.example.diagram_mindmap_builder.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GraphModel {
    private final ObservableList<NodeModel> nodes = FXCollections.observableArrayList();
    private final ObservableList<EdgeModel> edges = FXCollections.observableArrayList();

    public ObservableList<NodeModel> getNodes() {
        return nodes;
    }

    public ObservableList<EdgeModel> getEdges() {
        return edges;
    }

    public void addNode(NodeModel node) {
        nodes.add(node);
    }

    public void addNodeAt(int index, NodeModel node) {
        nodes.add(index, node);
    }

    public void removeNode(NodeModel node) {
        nodes.remove(node);
        edges.removeIf(e -> e.getSource() == node || e.getTarget() == node);
    }

    public int indexOfNode(NodeModel node) {
        return nodes.indexOf(node);
    }

    public void addEdge(EdgeModel edge) {
        edges.add(edge);
    }

    public void removeEdge(EdgeModel edge) {
        edges.remove(edge);
    }
}
