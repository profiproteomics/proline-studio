package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import static fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode.m_font;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.LinkedList;
import javax.swing.JPopupMenu;


/**
 *
 * @author JM235353
 */
public class GraphGroup extends AbstractGraphObject {

    private static final int DELTA_PIXEL_TOP = 50;
    private static final int DELTA_PIXEL = 25;
    
    private LinkedList<GraphNode> m_groupedGraphNodes = new LinkedList<>();
    
    private final String m_groupName;
    
    private int m_xMin = 0;
    private int m_yMin = 0;
    private int m_xMax = 0;
    private int m_yMax = 0;
    

    
    public GraphGroup(String groupName) {
        super(TypeGraphObject.GROUP);
        m_groupName = groupName;
    }
    
    public String getGroupName() {
        return m_groupName;
    }
    
    public void addObject(GraphNode graphNode ){
        m_groupedGraphNodes.add(graphNode);
        graphNode.setGroup(this);
        calculatePosition();
    }
    
    public void removeObject(GraphNode graphNode) {
        m_groupedGraphNodes.remove(graphNode);
        calculatePosition();
    }
    
    public void objectMoved() {
        calculatePosition();
    }
    
    private void calculatePosition() {
        m_xMin = Integer.MAX_VALUE;
        m_yMin = Integer.MAX_VALUE;
        m_xMax = 0;
        m_yMax = 0;
        for (GraphNode graphNode : m_groupedGraphNodes) {
            int x1 = graphNode.getX();
            if (x1 < m_xMin) {
                m_xMin = x1;
            }
            int y1 = graphNode.getY();
            if (y1 < m_yMin) {
                m_yMin = y1;
            }

            int x2 = graphNode.getXEnd();
            if (x2 > m_xMax) {
                m_xMax = x2;
            }
            int y2 = graphNode.getYEnd();
            if (y2 > m_yMax) {
                m_yMax = y2;
            }
        }

        m_xMin -= DELTA_PIXEL;
        m_yMin -= DELTA_PIXEL_TOP;
        m_xMax += DELTA_PIXEL;
        m_yMax += DELTA_PIXEL;
    }
    
    private void initFonts(Graphics g) {
        if (m_font != null) {
            return;
        }

        m_font = new Font(" TimesRoman ", Font.PLAIN, 11);
        m_fontBold = m_font.deriveFont(Font.BOLD);

        FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
        FontMetrics metricsPlain = g.getFontMetrics(m_font);

        m_hgtBold = metricsBold.getHeight();
        m_ascentBold = metricsBold.getAscent();

        m_hgtPlain = metricsPlain.getHeight();
        //m_ascentPlain = metricsPlain.getAscent();
    }
    
    
    @Override
    public void draw(Graphics g) {
        
        if (m_groupedGraphNodes.isEmpty()) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g;
                
        
        initFonts(g);
        
        //g2.setPaint(gradient);
        //g.fillRoundRect(m_x, m_y, WIDTH, HEIGHT, 8, 8);
        
        //g.setColor(getFrameColor());
        g.setColor(Color.gray);
        
        
        Stroke previousStroke = g2.getStroke();
        BasicStroke stroke = m_selected ? STROKE_SELECTED : STROKE_NOT_SELECTED;
        g2.setStroke(stroke);
        g.drawRoundRect(m_xMin, m_yMin, m_xMax-m_xMin, m_yMax-m_yMin, 8, 8);
        g2.setStroke(previousStroke);
        
        FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
        int stringWidth = metricsBold.stringWidth(m_groupName);
        
        int xText = m_xMin+(m_xMax-m_xMin-stringWidth)/2;
        int yText = m_yMin-m_hgtBold;

        
        g.setFont(m_fontBold);
        g.setColor(Color.black);
        g.drawString(m_groupName, xText, yText);

    }

    @Override
    public AbstractGraphObject inside(int x, int y) {
        if ((x>=m_xMin) && (y>=m_yMin) && (x<=m_xMax) && (y<=m_yMin)) {
            return this;
        }
        return null;
    }

    @Override
    public void move(int dx, int dy) {
        // not movable for the moment
    }

    @Override
    public void delete() {
        // not deletable for the moment
    }

    @Override
    public JPopupMenu createPopup(GraphPanel panel) {
        // no popup for themoment
        return null;
    }

    
}
