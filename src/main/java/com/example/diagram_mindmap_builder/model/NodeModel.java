package com.example.diagram_mindmap_builder.model;

import javafx.beans.property.*;
import javafx.scene.paint.*;
import java.util.*;

public abstract class NodeModel {
    private final String id;
    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();
    private final DoubleProperty width = new SimpleDoubleProperty();
    private final DoubleProperty height = new SimpleDoubleProperty();
    private final ObjectProperty<Color> fillColor = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    private final ObjectProperty<Color> strokeColor = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(1.0);
    private final StringProperty text = new SimpleStringProperty("");
    private final DoubleProperty fontSize = new SimpleDoubleProperty(12.0);

    public NodeModel(){
        this.id = UUID.randomUUID().toString();
        width.set(80);
        height.set(40);
    }

    //id
    public String getId() {
        return id;
    }

    // x property
    public DoubleProperty xProperty() {
        return x;
    }
    public double getX() {
        return x.get();
    }
    public void setX(double value) {
        x.set(value);
    }

    // y property
    public double getY() {
        return y.get();
    }
    public DoubleProperty yProperty() {
        return y;
    }
    public void setY(double value) {
        y.set(value);
    }

    // width property
    public double getWidth() {
        return width.get();
    }
    public DoubleProperty widthProperty() {
        return width;
    }
    public void setWidth(double value) {
        width.set(value);
    }

    // height value
    public double getHeight() {
        return height.get();
    }
    public DoubleProperty heightProperty() {
        return height;
    }
    public void setHeight(double value) {
        height.set(value);
    }

    // fill color property
    public Color getFillColor() {
        return fillColor.get();
    }
    public ObjectProperty<Color> fillColorProperty() {
        return fillColor;
    }
    public void setFillColor(Color value) {
        fillColor.set(value);
    }

    // stroke color
    public Color getStrokeColor() {
        return strokeColor.get();
    }
    public ObjectProperty<Color> strokeColorProperty() {
        return strokeColor;
    }
    public void setStrokeColor(Color value){
        strokeColor.set(value);
    }

    // stroke width
    public double getStrokeWidth() {
        return strokeWidth.get();
    }
    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }
    public void setStrokeWidth(double width){
        strokeWidth.set(width);
    }

    // text property
    public String getText() {
        return text.get();
    }
    public StringProperty textProperty() {
        return text;
    }
    public void setText(String value) {
        text.set(value);
    }

    //font size
    public double getFontSize() {
        return fontSize.get();
    }
    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }
    public void setFontSize(double value) {
        fontSize.set(value);
    }

    public abstract NodeType getType();

    @Override
    public String toString(){
        return "NodeModel{" +
                "id='" + id + '\'' +
                ", type=" + getType() +
                ", x=" + getX() +
                ", y=" + getY() +
                ", w=" + getWidth() +
                ", h=" + getHeight() +
                ", text=" + getText() +
                '}';
    }
}
