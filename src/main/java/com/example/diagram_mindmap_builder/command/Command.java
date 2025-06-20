package com.example.diagram_mindmap_builder.command;

public interface Command {
    void execute();
    void undo();
    String getName();
}

