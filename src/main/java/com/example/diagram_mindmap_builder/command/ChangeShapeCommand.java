package com.example.diagram_mindmap_builder.command;

import com.example.diagram_mindmap_builder.builder.NodeDirector;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.NodeType;

public class ChangeShapeCommand implements Command {
    private final GraphModel graphModel;
    private final NodeModel oldModel;
    private final NodeType newType;
    private NodeModel newModel;
    private int indexInGraph;
    // Lưu các EdgeModel liên quan để cập nhật tham chiếu
    private final java.util.List<com.example.diagram_mindmap_builder.model.EdgeModel> relatedEdges = new java.util.ArrayList<>();

    public ChangeShapeCommand(GraphModel graphModel, NodeModel oldModel, NodeType newType) {
        this.graphModel = graphModel;
        this.oldModel = oldModel;
        this.newType = newType;
    }

    @Override
    public void execute() {
        // Lưu index cũ
        indexInGraph = graphModel.indexOfNode(oldModel);
        if (indexInGraph < 0) {
            // Node không tồn tại trong model
            return;
        }
        // Tạo newModel theo type mới
        newModel = NodeDirector.makeDefault(newType);
        // Copy thuộc tính từ oldModel
        newModel.setX(oldModel.getX());
        newModel.setY(oldModel.getY());
        newModel.setWidth(oldModel.getWidth());
        if (newType == NodeType.Rectangle) {
            // Nếu oldModel là circle thì height được bind width; ở rectangle, set height cũ hoặc mặc định
            newModel.setHeight(oldModel.getHeight());
        }
        newModel.setText(oldModel.getText());
        newModel.setFillColor(oldModel.getFillColor());
        newModel.setStrokeColor(oldModel.getStrokeColor());
        newModel.setStrokeWidth(oldModel.getStrokeWidth());
        newModel.setFontSize(oldModel.getFontSize());

        // Lưu các edge liên quan để update reference
        relatedEdges.clear();
        for (com.example.diagram_mindmap_builder.model.EdgeModel e : graphModel.getEdges()) {
            if (e.getSource() == oldModel || e.getTarget() == oldModel) {
                relatedEdges.add(e);
            }
        }

        // Thay thế oldModel bằng newModel trong GraphModel
        graphModel.removeNode(oldModel);
        graphModel.addNodeAt(indexInGraph, newModel);

        // Cập nhật EdgeModel tham chiếu từ oldModel sang newModel
        for (com.example.diagram_mindmap_builder.model.EdgeModel e : relatedEdges) {
            if (e.getSource() == oldModel) {
                e.setSource(newModel);
            }
            if (e.getTarget() == oldModel) {
                e.setTarget(newModel);
            }
        }
    }

    @Override
    public void undo() {
        if (newModel == null) {
            return;
        }
        // Remove newModel, restore oldModel tại index cũ
        graphModel.removeNode(newModel);
        graphModel.addNodeAt(indexInGraph, oldModel);
        // Khôi phục EdgeModel tham chiếu về oldModel
        for (com.example.diagram_mindmap_builder.model.EdgeModel e : relatedEdges) {
            if (e.getSource() == newModel) {
                e.setSource(oldModel);
            }
            if (e.getTarget() == newModel) {
                e.setTarget(oldModel);
            }
        }
    }

    @Override
    public String getName() {
        return "Change Shape";
    }

    /**
     * Nếu cần, expose newModel để UI có thể select sau khi change shape.
     */
    public NodeModel getNewModel() {
        return newModel;
    }
}
