package com.example.diagram_mindmap_builder.model;

import com.example.diagram_mindmap_builder.factory.NodeFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NodeFactoryTest {
    @Test
    public void testCreateNode(){
        NodeModel node = NodeFactory.createNode(NodeType.Circle);

        // đảm bảo Factory thực sự trả về một object, không phải null
        assertNotNull(node, "Node không được null");

        // đảm bảo rằng factory thực tế object có đúng là CircleNodeModel không
        assertTrue(node instanceof CircleNodeModel, "Phải là CircleNodeModel");

        // đảm bảo rằng getType() trả về đúng NodeType tương ứng
        assertEquals(NodeType.Circle, node.getType());

        // Kiểm tra default properties
        assertEquals(80.0, node.getWidth(), "Default width");
        // x,y mặc định 0
        assertEquals(0.0, node.getX());
        assertEquals(0.0, node.getY());
    }

    @Test
    public void testCreateRectangleNode() {
        NodeModel node = NodeFactory.createNode(NodeType.Rectangle);
        assertNotNull(node);
        assertTrue(node instanceof RectangleNodeModel);
        assertEquals(NodeType.Rectangle, node.getType());
        assertEquals(80.0, node.getWidth());
        assertEquals(40.0, node.getHeight());
    }
}
