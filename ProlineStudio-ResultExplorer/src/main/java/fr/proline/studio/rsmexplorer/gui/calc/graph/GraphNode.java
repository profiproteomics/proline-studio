package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngine;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.windows.WindowManager;

/**
 * Parent Node for all graph nodes
 * @author JM235353
 */
public abstract class GraphNode extends AbstractConnectedGraphObject {

    
    public static final int WIDTH = 60;
    public static final int HEIGHT = 60;
    private static final int MARGIN = 5;
    
    protected LinkedList<GraphConnector> m_inConnectors = null;
    protected GraphConnector m_outConnector = null;
    
   
    protected int m_x = 0;
    protected int m_y = 0;

    protected static Font m_font = null;
    protected static Font m_fontBold = null;
    
    protected static int m_hgtBold;
    protected static int m_hgtPlain;
    protected static int m_ascentBold;
    
    protected GraphPanel m_graphPanel = null;

    public GraphNode(GraphPanel panel) {
        super(TypeGraphObject.GRAPH_NODE);
        m_graphPanel = panel;
    }

    private GraphConnector getFirstFreeConnector() {
        if (m_inConnectors == null) {
            return null;
        }
        
        for (GraphConnector inConnector : m_inConnectors) {
            if (inConnector.isConnected()) {
                continue;
            }
            return inConnector;
        }
        return null;
    }
    
    public void connectTo(GraphNode inGraphNode) {
        GraphConnector inConnector = inGraphNode.getFirstFreeConnector();
        m_outConnector.addConnection(inConnector);
        inConnector.addConnection(m_outConnector);
    }
    
    public boolean hasInConnector() {
        if ((m_inConnectors == null) || (m_inConnectors.isEmpty())) {
            return false;
        }
        return true;
    }
    
    public void propagateSourceChanged() {
        if (m_outConnector != null) {
            m_outConnector.propagateSourceChanged();
        }
    }
    
    public void setCenter(int x, int y) {
        m_x = x-WIDTH/2;
        m_y = y-HEIGHT/2;
        if (m_x<40) {
            m_x = 40;
        }
        if (m_y<40) {
            m_y = 40;
        }
        setConnectorsPosition();
    }
    
    public int getCenterX() {
        return m_x+WIDTH/2;
    }
    
    public int getCenterY() {
        return m_y+HEIGHT/2;
    }

    public int getX() {
        return m_x;
    }
    
    public int getY() {
        return m_y;
    }
    
    public abstract String getErrorMessage();
    
