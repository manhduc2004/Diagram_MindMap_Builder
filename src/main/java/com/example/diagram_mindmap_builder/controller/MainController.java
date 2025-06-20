package com.example.diagram_mindmap_builder.controller;

import com.example.diagram_mindmap_builder.builder.NodeDirector;
import com.example.diagram_mindmap_builder.command.*;
import com.example.diagram_mindmap_builder.factory.NodeFactory;
import com.example.diagram_mindmap_builder.layout.RadialLayoutStrategy;
import com.example.diagram_mindmap_builder.layout.TreeLayoutStrategy;
import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import com.example.diagram_mindmap_builder.persistence.JSONStrategy;
import com.example.diagram_mindmap_builder.persistence.PersistenceManager;
import com.example.diagram_mindmap_builder.persistence.SvgExporter;
import com.example.diagram_mindmap_builder.persistence.XMLStrategy;
import com.example.diagram_mindmap_builder.ui.NodeTemplate;
import com.example.diagram_mindmap_builder.view.NodeView;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private Pane canvasPane;
    @FXML private Group contentGroup;
    @FXML private ListView<NodeTemplate> templateListView;
    @FXML private Button btnAddCircle, btnAddRect, btnDelete, btnZoomIn, btnZoomOut, btnConnect;
    @FXML private MenuItem menuSaveJson, menuLoadJson, menuSaveXml, menuLoadXml;
    @FXML private MenuItem menuExportPng, menuExportSvg;
    @FXML private MenuItem menuLayoutTree, menuLayoutRadial;
    @FXML private Label lblNoSelection, lblStatus, lblZoomLevel, lblCoordinates;;
    @FXML private TextField tfLabel;
    @FXML private Spinner<Double> spFontSize, spStrokeWidth;
    @FXML private ColorPicker cpFill, cpStroke;
    @FXML private ComboBox<String> cbShape;
    @FXML private CheckBox chkSnap;
    @FXML private Spinner<Integer> spGridSize;
    @FXML private MenuItem menuUndo, menuRedo, menuDelete;

    private GraphModel graphModel;
    private CommandManager commandManager;
    private PersistenceManager persistenceManager;
    private IntegerProperty gridSizePop;
    private Canvas gridCanvas;
    private double zoomFactor = 1.0;

    private final BooleanProperty snapToGrid = new SimpleBooleanProperty(false);
    private final IntegerProperty gridSize = new SimpleIntegerProperty(20);

    private final Map<String, NodeView> nodeViewMap = new HashMap<>();
    private final Map<String, Line> edgeViewMap = new HashMap<>();
    private NodeView selectedNodeView = null;

    private boolean connectMode = false;
    private NodeView connectSource = null;
    @FXML
    public void initialize() {
        graphModel = new GraphModel();
        commandManager = new CommandManager();
        persistenceManager = new PersistenceManager(new JSONStrategy());

        // Thiết lập menu Undo/Redo/Delete
        menuUndo.disableProperty().bind(commandManager.canUndoProperty().not());
        menuRedo.disableProperty().bind(commandManager.canRedoProperty().not());
        menuUndo.setOnAction(e -> {
            commandManager.undo();
            lblStatus.setText("Undid: " + commandManager.peekRedoName());
        });
        menuRedo.setOnAction(e -> {
            commandManager.redo();
            lblStatus.setText("Redid: " + commandManager.peekUndoName());
        });
        menuDelete.disableProperty().bind(Bindings.createBooleanBinding(() -> selectedNodeView == null));
        menuDelete.setOnAction(e -> {
            if (selectedNodeView != null) {
                DeleteNodeCommand cmd = new DeleteNodeCommand(graphModel, selectedNodeView.getModel());
                commandManager.executeCommand(cmd);
                selectedNodeView = null;
                clearSelection();
                lblStatus.setText("Deleted Node");
            }
        });

        // Debug injection: đảm bảo không null
        System.out.println("canvasPane=" + canvasPane + ", contentGroup=" + contentGroup + ", spFontSize=" + spFontSize);

        gridSizePop = new SimpleIntegerProperty();
        // Spinner factories
        spFontSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(6, 72, 12, 1));
        spStrokeWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 20, 1, 0.5));
        spGridSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20, 5));
        gridSizePop.bind(spGridSize.getValueFactory().valueProperty());

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

        // Listener GraphModel nodes: tạo/remove NodeView
        graphModel.getNodes().addListener((ListChangeListener<NodeModel>) change -> {
            while (change.next()){
                if(change.wasAdded()) {
                    for (NodeModel m : change.getAddedSubList()) {
                        createNodeView(m);
                    }
                }
                if(change.wasRemoved()) {
                    for (NodeModel m : change.getRemoved()) {
                        removeNodeView(m);
                    }
                }
            }
        });
        // Listener GraphModel egdes: tạo/remove EdgeView
        graphModel.getEdges().addListener((ListChangeListener<EdgeModel>) change -> {
            while (change.next()) {
                if(change.wasAdded()) {
                    for (EdgeModel m : change.getAddedSubList()) {
                        createEdgeView(m);
                    }
                }
                if(change.wasRemoved()) {
                    for (EdgeModel m : change.getRemoved()) {
                        removeEdgeView(m);
                    }
                }
            }
        });

        // Add/Delete node
        btnAddCircle.setOnAction(evt -> {
            double cx = computeCenterX();
            double cy = computeCenterY();
            CreateNodeCommand cmd = new CreateNodeCommand(graphModel, NodeType.Circle);
            commandManager.executeCommand(cmd);
            NodeModel newNode = cmd.getNode();
            newNode.setX(cx);
            newNode.setY(cy);
            lblStatus.setText("Added Circle");
        });
        btnAddRect.setOnAction(evt -> {
            double cx = computeCenterX();
            double cy = computeCenterY();
            CreateNodeCommand cmd = new CreateNodeCommand(graphModel, NodeType.Rectangle);
            commandManager.executeCommand(cmd);
            NodeModel newNode = cmd.getNode();
            newNode.setX(cx);
            newNode.setY(cy);
            lblStatus.setText("Added Rectangle");
        });
        btnDelete.setOnAction(evt -> {
            if (selectedNodeView != null) {
                DeleteNodeCommand cmd = new DeleteNodeCommand(graphModel, selectedNodeView.getModel());
                commandManager.executeCommand(cmd);
                selectedNodeView = null;
                clearSelection();
                lblStatus.setText("Deleted Node");
            } else {
                lblStatus.setText("No node selected");

            }
        });
        btnConnect.setOnAction(evt -> {
            connectMode = !connectMode;
            connectSource = null;
            if(connectMode) {
                lblStatus.setText("Connect mode: select source node");
                btnConnect.setText("Connect");
            }else{
                lblStatus.setText("Connect mode canceled");
                btnConnect.setStyle("");
            }
        });

        // Property pane listeners: gom lệnh khi focus lost hoặc action
        tfLabel.focusedProperty().addListener((obs, oldF, newF) -> {
            if (!newF && selectedNodeView != null) {
                String oldText = selectedNodeView.getModel().getText();
                String newText = tfLabel.getText();
                if (!newText.equals(oldText)) {
                    EditTextCommand cmd = new EditTextCommand(selectedNodeView.getModel(), oldText, newText);
                    commandManager.executeCommand(cmd);
                    lblStatus.setText("Edited text");
                }
            }
        });
        spFontSize.focusedProperty().addListener((obs, oldF, newF) -> {
            if (!newF && selectedNodeView != null) {
                double oldVal = selectedNodeView.getModel().getFontSize();
                double newVal = spFontSize.getValue();
                if (newVal != oldVal) {
                    EditFontSizeCommand cmd = new EditFontSizeCommand(selectedNodeView.getModel(), oldVal, newVal);
                    commandManager.executeCommand(cmd);
                    lblStatus.setText("Edited font size");
                }
            }
        });
        cpFill.setOnAction(evt -> {
            if (selectedNodeView != null) {
                Color oldC = selectedNodeView.getModel().getFillColor();
                Color newC = cpFill.getValue();
                if (!newC.equals(oldC)) {
                    EditFillColorCommand cmd = new EditFillColorCommand(selectedNodeView.getModel(), oldC, newC);
                    commandManager.executeCommand(cmd);
                    lblStatus.setText("Edited fill color");
                }
            }
        });
        cpStroke.setOnAction(evt -> {
            if (selectedNodeView != null) {
                Color oldC = selectedNodeView.getModel().getStrokeColor();
                Color newC = cpStroke.getValue();
                if (!newC.equals(oldC)) {
                    EditStrokeColorCommand cmd = new EditStrokeColorCommand(selectedNodeView.getModel(), oldC, newC);
                    commandManager.executeCommand(cmd);
                    lblStatus.setText("Edited stroke color");
                }
            }
        });
        spStrokeWidth.focusedProperty().addListener((obs, oldF, newF) -> {
            if (!newF && selectedNodeView != null) {
                double oldVal = selectedNodeView.getModel().getStrokeWidth();
                double newVal = spStrokeWidth.getValue();
                if (newVal != oldVal) {
                    EditStrokeWidthCommand cmd = new EditStrokeWidthCommand(selectedNodeView.getModel(), oldVal, newVal);
                    commandManager.executeCommand(cmd);
                    lblStatus.setText("Edited stroke width");
                }
            }
        });
        cbShape.valueProperty().addListener((obs, oldV, newV) -> {
            if (selectedNodeView != null && newV != null && !newV.equals(oldV)) {
                NodeType newType = newV.equals("Circle") ? NodeType.Circle : NodeType.Rectangle;
                ChangeShapeCommand cmd = new ChangeShapeCommand(graphModel, selectedNodeView.getModel(), newType);
                commandManager.executeCommand(cmd);
                lblStatus.setText("Changed shape");
            }
        });

        // Setup Template Library
        if (templateListView != null) {
            NodeTemplate.setupTemplateListView(templateListView, canvasPane, contentGroup, graphModel, commandManager, lblStatus);
        }

        // FileChooser Save/Load JSON
        menuSaveJson.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files","*.json"));
            File file = fc.showSaveDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                persistenceManager.setSerializer(new JSONStrategy());
                try {
                    persistenceManager.save(graphModel, file);
                    lblStatus.setText("Saved JSON: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error saving JSON: " + e.getMessage());
                }
            }
        });
        menuLoadJson.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files","*.json"));
            File file = fc.showOpenDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                persistenceManager.setSerializer(new JSONStrategy());
                try {
                    persistenceManager.load(graphModel, file, () -> clearAll());
                    lblStatus.setText("Loaded JSON: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error loading JSON: " + e.getMessage());
                }
            }
        });
        // Save/Load XML via Jackson XML
        menuSaveXml.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files","*.xml"));
            File file = fc.showSaveDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                persistenceManager.setSerializer(new XMLStrategy());
                try {
                    persistenceManager.save(graphModel, file);
                    lblStatus.setText("Saved XML: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error saving XML: " + e.getMessage());
                }
            }
        });
        menuLoadXml.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files","*.xml"));
            File file = fc.showOpenDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                persistenceManager.setSerializer(new XMLStrategy());
                try {
                    persistenceManager.load(graphModel, file, () -> clearAll());
                    lblStatus.setText("Loaded XML: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error loading XML: " + e.getMessage());
                }
            }
        });

        // Export PNG
        menuExportPng.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files","*.png"));
            File file = fc.showSaveDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                WritableImage image = contentGroup.snapshot(new SnapshotParameters(), null);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    lblStatus.setText("Exported PNG: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error exporting PNG: " + e.getMessage());
                }
            }
        });
        // Export SVG
        menuExportSvg.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files","*.svg"));
            File file = fc.showSaveDialog(canvasPane.getScene().getWindow());
            if (file != null) {
                try {
                    String svg = SvgExporter.exportGraph(graphModel);
                    // Thay Files.writeString bằng BufferedWriter để tương thích Java < 11
                    Path path = file.toPath();
                    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                        writer.write(svg);
                    }
                    lblStatus.setText("Exported SVG: " + file.getName());
                } catch (IOException e) {
                    lblStatus.setText("Error exporting SVG: " + e.getMessage());
                }
            }
        });

        // Layout menu
        menuLayoutTree.setOnAction(evt -> applyLayout(new TreeLayoutStrategy()));
        menuLayoutRadial.setOnAction(evt -> applyLayout(new RadialLayoutStrategy()));
    }

    private void clearAll() {

        graphModel.getNodes().clear();
        graphModel.getEdges().clear();

        commandManager.clear();

    }

    private void applyLayout(com.example.diagram_mindmap_builder.layout.LayoutStrategy strategy) {
        Map<NodeModel, Point2D> oldPos = new java.util.HashMap<>();
        for (NodeModel node : graphModel.getNodes()) oldPos.put(node, new Point2D(node.getX(), node.getY()));
        Map<NodeModel, Point2D> newPos = strategy.applyLayout(graphModel);
        BatchMoveCommand cmd = new BatchMoveCommand(oldPos, newPos);
        commandManager.executeCommand(cmd);
        lblStatus.setText("Applied layout: " + strategy.getClass().getSimpleName());
    }

    // Tạo NodeView và add vào contentGroup khi GraphModel thêm node
    private void createNodeView(NodeModel model) {
        NodeView view = new NodeView(model, canvasPane, contentGroup, snapToGrid, gridSizePop, () -> zoomFactor);
        view.setOnMouseClicked(evt -> {
            if (connectMode) {
                handleConnectClick(view);
                evt.consume();
                return;
            }
            selectNodeView(view);
            evt.consume();
        });

        view.addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
            view.getProperties().put("oldX", model.getX());
            view.getProperties().put("oldY", model.getY());
        });
        view.addEventHandler(MouseEvent.MOUSE_RELEASED, evt -> {
            double oldX = (double) view.getProperties().getOrDefault("oldX", model.getX());
            double oldY = (double) view.getProperties().getOrDefault("oldY", model.getY());
            double newX = model.getX();
            double newY = model.getY();
            if (oldX != newX || oldY != newY) {
                MoveNodeCommand cmd = new MoveNodeCommand(model, oldX, oldY, newX, newY);
                commandManager.executeCommand(cmd);
                lblStatus.setText("Moved Node");
            }
        });
        contentGroup.getChildren().add(view);
        nodeViewMap.put(model.getId(), view);
    }

    private void removeNodeView(NodeModel model) {
        NodeView view = nodeViewMap.remove(model.getId());
        if (view != null) {
            contentGroup.getChildren().remove(view);
            if (selectedNodeView == view) {
                selectedNodeView = null;
                clearSelection();
            }
        }
    }

    // Xử lý click khi trong connectMode
    private void handleConnectClick(NodeView clickedView) {
        if (connectSource == null) {
            connectSource = clickedView;
            connectSource.setSelected(true);
            lblStatus.setText("Source selected, now select target");
        } else {
            if (clickedView == connectSource) {
                // Bỏ chọn source
                connectSource.setSelected(false);
                connectSource = null;
                lblStatus.setText("Select source node");
            } else {
                // Chọn target, thực thi CreateEdgeCommand
                NodeModel sourceModel = connectSource.getModel();
                NodeModel targetModel = clickedView.getModel();
                CreateEdgeCommand cmd = new CreateEdgeCommand(graphModel, sourceModel, targetModel);
                commandManager.executeCommand(cmd);
                lblStatus.setText("Connected nodes");
                // Reset trạng thái connect
                connectSource.setSelected(false);
                connectSource = null;
                connectMode = false;
                btnConnect.setStyle("");
            }
        }
    }

    // Tạo EdgeView (Line) khi GraphModel thêm edge
    private void createEdgeView(EdgeModel edge) {
        NodeModel src = edge.getSource();
        NodeModel tgt = edge.getTarget();
        Line line = new Line();
        // Bind start/end tới model.x/y
        line.startXProperty().bind(src.xProperty());
        line.startYProperty().bind(src.yProperty());
        line.endXProperty().bind(tgt.xProperty());
        line.endYProperty().bind(tgt.yProperty());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        // Thêm vào index 0 để nằm dưới NodeView
        contentGroup.getChildren().add(0, line);
        edgeViewMap.put(edge.getId(), line);
        // Context menu để xóa edge
        line.setOnMouseClicked(evt -> {
            if (evt.isSecondaryButtonDown()) {
                ContextMenu menu = new ContextMenu();
                MenuItem miDel = new MenuItem("Delete Connection");
                miDel.setOnAction(ae -> {
                    DeleteEdgeCommand delCmd = new DeleteEdgeCommand(graphModel, edge);
                    commandManager.executeCommand(delCmd);
                    lblStatus.setText("Deleted Edge");
                });
                menu.getItems().add(miDel);
                menu.show(line, evt.getScreenX(), evt.getScreenY());
                evt.consume();
            }
        });
    }

    private void removeEdgeView(EdgeModel edge) {
        Line line = edgeViewMap.remove(edge.getId());
        if (line != null) {
            contentGroup.getChildren().remove(line);
        }
    }

    // Xử lý chọn NodeView
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
        } else {
            cbShape.setValue("Rectangle");
        }
    }

    private void clearSelection() {
        if (selectedNodeView != null) {
            selectedNodeView.setSelected(false);
        }
        selectedNodeView = null;
        lblNoSelection.setText("No selection");
        setPropertyPaneDisabled(true);
        lblStatus.setText("Ready");
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

    // Tính tâm canvas để đặt node mới
    private double computeCenterX() {
        double viewW = canvasPane.getWidth();
        double halfW = 40; // mặc định bán kính/tựa node
        if (zoomFactor >= 1.0) {
            return clamp(viewW / zoomFactor / 2.0, halfW, viewW / zoomFactor - halfW);
        } else {
            return clamp(viewW / 2.0, halfW, viewW - halfW);
        }
    }
    private double computeCenterY() {
        double viewH = canvasPane.getHeight();
        double halfH = 20;
        if (zoomFactor >= 1.0) {
            return clamp(viewH / zoomFactor / 2.0, halfH, viewH / zoomFactor - halfH);
        } else {
            return clamp(viewH / 2.0, halfH, viewH - halfH);
        }
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
