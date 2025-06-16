package com.example.diagram_mindmap_builder.view;

import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.util.function.Supplier;

import static org.controlsfx.tools.Utils.clamp;

public class NodeView extends StackPane{
    private final NodeModel model;
    private final Pane canvasPane;
    private final javafx.scene.Group contentGroup;
    private final BooleanProperty snapToGrid;
    private final IntegerProperty gridSize;
    private final Supplier<Double> zoomSupplier;
    private double dragOffsetX;
    private double dragOffsetY;

    public NodeView(NodeModel model, Pane canvasPane, javafx.scene.Group contentGroup,
                    BooleanProperty snapToGrid, IntegerProperty gridSize,
                    Supplier<Double> zoomSupplier){
        this.model = model;
        this.canvasPane = canvasPane;
        this.contentGroup = contentGroup;
        this.snapToGrid = snapToGrid;
        this.gridSize = gridSize;
        this.zoomSupplier = zoomSupplier;

        this.setAlignment(Pos.CENTER);

        Shape shape;
        if(model.getType() == NodeType.Circle){
            this.prefWidthProperty().bind(model.widthProperty());
            this.prefHeightProperty().bind(model.heightProperty());
            this.minWidthProperty().bind(model.widthProperty());
            this.minHeightProperty().bind(model.heightProperty());

            Circle circle = new Circle();
            circle.radiusProperty().bind(model.widthProperty().divide(2));
            shape = circle;
        }
        else {
            this.prefWidthProperty().bind(model.widthProperty());
            this.prefHeightProperty().bind(model.heightProperty());
            this.minWidthProperty().bind(model.widthProperty());
            this.minHeightProperty().bind(model.heightProperty());

            Rectangle rect = new Rectangle();
            rect.widthProperty().bind(model.widthProperty());
            rect.heightProperty().bind(model.heightProperty());
            shape = rect;
        }

        //Bind style
        shape.fillProperty().bind(model.fillColorProperty());
        shape.strokeProperty().bind(model.strokeColorProperty());
        shape.strokeWidthProperty().bind(model.strokeWidthProperty());

        //tạo label
        Text label = new Text();
        label.textProperty().bind(model.textProperty());
        model.fontSizeProperty().addListener((obs, old, ni) -> label.setFont(Font.font(ni.doubleValue())));
        label.setFont(Font.font(model.getFontSize()));

        this.getChildren().addAll(shape, label);

        if(model.getType() == NodeType.Circle){
            this.layoutXProperty().bind(model.xProperty().subtract(model.widthProperty().divide(2)));
            this.layoutYProperty().bind(model.yProperty().subtract(model.heightProperty().divide(2)));
        } else {
            this.layoutXProperty().bind(model.xProperty().subtract(model.widthProperty().divide(2)));
            this.layoutYProperty().bind(model.yProperty().subtract(model.heightProperty().divide(2)));
        }

        this.setOnMousePressed(event -> {
            this.setScaleX(1.05);
            this.setScaleY(1.05);
        });
        this.setOnMouseExited(event -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });

        enableDrag();
    }

    private void enableDrag() {
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            Point2D parentCoord = canvasPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            dragOffsetX = parentCoord.getX() - model.getX();
            dragOffsetY = parentCoord.getY() - model.getY();
            event.consume();
        });

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
            Point2D p = contentGroup.sceneToLocal(evt.getSceneX(), evt.getSceneY());
            double rawX = p.getX() - dragOffsetX;
            double rawY = p.getY() - dragOffsetY;

            double zoom = zoomSupplier.get();
            double contentW = canvasPane.getWidth() / zoom;
            double contentH = canvasPane.getHeight() / zoom;
            double halfW = model.getWidth() / 2.0;
            double halfH = (model.getType() == NodeType.Circle) ? halfW : (model.getHeight() / 2.0);

            double clampedX = clamp(rawX, halfW, contentW - halfW);
            double clampedY = clamp(rawY, halfH, contentH - halfH);

            if (snapToGrid.get()) {
                int gs = gridSize.get();
                clampedX = Math.round(clampedX / gs) * gs;
                clampedY = Math.round(clampedY / gs) * gs;
                clampedX = clamp(clampedX, halfW, contentW - halfW);
                clampedY = clamp(clampedY, halfH, contentH - halfH);
            }

            model.setX(clampedX);
            model.setY(clampedY);
            evt.consume();
        });
    }

    private double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public NodeModel getModel() {
        return model;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            // Ví dụ dùng DropShadow highlight
            DropShadow ds = new DropShadow();
            ds.setColor(Color.DODGERBLUE);
            ds.setRadius(10);
            this.setEffect(ds);
        } else {
            this.setEffect(null);
        }
    }
}
