package com.example.diagram_mindmap_builder.ui;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import com.example.diagram_mindmap_builder.command.CreateNodeCommand;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.command.CommandManager;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.scene.control.Label;

public class NodeTemplate {
    public String name;
    public NodeType type;
    public double width, height;
    public String fillColor;
    public String strokeColor;
    public double strokeWidth;
    public double fontSize;
    public String defaultText;

    public NodeTemplate(String name, NodeType type, double width, double height,
                        String fillColor, String strokeColor, double strokeWidth, double fontSize, String defaultText) {
        this.name = name;
        this.type = type;
        this.width = width;
        this.height = height;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.fontSize = fontSize;
        this.defaultText = defaultText;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String serialize(NodeTemplate tpl) {
        return String.format("%s;%s;%.1f;%.1f;%s;%s;%.1f;%.1f;%s",
                tpl.name, tpl.type.name(), tpl.width, tpl.height, tpl.fillColor, tpl.strokeColor,
                tpl.strokeWidth, tpl.fontSize, tpl.defaultText.replace(";", "\\;"));
    }

    public static NodeTemplate deserialize(String s) {
        String[] parts = s.split("(?<!\\\\)", 9);
        String name = parts[0];
        NodeType type = NodeType.valueOf(parts[1]);
        double w = Double.parseDouble(parts[2]);
        double h = Double.parseDouble(parts[3]);
        String fill = parts[4];
        String stroke = parts[5];
        double sw = Double.parseDouble(parts[6]);
        double fs = Double.parseDouble(parts[7]);
        String text = parts[8].replace("\\;", ";");
        return new NodeTemplate(name, type, w, h, fill, stroke, sw, fs, text);
    }

    public static void setupTemplateListView(ListView<NodeTemplate> listView, Pane canvasPane, Group contentGroup,
                                             GraphModel graphModel, CommandManager commandManager, Label lblStatus) {
        java.util.List<NodeTemplate> templates = new java.util.ArrayList<>();
        templates.add(new NodeTemplate("Default Circle", NodeType.Circle, 80, 80, "#ADD8E6", "#000000", 2, 12, "Node"));
        templates.add(new NodeTemplate("Default Rect", NodeType.Rectangle, 100, 60, "#90EE90", "#000000", 2, 12, "Item"));
        listView.getItems().setAll(templates);
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<NodeTemplate> call(ListView<NodeTemplate> lv) {
                ListCell<NodeTemplate> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(NodeTemplate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setText(null);
                        else setText(item.name);
                    }
                };
                cell.setOnDragDetected(evt -> {
                    if (cell.getItem() == null) return;
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(NodeTemplate.serialize(cell.getItem()));
                    db.setContent(content);
                    evt.consume();
                });
                return cell;
            }
        });
        canvasPane.setOnDragOver(evt -> {
            if (evt.getDragboard().hasString()) evt.acceptTransferModes(TransferMode.COPY);
            evt.consume();
        });
        canvasPane.setOnDragDropped(evt -> {
            Dragboard db = evt.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                NodeTemplate tpl = NodeTemplate.deserialize(db.getString());
                Point2D p = contentGroup.sceneToLocal(evt.getSceneX(), evt.getSceneY());
                CreateNodeCommand cmd = new CreateNodeCommand(graphModel, tpl.type);
                commandManager.executeCommand(cmd);
                NodeModel m = cmd.getNode();
                m.setX(p.getX());
                m.setY(p.getY());
                m.setWidth(tpl.width);
                if (tpl.type != NodeType.Circle) m.setHeight(tpl.height);
                m.setText(tpl.defaultText);
                m.setFillColor(Color.web(tpl.fillColor));
                m.setStrokeColor(Color.web(tpl.strokeColor));
                m.setStrokeWidth(tpl.strokeWidth);
                m.setFontSize(tpl.fontSize);
                lblStatus.setText("Added template: " + tpl.name);
                success = true;
            }
            evt.setDropCompleted(success);
            evt.consume();
        });
    }
}