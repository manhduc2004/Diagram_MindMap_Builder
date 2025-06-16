package com.example.diagram_mindmap_builder.controller;

import com.example.diagram_mindmap_builder.factory.NodeFactory;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import com.example.diagram_mindmap_builder.view.NodeView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private Pane canvasPane;
    @FXML private Button btnAddCircle;
    @FXML private Button btnAddRect;
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;
    @FXML private Label statusBar;

    @FXML private Label lblNoSelection;
    @FXML private TextField tfLabel;
    @FXML private ColorPicker cpFill;
    @FXML private ColorPicker cpStroke;
    @FXML private Spinner<Double> spStrokeWidth;
    @FXML private Spinner<Double> spWidth;
    @FXML private Spinner<Double> spHeight;

    private final Map<String, NodeView> nodeViewMap = new HashMap<>();

    private NodeView selectedNodeView = null;

    @FXML public void initialize() {
        // Khởi tạo Spinner factories
        spStrokeWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 20, 1, 0.5));
        spWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, 80, 5));
        spHeight.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, 40, 5));
        // Ban đầu disable
        setPropertyPaneDisabled(true);

        // Button Add
        btnAddCircle.setOnAction(evt -> addNode(NodeType.Circle));
        btnAddRect.setOnAction(evt -> addNode(NodeType.Rectangle));

        // Zoom (nếu muốn): ví dụ scale canvasPane
        btnZoomIn.setOnAction(evt -> canvasPane.setScaleX(canvasPane.getScaleX() * 1.1));
        btnZoomIn.setOnAction(evt -> {
            canvasPane.setScaleX(canvasPane.getScaleX() * 1.1);
            canvasPane.setScaleY(canvasPane.getScaleY() * 1.1);
        });
        btnZoomOut.setOnAction(evt -> {
            canvasPane.setScaleX(canvasPane.getScaleX() / 1.1);
            canvasPane.setScaleY(canvasPane.getScaleY() / 1.1);
        });

        // Click lên vùng trống của canvas để clear selection
        canvasPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            if (evt.getTarget() == canvasPane) {
                clearSelection();
            }
        });

        // Khi resize canvas (dù bạn không cho resize, nhưng để an toàn)
        canvasPane.widthProperty().addListener((obs, oldW, newW) -> clampAllNodes());
        canvasPane.heightProperty().addListener((obs, oldH, newH) -> clampAllNodes());

        // Khi người dùng chỉnh property, cập nhật model
        tfLabel.textProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null) {
                selectedNodeView.getModel().setText(ni);
            }
        });
        cpFill.setOnAction(evt -> {
            if (selectedNodeView != null) {
                selectedNodeView.getModel().setFillColor(cpFill.getValue());
            }
        });
        cpStroke.setOnAction(evt -> {
            if (selectedNodeView != null) {
                selectedNodeView.getModel().setStrokeColor(cpStroke.getValue());
            }
        });
        spStrokeWidth.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) {
                selectedNodeView.getModel().setStrokeWidth(ni);
            }
        });
        spWidth.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) {
                selectedNodeView.getModel().setWidth(ni);
                // Nếu circle, height bind với width trong model sẽ tự cập nhật
            }
        });
        spHeight.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) {
                // Với circle, ignore height; với rectangle, cập nhật
                if (selectedNodeView.getModel().getType() == NodeType.Rectangle) {
                    selectedNodeView.getModel().setHeight(ni);
                }
            }
        });
    }

    private void addNode(NodeType type) {
        NodeModel model = NodeFactory.createNode(type);
        model.setText(type.name());
        // Màu mặc định để dễ thấy
        model.setFillColor(Color.LIGHTBLUE);
        model.setStrokeColor(Color.DARKBLUE);
        model.setStrokeWidth(2.0);

        NodeView view = new NodeView(model, canvasPane);
        // Khi click node, chọn
        view.setOnMouseClicked(evt -> {
            selectNodeView(view);
            evt.consume();
        });

        canvasPane.getChildren().add(view);
        nodeViewMap.put(model.getId(), view);

        // Đặt vị trí giữa canvas sau layout xong
        Platform.runLater(() -> {
            double w = canvasPane.getWidth();
            double h = canvasPane.getHeight();
            double halfW = model.getWidth() / 2.0;
            double halfH = (type == NodeType.Circle) ? (model.getWidth() / 2.0) : (model.getHeight() / 2.0);
            double centerX = clamp(w / 2.0, halfW, w - halfW);
            double centerY = clamp(h / 2.0, halfH, h - halfH);
            model.setX(centerX);
            model.setY(centerY);
            statusBar.setText("Added " + type.name() + " at (" +
                    String.format("%.1f", centerX) + "," + String.format("%.1f", centerY) + ")");
        });
    }

    private void selectNodeView(NodeView view) {
        // Bỏ highlight node cũ
        if (selectedNodeView != null) {
            selectedNodeView.setSelected(false);
        }
        selectedNodeView = view;
        // Highlight mới
        selectedNodeView.setSelected(true);

        // Hiển thị properties
        NodeModel m = view.getModel();
        lblNoSelection.setText("Selected: " + m.getType() +
                " at (" + String.format("%.1f", m.getX()) + "," + String.format("%.1f", m.getY()) + ")");
        setPropertyPaneDisabled(false);
        // Đặt giá trị controls theo model
        tfLabel.setText(m.getText());
        cpFill.setValue(m.getFillColor());
        cpStroke.setValue(m.getStrokeColor());
        spStrokeWidth.getValueFactory().setValue(m.getStrokeWidth());
        spWidth.getValueFactory().setValue(m.getWidth());
        if (m.getType() == NodeType.Rectangle) {
            spHeight.getValueFactory().setValue(m.getHeight());
            spHeight.setDisable(false);
        } else {
            // circle: disable height spinner
            spHeight.setDisable(true);
        }
        statusBar.setText("Node selected");
    }

    private void clearSelection() {
        if (selectedNodeView != null) {
            selectedNodeView.setSelected(false);
            selectedNodeView = null;
        }
        lblNoSelection.setText("No selection");
        setPropertyPaneDisabled(true);
        statusBar.setText("Ready");
    }

    private void setPropertyPaneDisabled(boolean disable) {
        tfLabel.setDisable(disable);
        cpFill.setDisable(disable);
        cpStroke.setDisable(disable);
        spStrokeWidth.setDisable(disable);
        spWidth.setDisable(disable);
        spHeight.setDisable(disable);
    }

    private void clampAllNodes() {
        double w = canvasPane.getWidth();
        double h = canvasPane.getHeight();
        for (NodeView nv : nodeViewMap.values()) {
            NodeModel m = nv.getModel();
            double halfW = m.getWidth() / 2.0;
            double halfH = (m.getType() == NodeType.Circle) ? halfW : (m.getHeight() / 2.0);
            double clampedX = clamp(m.getX(), halfW, w - halfW);
            double clampedY = clamp(m.getY(), halfH, h - halfH);
            if (clampedX != m.getX()) m.setX(clampedX);
            if (clampedY != m.getY()) m.setY(clampedY);
        }
    }

    private double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}
