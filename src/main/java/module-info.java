module com.example.diagram_mindmap_builder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires javafx.swing;

    opens com.example.diagram_mindmap_builder to javafx.fxml, javafx.graphics;
    opens com.example.diagram_mindmap_builder.controller to javafx.fxml;
    opens com.example.diagram_mindmap_builder.view to javafx.fxml;

    exports com.example.diagram_mindmap_builder;
    exports com.example.diagram_mindmap_builder.controller;
    exports com.example.diagram_mindmap_builder.model;
    exports com.example.diagram_mindmap_builder.factory;
    exports com.example.diagram_mindmap_builder.view;

}