    @Override
    public void draw(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
                
        
        initFonts(g);

        String dataName = getDataName();
        String typeName = getTypeName();

        LinearGradientPaint gradient = getBackgroundGradient();
        g2.setPaint(gradient);
        g.fillRoundRect(m_x, m_y, WIDTH, HEIGHT, 8, 8);
        
        g.setColor(getFrameColor());
        
        Stroke previousStroke = g2.getStroke();
        BasicStroke stroke = m_selected ? STROKE_SELECTED : STROKE_NOT_SELECTED;
        g2.setStroke(stroke);
        g.drawRoundRect(m_x, m_y, WIDTH, HEIGHT, 8, 8);
        g2.setStroke(previousStroke);
        
        FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
        int stringWidth = metricsBold.stringWidth(typeName);
        
        int xText = m_x+(WIDTH-stringWidth)/2;
        int yText = m_y-m_hgtBold;
        
        ImageIcon icon = getIcon();
        if (icon != null) {
            g.drawImage(icon.getImage(), m_x+MARGIN, m_y+MARGIN, null);
        }
        ImageIcon statusIcon = getStatusIcon();
        if (statusIcon != null) {
            g.drawImage(statusIcon.getImage(), m_x+MARGIN, m_y+HEIGHT-MARGIN-statusIcon.getIconHeight(), null);
        }
        
        g.setFont(m_fontBold);
        g.setColor(Color.black);
        g.drawString(typeName, xText, yText);
        
        if (dataName != null) {
            stringWidth = metricsBold.stringWidth(dataName);
            xText = m_x+(WIDTH-stringWidth)/2;
            yText = yText - m_hgtBold;
            g.drawString(dataName, xText, yText);
        }
        
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                connector.draw(g);
            }
        }
        if (m_outConnector != null) {
            m_outConnector.draw(g);
        }
        
        
    }

    public abstract Color getFrameColor();
    public abstract ImageIcon getIcon();
    public abstract ImageIcon getStatusIcon();
    public abstract boolean canBeProcessed();
    

    public abstract void process(ProcessCallbackInterface callback);

    public abstract void askDisplay();
    public abstract boolean settings();

        
    @Override
    public String getPreviousDataName() {
        if (m_inConnectors != null) {
            StringBuilder sb = new StringBuilder();
            for (GraphConnector connector : m_inConnectors) {
                String dataName = connector.getDataName();
                if (dataName != null) {
                    if (sb.length()>0) {
                        sb.append(' ');
                    }
                    sb.append(dataName);
                }
            }
            if (sb.length()>0) {
                return sb.toString();
            }
            return null;

        }
        return null;
    }

    public LinearGradientPaint getBackgroundGradient() {
        
        if ((m_gradient == null) || (m_gradientStartY != m_y)) {

            Color frameColor = getFrameColor();

            Point2D start = new Point2D.Float(m_x, m_y);
            Point2D end = new Point2D.Float(m_x, m_y + HEIGHT);
            float[] dist = {0.0f, 0.5f, 0.501f, 1.0f};
            Color[] colors = {Color.white, frameColor.brighter(), frameColor, frameColor.brighter()};
            m_gradient =  new LinearGradientPaint(start, end, dist, colors);
            m_gradientStartY = m_y;
        }
        
        return m_gradient;
            
    }
    private LinearGradientPaint m_gradient = null;
    private int m_gradientStartY = -1;

    protected void initFonts(Graphics g) {
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
    public AbstractConnectedGraphObject inside(int x, int y) {
        
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                AbstractConnectedGraphObject object = connector.inside(x, y);
                if (object != null) {
                    return object;
                }
            }
        }
        if (m_outConnector != null) {
            AbstractConnectedGraphObject object = m_outConnector.inside(x, y);
            if (object != null) {
                return object;
            }
        }
        
        
        if ((x>=m_x) && (y>=m_y) && (x<=m_x+WIDTH) && (y<=m_y+HEIGHT)) {
            return this;
        }
        
        return null;
    }
    
    @Override
    public void move(int dx, int dy) {
        m_x += dx;
        m_y += dy;
        setConnectorsPosition();
    }
    
    private void setConnectorsPosition() {
        if (m_inConnectors != null) {
            int nb = m_inConnectors.size();
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                int y = m_y + (int) Math.round((((double) HEIGHT)/(nb+1))*(i+1));
                connector.setRightPosition(m_x, y);
                i++;
            }
        }
        if (m_outConnector != null) {
            m_outConnector.setPosition(m_x + WIDTH, m_y + HEIGHT / 2);
        }
    }
    
    @Override
    public void delete() {
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                connector.delete();
            }
        }
        if (m_outConnector != null) {
            m_outConnector.delete();
        }
    }
    
    @Override
    public JPopupMenu createPopup(final GraphPanel panel) {
        
        int nbConnections = (m_inConnectors == null) ? 0 : m_inConnectors.size();
        
        JPopupMenu popup = new JPopupMenu();
        popup.add(new DisplayAction(this, nbConnections));
        popup.add(new SettingsAction(this, nbConnections));
        popup.add(new ProcessAction(this));
        popup.add(new ErrorAction(this));
        popup.addSeparator();
        popup.add(new DeleteAction(panel, this));
        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                setSelected(false); 
                panel.repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }
        });
        return popup;
    }
 
    public class DeleteAction  extends AbstractAction {
        
        private AbstractConnectedGraphObject m_graphObject = null;
        private GraphPanel m_graphPanel = null;
        
        public DeleteAction(GraphPanel panel, AbstractConnectedGraphObject graphObject) {
            super("Delete");
            m_graphPanel = panel;
            m_graphObject = graphObject;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            m_graphObject.delete();
            m_graphPanel.removeGraphNode(m_graphObject);
            m_graphPanel.repaint();
        }
    }
    
    public class DisplayAction extends AbstractAction {

        private GraphNode m_graphNode = null;

        public DisplayAction(GraphNode graphNode, int nbInConnections) {
            super("Display");
            m_graphNode = graphNode;
            
            setEnabled(graphNode.calculationDone());

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_graphNode.askDisplay();
            m_graphPanel.repaint();
        }
    }
    
    public class SettingsAction extends AbstractAction {

        private GraphNode m_graphNode = null;

        public SettingsAction(GraphNode graphNode, int nbInConnections) {
            super("Settings");

            m_graphNode = graphNode;
            setEnabled(graphNode.canSetSettings());

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_graphNode.settings();
            m_graphPanel.repaint();

        }
    }

    public class ProcessAction extends AbstractAction {

        private GraphNode m_graphNode = null;

        public ProcessAction(GraphNode graphNode) {
            super("Process");

            m_graphNode = graphNode;
            setEnabled(graphNode.canBeProcessed());

        }

        @Override
        public void actionPerformed(ActionEvent e) {

            ProcessEngine.getProcessEngine().runANode(m_graphNode, m_graphPanel);
        }
    }
            
    public class ErrorAction extends AbstractAction {
        private GraphNode m_graphNode = null;
        
        public ErrorAction(GraphNode graphNode) {
            super("Display Error");

            m_graphNode = graphNode;
            setEnabled(graphNode.getErrorMessage()!=null);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Error", m_graphNode.getErrorMessage());
            errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
            errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            errorDialog.setVisible(true);
        }
    }

    public LinkedList<GraphNode> getOutLinkedGraphNodes() {
        if (!calculationDone()) {
            return null;
        }
        return (m_outConnector == null) ? null : m_outConnector.getOutLinkedGraphNodes();
    }
    
}
