package com.example.diagram_mindmap_builder.controller;

import com.example.diagram_mindmap_builder.factory.NodeFactory;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import com.example.diagram_mindmap_builder.view.NodeView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainController {

    @FXML private Pane canvasPane;
    @FXML private Group contentGroup;    // injected từ FXML
    @FXML private Button btnAddCircle;
    @FXML private Button btnAddRect;
    @FXML private Button btnDelete;
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;

    @FXML private Label lblNoSelection;
    @FXML private TextField tfLabel;
    @FXML private Spinner<Double> spFontSize;
    @FXML private ColorPicker cpFill;
    @FXML private ColorPicker cpStroke;
    @FXML private Spinner<Double> spStrokeWidth;
    @FXML private ComboBox<String> cbShape;
    @FXML private CheckBox chkSnap;
    @FXML private Spinner<Integer> spGridSize;

    @FXML private Label lblStatus;
    @FXML private Label lblZoomLevel;
    @FXML private Label lblCoordinates;

    private Canvas gridCanvas;
    private double zoomFactor = 1.0;

    // Snap-to-grid và grid size
    private final BooleanProperty snapToGrid = new SimpleBooleanProperty(false);
    private final IntegerProperty gridSize = new SimpleIntegerProperty(20);

    private final Map<String, NodeView> nodeViewMap = new HashMap<>();
    private NodeView selectedNodeView = null;

    @FXML
    public void initialize() {
        // Khởi tạo Spinner factories
        spFontSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(6, 72, 12, 1));
        spStrokeWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 20, 1, 0.5));
        spGridSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20, 5));

        // ComboBox shape
        cbShape.getItems().addAll("Circle", "Rectangle");

        // Disable property pane ban đầu
        setPropertyPaneDisabled(true);

        // Thiết lập gridCanvas và vẽ grid tĩnh
        setupGridCanvas();

        // Khi resize canvasPane: redraw grid và clamp nodes
        canvasPane.widthProperty().addListener((obs, o, n) -> {
            redrawGrid();
            clampAllNodes();
        });
        canvasPane.heightProperty().addListener((obs, o, n) -> {
            redrawGrid();
            clampAllNodes();
        });

        // Snap-to-grid binding
        chkSnap.selectedProperty().bindBidirectional(snapToGrid);
        spGridSize.valueProperty().addListener((obs, old, ni) -> {
            if (ni != null) {
                gridSize.set(ni);
                redrawGrid();
                clampAllNodes();
            }
        });

        // Zoom buttons
        btnZoomIn.setOnAction(e -> {
            zoomFactor *= 1.1;
            applyZoom();
        });
        btnZoomOut.setOnAction(e -> {
            zoomFactor /= 1.1;
            applyZoom();
        });
        updateZoomLabel();

        // Mouse moved: hiển thị nội dung coords
        canvasPane.addEventFilter(MouseEvent.MOUSE_MOVED, evt -> {
            Point2D p = contentGroup.sceneToLocal(evt.getSceneX(), evt.getSceneY());
            lblCoordinates.setText(String.format("Coordinates: (%.1f, %.1f)", p.getX(), p.getY()));
        });

        // Click trắng canvas: clear selection
        canvasPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            if (evt.getTarget() == canvasPane) {
                clearSelection();
            }
        });

        // Button Add Circle/Rectangle/Delete
        btnAddCircle.setOnAction(evt -> addNode(NodeType.Circle));
        btnAddRect.setOnAction(evt -> addNode(NodeType.Rectangle));
        btnDelete.setOnAction(evt -> {
            if (selectedNodeView != null) deleteSelectedNode();
            else lblStatus.setText("No node selected to delete");
        });

        // Property pane binding: khi chỉnh, cập nhật model
        tfLabel.textProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null) {
                selectedNodeView.getModel().setText(ni);
            }
        });
        spFontSize.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) {
                selectedNodeView.getModel().setFontSize(ni);
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
        cbShape.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null && !ni.equals(old)) {
                changeShapeOfSelected(ni);
            }
        });
    }

    /** Thiết lập gridCanvas để vẽ grid dưới contentGroup */
    private void setupGridCanvas() {
        gridCanvas = new Canvas();
        gridCanvas.widthProperty().bind(canvasPane.widthProperty());
        gridCanvas.heightProperty().bind(canvasPane.heightProperty());
        // Thêm gridCanvas trước contentGroup: index 0
        canvasPane.getChildren().add(0, gridCanvas);
        redrawGrid();
    }

    /** Vẽ grid tĩnh (không scale) với spacing = gridSize */
    private void redrawGrid() {
        double w = canvasPane.getWidth();
        double h = canvasPane.getHeight();
        double gs = gridSize.get();
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (double x = 0; x < w; x += gs) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y < h; y += gs) {
            gc.strokeLine(0, y, w, y);
        }
    }

    /** Áp dụng zoom: scale contentGroup */
    private void applyZoom() {
        contentGroup.setScaleX(zoomFactor);
        contentGroup.setScaleY(zoomFactor);
        updateZoomLabel();
        clampAllNodes();
    }

    private void updateZoomLabel() {
        lblZoomLevel.setText(String.format("Zoom: %.0f%%", zoomFactor * 100));
    }

    /** Thêm node mới */
    private void addNode(NodeType type) {
        NodeModel model = NodeFactory.createNode(type);
        model.setText(type.name());
        model.setFillColor(Color.LIGHTBLUE);
        model.setStrokeColor(Color.DARKBLUE);
        model.setStrokeWidth(2.0);
        model.setFontSize(12);

        // Tạo NodeView truyền canvasPane, contentGroup, snapToGrid, gridSize, zoomSupplier
        NodeView view = new NodeView(model, canvasPane, contentGroup, snapToGrid, gridSize, () -> zoomFactor);
        view.setOnMouseClicked(evt -> {
            selectNodeView(view);
            evt.consume();
        });

        contentGroup.getChildren().add(view);
        nodeViewMap.put(model.getId(), view);

        // Đặt vị trí giữa nội dung sau layout
        Platform.runLater(() -> {
            double contentW = canvasPane.getWidth() / zoomFactor;
            double contentH = canvasPane.getHeight() / zoomFactor;
            double halfW = model.getWidth() / 2.0;
            double halfH = (type == NodeType.Circle) ? halfW : (model.getHeight() / 2.0);
            double centerX = clamp(contentW / 2.0, halfW, contentW - halfW);
            double centerY = clamp(contentH / 2.0, halfH, contentH - halfH);

            if (snapToGrid.get()) {
                centerX = Math.round(centerX / gridSize.get()) * gridSize.get();
                centerY = Math.round(centerY / gridSize.get()) * gridSize.get();
                centerX = clamp(centerX, halfW, contentW - halfW);
                centerY = clamp(centerY, halfH, contentH - halfH);
            }
            model.setX(centerX);
            model.setY(centerY);
            lblStatus.setText("Added " + type.name());
        });
    }

    /** Chọn node: highlight và populate property pane */
    private void selectNodeView(NodeView view) {
        if (selectedNodeView != null) {
            selectedNodeView.setSelected(false);
        }
        selectedNodeView = view;
        selectedNodeView.setSelected(true);

        NodeModel m = view.getModel();
        lblNoSelection.setText("Selected: " + m.getType() +
                String.format(" (%.1f,%.1f)", m.getX(), m.getY()));
        setPropertyPaneDisabled(false);

        tfLabel.setText(m.getText());
        spFontSize.getValueFactory().setValue(m.getFontSize());
        cpFill.setValue(m.getFillColor());
        cpStroke.setValue(m.getStrokeColor());
        spStrokeWidth.getValueFactory().setValue(m.getStrokeWidth());
        if (m.getType() == NodeType.Circle) {
            cbShape.setValue("Circle");
            // width spinner
            SpinnerValueFactory.DoubleSpinnerValueFactory vfW =
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, m.getWidth(), 5);
            // Nếu bạn dùng spWidth/spHeight, cần inject và binding tương tự
        } else {
            cbShape.setValue("Rectangle");
            SpinnerValueFactory.DoubleSpinnerValueFactory vfW =
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, m.getWidth(), 5);
            SpinnerValueFactory.DoubleSpinnerValueFactory vfH =
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, m.getHeight(), 5);
        }
    }

    /** Clear selection */
    private void clearSelection() {
        if (selectedNodeView != null) {
            selectedNodeView.setSelected(false);
            selectedNodeView = null;
        }
        lblNoSelection.setText("No selection");
        setPropertyPaneDisabled(true);
        lblStatus.setText("Ready");
    }

    /** Delete selected node */
    private void deleteSelectedNode() {
        if (selectedNodeView != null) {
            NodeModel m = selectedNodeView.getModel();
            contentGroup.getChildren().remove(selectedNodeView);
            nodeViewMap.remove(m.getId());
            selectedNodeView = null;
            clearSelection();
        }
    }

    /** Change shape of selected node */
    private void changeShapeOfSelected(String shapeName) {
        if (selectedNodeView == null) return;
        NodeModel oldModel = selectedNodeView.getModel();
        NodeType oldType = oldModel.getType();
        NodeType newType = shapeName.equals("Circle") ? NodeType.Circle : NodeType.Rectangle;
        if (oldType == newType) return;

        // Tạo model mới, copy thuộc tính
        NodeModel newModel = NodeFactory.createNode(newType);
        newModel.setX(oldModel.getX());
        newModel.setY(oldModel.getY());
        newModel.setWidth(oldModel.getWidth());
        if (newType == NodeType.Rectangle) {
            newModel.setHeight(oldModel.getHeight());
        }
        newModel.setText(oldModel.getText());
        newModel.setFillColor(oldModel.getFillColor());
        newModel.setStrokeColor(oldModel.getStrokeColor());
        newModel.setStrokeWidth(oldModel.getStrokeWidth());
        newModel.setFontSize(oldModel.getFontSize());

        NodeView newView = new NodeView(newModel, canvasPane, contentGroup, snapToGrid, gridSize, () -> zoomFactor);
        newView.setOnMouseClicked(evt -> {
            selectNodeView(newView);
            evt.consume();
        });

        contentGroup.getChildren().remove(selectedNodeView);
        contentGroup.getChildren().add(newView);
        nodeViewMap.remove(oldModel.getId());
        nodeViewMap.put(newModel.getId(), newView);
        selectedNodeView = newView;
        selectNodeView(newView);
    }

    /** Clamp all nodes khi resize hoặc zoom hoặc grid change */
    private void clampAllNodes() {
        double viewW = canvasPane.getWidth();
        double viewH = canvasPane.getHeight();
        double contentW = viewW / zoomFactor;
        double contentH = viewH / zoomFactor;
        for (NodeView nv : nodeViewMap.values()) {
            NodeModel m = nv.getModel();
            double halfW = m.getWidth() / 2.0;
            double halfH = (m.getType() == NodeType.Circle) ? halfW : (m.getHeight() / 2.0);
            double x = clamp(m.getX(), halfW, contentW - halfW);
            double y = clamp(m.getY(), halfH, contentH - halfH);
            if (snapToGrid.get()) {
                x = Math.round(x / gridSize.get()) * gridSize.get();
                y = Math.round(y / gridSize.get()) * gridSize.get();
                x = clamp(x, halfW, contentW - halfW);
                y = clamp(y, halfH, contentH - halfH);
            }
            m.setX(x);
            m.setY(y);
        }
    }

    private double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private void setPropertyPaneDisabled(boolean disable) {
        tfLabel.setDisable(disable);
        spFontSize.setDisable(disable);
        cpFill.setDisable(disable);
        cpStroke.setDisable(disable);
        spStrokeWidth.setDisable(disable);
        cbShape.setDisable(disable);
        chkSnap.setDisable(false); // luôn edit snap được
        spGridSize.setDisable(false);
    }
}
