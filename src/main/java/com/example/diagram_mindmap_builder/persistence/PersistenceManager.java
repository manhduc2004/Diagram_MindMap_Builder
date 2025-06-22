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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistenceManager {
    private static final Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());

    private Serializer serializer;

    public PersistenceManager(Serializer serializer) {
        this.serializer = serializer;
    }

    public void setSerializer(Serializer s) {
        this.serializer = s;
    }

    /**
     * Lưu GraphModel hiện tại vào file.
     */
    public void save(GraphModel graphModel, File file) throws IOException {
        GraphDTO dto = toDTO(graphModel);
        serializer.save(dto, file);
    }

    /**
     * Load từ file, xóa model & views hiện tại qua clearHistoryAndViews, rồi populate graphModel mới.
     */
    public void load(GraphModel graphModel, File file, Runnable clearHistoryAndViews) throws IOException {
        GraphDTO dto = serializer.load(file);
        // Clear UI/history trước
        clearHistoryAndViews.run();
        // Clear model cũ
        clearGraphModel(graphModel);
        // Load model mới
        fromDTO(graphModel, dto);
    }

    /**
     * Chuyển GraphModel sang GraphDTO để serialize.
     * Giả sử NodeDTO, EdgeDTO có các trường public hoặc getter/setter phù hợp.
     */
    private GraphDTO toDTO(GraphModel graphModel) {
        GraphDTO dto = new GraphDTO();
        dto.nodes = new java.util.ArrayList<>();
        dto.edges = new java.util.ArrayList<>();

        for (NodeModel m : graphModel.getNodes()) {
            NodeDTO nd = new NodeDTO();
            try {
                nd.id = m.getId();
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "NodeModel.getId() lỗi: " + ex.getMessage(), ex);
                nd.id = null;
            }
            // type
            NodeType type = m.getType();
            nd.type = (type != null) ? type.name() : null;

            // tọa độ
            nd.x = m.getX();
            nd.y = m.getY();

            // kích thước: width luôn gán; height nếu Circle thì null, else gán
            nd.width = m.getWidth();
            if (type == NodeType.Circle) {
                nd.height = null;
            } else {
                nd.height = m.getHeight();
            }

            // text
            nd.text = m.getText();

            // màu
            nd.fillColor = (m.getFillColor() != null) ? toHex(m.getFillColor()) : null;
            nd.strokeColor = (m.getStrokeColor() != null) ? toHex(m.getStrokeColor()) : null;

            // strokeWidth, fontSize
            nd.strokeWidth = m.getStrokeWidth();
            nd.fontSize = m.getFontSize();

            dto.nodes.add(nd);
        }

        for (EdgeModel e : graphModel.getEdges()) {
            EdgeDTO ed = new EdgeDTO();
            try {
                ed.id = e.getId();
                ed.sourceId = e.getSource().getId();
                ed.targetId = e.getTarget().getId();
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "EdgeModel.getId()/getSource()/getTarget() lỗi: " + ex.getMessage(), ex);
            }
            dto.edges.add(ed);
        }

        return dto;
    }

    /**
     * Tạo lại GraphModel từ GraphDTO. Nếu dto hoặc dto.nodes null thì bỏ qua tương ứng.
     * Giả sử NodeDTO dùng wrapper Double cho x,y,width,height,... để null-check an toàn.
     */
    private void fromDTO(GraphModel graphModel, GraphDTO dto) {
        if (dto == null) {
            LOGGER.warning("GraphDTO is null, không load gì cả.");
            return;
        }
        Map<String, NodeModel> idMap = new HashMap<>();

        // Load nodes
        if (dto.nodes != null) {
            for (NodeDTO nd : dto.nodes) {
                if (nd == null) {
                    LOGGER.warning("Gặp NodeDTO null trong danh sách, bỏ qua.");
                    continue;
                }
                // Kiểm tra type
                NodeType type;
                if (nd.type == null) {
                    LOGGER.warning(() -> String.format("NodeDTO.type is null cho node id=%s, dùng default Circle", nd.id));
                    type = NodeType.Circle;
                } else {
                    try {
                        type = NodeType.valueOf(nd.type);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.log(Level.WARNING, String.format(
                                "NodeDTO.type '%s' không hợp lệ cho node id=%s, dùng default Rectangle", nd.type, nd.id), ex);
                        type = NodeType.Rectangle;
                    }
                }

                // Tạo NodeModel
                NodeModel m;
                try {
                    m = NodeDirector.makeDefault(type);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                            String.format("Không tạo được NodeModel mặc định cho type=%s, node id=%s. Bỏ qua.", type, nd.id), ex);
                    continue;
                }

                // Gán ID cũ qua reflection nếu nd.id != null
                if (nd.id != null) {
                    try {
                        java.lang.reflect.Field idField = NodeModel.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(m, nd.id);
                    } catch (NoSuchFieldException nsf) {
                        LOGGER.fine("NodeModel không có field 'id' để set qua reflection.");
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set id cũ cho NodeModel: " + ex.getMessage(), ex);
                    }
                }

                // Gán tọa độ
                if (nd.x != null && nd.y != null) {
                    try {
                        m.setX(nd.x);
                        m.setY(nd.y);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set X/Y cho node id=" + nd.id, ex);
                    }
                } else {
                    LOGGER.fine("NodeDTO.x hoặc y null cho node id=" + nd.id + ", giữ vị trí mặc định.");
                }

                // Gán kích thước
                if (nd.width != null) {
                    try {
                        m.setWidth(nd.width);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set width cho node id=" + nd.id, ex);
                    }
                } else {
                    LOGGER.fine("NodeDTO.width null cho node id=" + nd.id + ", giữ width mặc định.");
                }
                if (type != NodeType.Circle) {
                    if (nd.height != null) {
                        try {
                            m.setHeight(nd.height);
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Lỗi khi set height cho node id=" + nd.id, ex);
                        }
                    } else {
                        LOGGER.fine("NodeDTO.height null cho node id=" + nd.id + ", giữ height mặc định.");
                    }
                }

                // Gán text
                if (nd.text != null) {
                    try {
                        m.setText(nd.text);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set text cho node id=" + nd.id, ex);
                    }
                }

                // Gán màu
                if (nd.fillColor != null) {
                    try {
                        m.setFillColor(Color.web(nd.fillColor));
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set fillColor cho node id=" + nd.id + ": " + nd.fillColor, ex);
                    }
                }
                if (nd.strokeColor != null) {
                    try {
                        m.setStrokeColor(Color.web(nd.strokeColor));
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set strokeColor cho node id=" + nd.id + ": " + nd.strokeColor, ex);
                    }
                }

                // Gán strokeWidth, fontSize
                if (nd.strokeWidth != null) {
                    try {
                        m.setStrokeWidth(nd.strokeWidth);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set strokeWidth cho node id=" + nd.id, ex);
                    }
                }
                if (nd.fontSize != null) {
                    try {
                        m.setFontSize(nd.fontSize);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set fontSize cho node id=" + nd.id, ex);
                    }
                }

                // Thêm node vào model và map id->node
                graphModel.addNode(m);
                if (nd.id != null) {
                    idMap.put(nd.id, m);
                }
            }
        } else {
            LOGGER.info("GraphDTO.nodes is null hoặc empty, không tạo node nào.");
        }

        // Load edges
        if (dto.edges != null) {
            for (EdgeDTO ed : dto.edges) {
                if (ed == null) {
                    LOGGER.warning("Gặp EdgeDTO null trong danh sách, bỏ qua.");
                    continue;
                }
                if (ed.sourceId == null || ed.targetId == null) {
                    LOGGER.warning("EdgeDTO thiếu sourceId hoặc targetId, bỏ qua edge id=" + ed.id);
                    continue;
                }
                NodeModel src = idMap.get(ed.sourceId);
                NodeModel tgt = idMap.get(ed.targetId);
                if (src == null || tgt == null) {
                    LOGGER.warning(String.format(
                            "Không tìm thấy node source hoặc target cho edge id=%s: sourceId=%s, targetId=%s; bỏ qua.",
                            ed.id, ed.sourceId, ed.targetId));
                    continue;
                }
                EdgeModel e;
                try {
                    e = new EdgeModel(src, tgt);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                            String.format("Lỗi khi tạo EdgeModel cho edge id=%s: %s", ed.id, ex.getMessage()), ex);
                    continue;
                }
                // Gán id cũ nếu có
                if (ed.id != null) {
                    try {
                        java.lang.reflect.Field idField = EdgeModel.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(e, ed.id);
                    } catch (NoSuchFieldException nsf) {
                        LOGGER.fine("EdgeModel không có field 'id' để set qua reflection.");
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Lỗi khi set id cũ cho EdgeModel: " + ex.getMessage(), ex);
                    }
                }
                graphModel.addEdge(e);
            }
        } else {
            LOGGER.info("GraphDTO.edges is null hoặc empty, không tạo edge nào.");
        }
    }

    /**
     * Xóa sạch nodes và edges trong graphModel.
     */
    private void clearGraphModel(GraphModel graphModel) {
        try {
            graphModel.getEdges().clear();
            graphModel.getNodes().clear();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Lỗi khi clear GraphModel: " + ex.getMessage(), ex);
        }
    }

    /**
     * Chuyển Color sang chuỗi hex "#RRGGBB"
     */
    private String toHex(Color c) {
        if (c == null) return null;
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
