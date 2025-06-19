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
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class MainController {
    
    @FXML private Pane canvasPane;
    @FXML private Group contentGroup;

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

    private final BooleanProperty snapToGrid = new SimpleBooleanProperty(false);
    private final IntegerProperty gridSize = new SimpleIntegerProperty(20);

    private final Map<String, NodeView> nodeViewMap = new HashMap<>();
    private NodeView selectedNodeView = null;

    @FXML
    public void initialize() {
        // Debug injection: đảm bảo không null
        System.out.println("canvasPane=" + canvasPane + ", contentGroup=" + contentGroup + ", spFontSize=" + spFontSize);

        // Spinner factories
        spFontSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(6, 72, 12, 1));
        spStrokeWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 20, 1, 0.5));
        spGridSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20, 5));

        cbShape.getItems().addAll("Circle", "Rectangle");
        setPropertyPaneDisabled(true);

        // Clip canvasPane để ẩn nội dung vượt ra ngoài bounds
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvasPane.widthProperty());
        clip.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.setClip(clip);

        // Setup gridCanvas và cho nó nhận mouseTransparent = true
        gridCanvas = new Canvas();
        gridCanvas.setMouseTransparent(true);
        gridCanvas.widthProperty().bind(canvasPane.widthProperty());
        gridCanvas.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.getChildren().add(0, gridCanvas);
        redrawGrid();

        // Khi resize canvasPane, redraw grid và clamp nodes
        canvasPane.widthProperty().addListener((obs, o, n) -> {
            redrawGrid();

//            clampAllNodes();
        });
        canvasPane.heightProperty().addListener((obs, o, n) -> {
            redrawGrid();

//            clampAllNodes();
        });

        // Snap-to-grid binding
        chkSnap.selectedProperty().bindBidirectional(snapToGrid);
        spGridSize.valueProperty().addListener((obs, old, ni) -> {
            if (ni != null) {
                gridSize.set(ni);
                redrawGrid();
                if(selectedNodeView != null) clampNode(selectedNodeView.getModel());
//                clampAllNodes();
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

        // Mouse moved trên canvasPane: hiển thị nội dung coords
        canvasPane.addEventFilter(MouseEvent.MOUSE_MOVED, evt -> {
            Point2D p = contentGroup.sceneToLocal(evt.getSceneX(), evt.getSceneY());
            lblCoordinates.setText(String.format("Coordinates: (%.1f, %.1f)", p.getX(), p.getY()));
        });
        // Click lên background canvasPane để clear selection
        canvasPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            if (evt.getTarget() == canvasPane) {
                clearSelection();
            }
        });

        // Add/Delete node
        btnAddCircle.setOnAction(evt -> addNode(NodeType.Circle));
        btnAddRect.setOnAction(evt -> addNode(NodeType.Rectangle));
        btnDelete.setOnAction(evt -> {
            if (selectedNodeView != null) deleteSelectedNode();
            else lblStatus.setText("No node selected");
        });

        // Property pane listeners
        tfLabel.textProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null) selectedNodeView.getModel().setText(ni);
        });
        spFontSize.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) selectedNodeView.getModel().setFontSize(ni);
        });
        cpFill.setOnAction(evt -> {
            if (selectedNodeView != null) selectedNodeView.getModel().setFillColor(cpFill.getValue());
        });
        cpStroke.setOnAction(evt -> {
            if (selectedNodeView != null) selectedNodeView.getModel().setStrokeColor(cpStroke.getValue());
        });
        spStrokeWidth.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null) selectedNodeView.getModel().setStrokeWidth(ni);
        });
        cbShape.valueProperty().addListener((obs, old, ni) -> {
            if (selectedNodeView != null && ni != null && !ni.equals(old)) {
                changeShapeOfSelected(ni);
            }
        });
    }

    /** Vẽ grid tĩnh */
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

    /** Áp dụng zoom: chỉ scale contentGroup */
    private void applyZoom() {
        contentGroup.setScaleX(zoomFactor);
        contentGroup.setScaleY(zoomFactor);
        updateZoomLabel();
        if (selectedNodeView != null) {
            clampNode(selectedNodeView.getModel());
        }
//        clampAllNodes();

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


        //debug
        model.xProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[Model] xProperty changed for id=" + model.getId()
                    + " from " + String.format("%.1f", oldVal.doubleValue())
                    + " to " + String.format("%.1f", newVal.doubleValue()));
        });
        model.yProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[Model] yProperty changed for id=" + model.getId()
                    + " from " + String.format("%.1f", oldVal.doubleValue())
                    + " to " + String.format("%.1f", newVal.doubleValue()));
        });

        NodeView view = new NodeView(model, canvasPane, contentGroup, snapToGrid, gridSize, () -> zoomFactor);
        // Gắn handler chọn node
        view.setOnMouseClicked(evt -> {
            selectNodeView(view);
            evt.consume();
        });

        contentGroup.getChildren().add(view);
        nodeViewMap.put(model.getId(), view);

        // Đặt vị trí giữa canvas theo zoom
        Platform.runLater(() -> {
            double viewW = canvasPane.getWidth();
            double viewH = canvasPane.getHeight();
            double halfW = model.getWidth() / 2.0;
            double halfH = (type == NodeType.Circle) ? halfW : (model.getHeight() / 2.0);
            double centerX, centerY;
            if (zoomFactor >= 1.0) {
                centerX = clamp(viewW / zoomFactor / 2.0, halfW, viewW / zoomFactor - halfW);
                centerY = clamp(viewH / zoomFactor / 2.0, halfH, viewH / zoomFactor - halfH);
            } else {
                centerX = clamp(viewW / 2.0, halfW, viewW - halfW);
                centerY = clamp(viewH / 2.0, halfH, viewH - halfH);
            }
            if (snapToGrid.get()) {
                centerX = Math.round(centerX / gridSize.get()) * gridSize.get();
                centerY = Math.round(centerY / gridSize.get()) * gridSize.get();
                if (zoomFactor >= 1.0) {
                    centerX = clamp(centerX, halfW, viewW / zoomFactor - halfW);
                    centerY = clamp(centerY, halfH, viewH / zoomFactor - halfH);
                } else {
                    centerX = clamp(centerX, halfW, viewW - halfW);
                    centerY = clamp(centerY, halfH, viewH - halfH);
                }
            }
            model.setX(centerX);
            model.setY(centerY);
            lblStatus.setText("Added " + type.name());
        });
    }

    /** Chọn node: highlight và populate properties */
    private void selectNodeView(NodeView view) {
        if (selectedNodeView != null) selectedNodeView.setSelected(false);
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
        } else {
            cbShape.setValue("Rectangle");
        }
    }

    /** Clear selection */
    private void clearSelection() {
        if (selectedNodeView != null) selectedNodeView.setSelected(false);
        selectedNodeView = null;
        lblNoSelection.setText("No selection");
        setPropertyPaneDisabled(true);
        lblStatus.setText("Ready");
    }

    /** Xóa node đã chọn */
    private void deleteSelectedNode() {
        if (selectedNodeView != null) {
            NodeModel m = selectedNodeView.getModel();
            contentGroup.getChildren().remove(selectedNodeView);
            nodeViewMap.remove(m.getId());
            selectedNodeView = null;
            clearSelection();
        }
    }

    /** Đổi shape node đã chọn */
    private void changeShapeOfSelected(String shapeName) {
        if (selectedNodeView == null) return;
        NodeModel oldModel = selectedNodeView.getModel();
        NodeType oldType = oldModel.getType();
        NodeType newType = shapeName.equals("Circle") ? NodeType.Circle : NodeType.Rectangle;
        if (oldType == newType) return;

        NodeModel newModel = NodeFactory.createNode(newType);
        newModel.setX(oldModel.getX());
        newModel.setY(oldModel.getY());
        newModel.setWidth(oldModel.getWidth());
        if (newType == NodeType.Rectangle) newModel.setHeight(oldModel.getHeight());
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

    /** Clamp 1 node model sang trong vùng visible */
    private void clampNode(NodeModel m) {
        double viewW = canvasPane.getWidth();
        double viewH = canvasPane.getHeight();
        double halfW = m.getWidth()/2.0;
        double halfH = (m.getType()==NodeType.Circle)?halfW:(m.getHeight()/2.0);
        double clampedX, clampedY;
        if (zoomFactor >= 1.0) {
            clampedX = clamp(m.getX(), halfW, viewW/zoomFactor - halfW);
            clampedY = clamp(m.getY(), halfH, viewH/zoomFactor - halfH);
        } else {
            clampedX = clamp(m.getX(), halfW, viewW - halfW);
            clampedY = clamp(m.getY(), halfH, viewH - halfH);
        }
        if (snapToGrid.get()) {
            clampedX = Math.round(clampedX/gridSize.get())*gridSize.get();
            clampedY = Math.round(clampedY/gridSize.get())*gridSize.get();
            if (zoomFactor >= 1.0) {
                clampedX = clamp(clampedX, halfW, viewW/zoomFactor - halfW);
                clampedY = clamp(clampedY, halfH, viewH/zoomFactor - halfH);
            } else {
                clampedX = clamp(clampedX, halfW, viewW - halfW);
                clampedY = clamp(clampedY, halfH, viewH - halfH);
            }
        }
        m.setX(clampedX);
        m.setY(clampedY);
    }

    /** Clamp tất cả node để không vượt ra ngoài visible region khi zoom in/out */
//    private void clampAllNodes() {
//        double viewW = canvasPane.getWidth();
//        double viewH = canvasPane.getHeight();
//        for (NodeView nv : nodeViewMap.values()) {
//            NodeModel m = nv.getModel();
//
//            double oldX = m.getX(), oldY = m.getY();
//
//
//            double halfW = m.getWidth() / 2.0;
//            double halfH = (m.getType() == NodeType.Circle) ? halfW : (m.getHeight() / 2.0);
//            double clampedX, clampedY;
//            if (zoomFactor >= 1.0) {
//                clampedX = clamp(m.getX(), halfW, viewW / zoomFactor - halfW);
//                clampedY = clamp(m.getY(), halfH, viewH / zoomFactor - halfH);
//            } else {
//                clampedX = clamp(m.getX(), halfW, viewW - halfW);
//                clampedY = clamp(m.getY(), halfH, viewH - halfH);
//            }
//            if (snapToGrid.get()) {
//                clampedX = Math.round(clampedX / gridSize.get()) * gridSize.get();
//                clampedY = Math.round(clampedY / gridSize.get()) * gridSize.get();
//                if (zoomFactor >= 1.0) {
//                    clampedX = clamp(clampedX, halfW, viewW / zoomFactor - halfW);
//                    clampedY = clamp(clampedY, halfH, viewH / zoomFactor - halfH);
//                } else {
//                    clampedX = clamp(clampedX, halfW, viewW - halfW);
//                    clampedY = clamp(clampedY, halfH, viewH - halfH);
//                }
//            }
//            if (clampedX != oldX || clampedY != oldY) {
//                System.out.println("[clampAllNodes] Adjust node id=" + m.getId()
//                        + " from (" + String.format("%.1f", oldX) + "," + String.format("%.1f", oldY) + ")"
//                        + " to (" + String.format("%.1f", clampedX) + "," + String.format("%.1f", clampedY) + ")");
//            }
//            m.setX(clampedX);
//            m.setY(clampedY);
//        }
//    }

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
        // Snap và Grid size luôn cho chỉnh
        chkSnap.setDisable(false);
        spGridSize.setDisable(false);
    }
}
