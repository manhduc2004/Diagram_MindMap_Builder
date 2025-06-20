package com.example.diagram_mindmap_builder.persistence;

import com.example.diagram_mindmap_builder.model.GraphModel;
import com.example.diagram_mindmap_builder.model.NodeModel;
import com.example.diagram_mindmap_builder.model.EdgeModel;
import com.example.diagram_mindmap_builder.model.NodeType;
import javafx.scene.paint.Color;

public class SvgExporter {
    public static String exportGraph(GraphModel graphModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\">\n");
        // Edges dưới nodes
        for (EdgeModel e : graphModel.getEdges()) {
            NodeModel s = e.getSource(), t = e.getTarget();
            sb.append(String.format("<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#000\" stroke-width=\"2\" />\n",
                    s.getX(), s.getY(), t.getX(), t.getY()));
        }
        // Nodes
        for (NodeModel m : graphModel.getNodes()) {
            String fill = colorToHex(m.getFillColor());
            String stroke = colorToHex(m.getStrokeColor());
            if (m.getType() == NodeType.Circle) {
                double r = m.getWidth()/2;
                sb.append(String.format("<circle cx=\"%.1f\" cy=\"%.1f\" r=\"%.1f\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\" />\n",
                        m.getX(), m.getY(), r, fill, stroke, m.getStrokeWidth()));
            } else {
                double w = m.getWidth(), h = m.getHeight();
                double x = m.getX() - w/2, y = m.getY() - h/2;
                sb.append(String.format("<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\" />\n",
                        x, y, w, h, fill, stroke, m.getStrokeWidth()));
            }
            if (m.getText() != null && !m.getText().isEmpty()) {
                sb.append(String.format("<text x=\"%.1f\" y=\"%.1f\" font-size=\"%.1f\" text-anchor=\"middle\" alignment-baseline=\"middle\">%s</text>\n",
                        m.getX(), m.getY(), m.getFontSize(), escapeXml(m.getText())));
            }
        }
        sb.append("</svg>");
        return sb.toString();
    }
    private static String colorToHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }
    private static String escapeXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
