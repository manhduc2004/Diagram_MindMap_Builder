package com.example.diagram_mindmap_builder.model;

public class EdgeModel {
    private final String id;
    private NodeModel source;
    private NodeModel target;

    public EdgeModel(NodeModel source, NodeModel target){
        this.source = source;
        this.target = target;
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public NodeModel getSource() {
        return source;
    }
    public void setSource(NodeModel s){
        this.source = s;
    }

    public NodeModel getTarget() {
        return target;
    }
    public void setTarget(NodeModel t){
        this.target = t;
    }
}
