package com.example.diagram_mindmap_builder.persistence;
import com.example.diagram_mindmap_builder.builder.NodeDirector;
import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import com.example.diagram_mindmap_builder.model.dto.NodeDTO;
import com.example.diagram_mindmap_builder.model.dto.EdgeDTO;
import com.example.diagram_mindmap_builder.model.dto.GraphDTO;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

public class PersistenceManager {
    private Serializer serializer;
    public PersistenceManager(Serializer serializer) {
        this.serializer = serializer;
    }
    public void setSerializer(Serializer s) {
        this.serializer = s;
    }
    public void save(GraphModel graphModel, File file) throws IOException {
        GraphDTO dto = toDTO(graphModel);
        serializer.save(dto, file);
    }
    public void load(GraphModel graphModel, File file, Runnable clearHistoryAndViews) throws IOException {
        GraphDTO dto = serializer.load(file);
        // Clear existing model and views via callback
        clearHistoryAndViews.run();
        fromDTO(graphModel, dto);
    }
    private GraphDTO toDTO(GraphModel graphModel) {
        GraphDTO dto = new GraphDTO();
        dto.nodes = new java.util.ArrayList<>();
        dto.edges = new java.util.ArrayList<>();
        for (NodeModel m : graphModel.getNodes()) {
            NodeDTO nd = new NodeDTO();
            nd.id = m.getId();
            nd.type = m.getType().name();
            nd.x = m.getX(); nd.y = m.getY();
            nd.width = m.getWidth();
            if (m.getType() == NodeType.Circle) nd.height = null;
            else nd.height = m.getHeight();
            nd.text = m.getText();
            nd.fillColor = toHex(m.getFillColor());
            nd.strokeColor = toHex(m.getStrokeColor());
            nd.strokeWidth = m.getStrokeWidth();
            nd.fontSize = m.getFontSize();
            dto.nodes.add(nd);
        }
        for (EdgeModel e : graphModel.getEdges()) {
            EdgeDTO ed = new EdgeDTO();
            ed.id = e.getId();
            ed.sourceId = e.getSource().getId();
            ed.targetId = e.getTarget().getId();
            dto.edges.add(ed);
        }
        return dto;
    }
    private void fromDTO(GraphModel graphModel, GraphDTO dto) {
        Map<String, NodeModel> idMap = new HashMap<>();
        // Tạo node mới
        for (NodeDTO nd : dto.nodes) {
            NodeType type = NodeType.valueOf(nd.type);
            NodeModel m = NodeDirector.makeDefault(type);
            // Nếu muốn giữ ID cũ, NodeModel cần hỗ trợ setId; nếu không, ID mới, mapping edges sẽ khác
            try {
                java.lang.reflect.Field idField = NodeModel.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(m, nd.id);
            } catch (Exception ex) {
                // bỏ qua
            }
            m.setX(nd.x); m.setY(nd.y);
            m.setWidth(nd.width);
            if (type != NodeType.Circle && nd.height != null) m.setHeight(nd.height);
            m.setText(nd.text);
            m.setFillColor(Color.web(nd.fillColor));
            m.setStrokeColor(Color.web(nd.strokeColor));
            m.setStrokeWidth(nd.strokeWidth);
            m.setFontSize(nd.fontSize);
            graphModel.addNode(m);
            idMap.put(nd.id, m);
        }
        // Tạo edges
        for (EdgeDTO ed : dto.edges) {
            NodeModel src = idMap.get(ed.sourceId);
            NodeModel tgt = idMap.get(ed.targetId);
            if (src != null && tgt != null) {
                EdgeModel e = new EdgeModel(src, tgt);
                try {
                    java.lang.reflect.Field idField = EdgeModel.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(e, ed.id);
                } catch (Exception ex) {}
                graphModel.addEdge(e);
            }
        }
    }
    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}
