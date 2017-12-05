package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngine;
import fr.proline.studio.utils.IconManager;
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
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.openide.windows.WindowManager;

/**
 * Parent Node for all graph nodes
 * @author JM235353
 */
public abstract class GraphNode extends AbstractConnectedGraphObject {

    
    public static final int WIDTH = 60;
    public static final int HEIGHT_MIN = 60;
    private static final int MARGIN = 5;
    
    protected LinkedList<GraphConnector> m_inConnectors = null;
    protected LinkedList<GraphConnector> m_outConnector = null;
    
    
   
    protected int m_x = 0;
    protected int m_y = 0;
    protected int m_extraHeight = 0;

    protected GraphNodeAction m_graphNodeAction = new GraphNodeAction();
    protected GraphNodeAction m_menuAction = new GraphNodeAction();
    
    
    protected GraphPanel m_graphPanel = null;

    private long m_startTime = System.currentTimeMillis();
    
    protected int m_id;
    
    public static int INCREMANTAL_ID = 0;
    
    public enum NodeAction {
        NO_ACTION,
        STEP_ACTION,
        ERROR_ACTION,
        RESULT_ACTION
    }
    
    public GraphNode(GraphPanel panel) {
        super(TypeGraphObject.GRAPH_NODE);
        m_graphPanel = panel;
        
        // menu always available 
        m_menuAction.setAction(true);
        
        m_id = INCREMANTAL_ID;
        INCREMANTAL_ID++;
    }

    public int getId() {
        return m_id;
    }
    
    public GraphConnector getFirstFreeConnector() {
        if (m_inConnectors == null) {
            return null;
        }
        
        for (GraphConnector inConnector : m_inConnectors) {
            if (inConnector.isConnected(false)) {
                continue;
            }
            return inConnector;
        }
        return null;
    }
    
    public boolean canAddConnector() {
        return false;
    }
    public void addFreeConnector() {
        // nothing to do
    }
    
    public void updateNumberOfInConnections() {
        // nothing to do
    }
    
    public void connectTo(GraphNode inGraphNode, int outConnectorIndex) {
        GraphConnector inConnector = inGraphNode.getFirstFreeConnector();
        m_outConnector.get(outConnectorIndex).addConnection(inConnector);
        inConnector.addConnection(m_outConnector.get(0));
    }
    
    public boolean hasInConnector() {
        if ((m_inConnectors == null) || (m_inConnectors.isEmpty())) {
            return false;
        }
        return true;
    }
    
    public void propagateSourceChanged() {
        if (m_outConnector != null) {
            for (GraphConnector connector : m_outConnector) {
                connector.propagateSourceChanged();
            }
        }
    }
    
    public void setCenter(int x, int y) {
        m_x = x-WIDTH/2;
        m_y = y-(HEIGHT_MIN+m_extraHeight)/2;
        if (m_x<40) {
            m_x = 40;
        }
        if (m_y<40) {
            m_y = 40;
        }
        setConnectorsPosition();

        if (m_group != null) {
            m_group.objectMoved();
        }
    }
    
    public int getCenterX() {
        return m_x+WIDTH/2;
    }
    
    public int getCenterY() {
        return m_y+(HEIGHT_MIN+m_extraHeight)/2;
    }

    public int getX() {
        return m_x;
    }
    
    public int getY() {
        return m_y;
    }
    
    public int getXEnd() {
        return m_x+WIDTH;
    }
    
    public int getYEnd() {
        return m_y+(HEIGHT_MIN+m_extraHeight);
    }
    
    public abstract String getErrorMessage();
    
    @Override
    public void draw(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
                
        
        initFonts(g);

        String dataName = getDataName();
        String typeName = getTypeName();

        if (m_hightlighted) {
            g2.setColor(getFrameColor().brighter());
            g.fillRoundRect(m_x-5, m_y-5, WIDTH+10, (HEIGHT_MIN+m_extraHeight)+10, 8, 8);
        }
        
        
        LinearGradientPaint gradient = getBackgroundGradient();
        g2.setPaint(gradient);
        g.fillRoundRect(m_x, m_y, WIDTH, HEIGHT_MIN+m_extraHeight, 8, 8);
        
        g.setColor(getFrameColor());
        
        Stroke previousStroke = g2.getStroke();
        BasicStroke stroke = (m_selected || m_hightlighted) ? STROKE_SELECTED : STROKE_NOT_SELECTED;
        g2.setStroke(stroke);
        g.drawRoundRect(m_x, m_y, WIDTH, (HEIGHT_MIN+m_extraHeight), 8, 8);
        g2.setStroke(previousStroke);
        
        FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
        int stringWidth = metricsBold.stringWidth(typeName);
        
        int xText = m_x+(WIDTH-stringWidth)/2;
        int yText = m_y-m_hgtBold;
        
        ImageIcon icon = getIcon();
        if (icon != null) {
            g.drawImage(icon.getImage(), m_x+MARGIN, m_y+MARGIN, null);
        }

        
        ImageIcon menuIcon = IconManager.getIcon(IconManager.IconType.MENU);
        int wIcon = menuIcon.getIconWidth();
        int hIcon = menuIcon.getIconHeight();
        int xIcon = m_x+WIDTH-MARGIN-menuIcon.getIconWidth();
        int yIcon = m_y+MARGIN;
        g.drawImage(menuIcon.getImage(), xIcon, yIcon, null);
        m_menuAction.setBounds(xIcon - 2, yIcon - 2, wIcon + 4, hIcon + 4);
        m_menuAction.draw(g);
        
        NodeAction possibleAction = possibleAction();

        m_graphNodeAction.setAction(possibleAction != NodeAction.NO_ACTION);


        ImageIcon statusIcon = getStatusIcon();
        if (statusIcon != null) {
            
            wIcon = statusIcon.getIconWidth();
            hIcon = statusIcon.getIconHeight();
            xIcon = m_x+MARGIN;
            yIcon = m_y+(HEIGHT_MIN+m_extraHeight)-MARGIN-statusIcon.getIconHeight();
            
            
             if ((possibleAction == NodeAction.STEP_ACTION) || (possibleAction == NodeAction.ERROR_ACTION)) {
                m_graphNodeAction.setBounds(xIcon - 2, yIcon - 2, wIcon + 4, hIcon + 4);
                m_graphNodeAction.setAction(true);
                m_graphNodeAction.draw(g);
            }

            g.drawImage(statusIcon.getImage(), xIcon, yIcon, null);

            if ((possibleAction == NodeAction.STEP_ACTION) || (possibleAction == NodeAction.ERROR_ACTION)) {

                final int LINE_LENGTH = 3;

                g.setColor(getFrameColor().darker());

                double coef = ((double) (System.currentTimeMillis() - m_startTime)) / 1000.0d;
                coef = coef % 3;
                if (coef >= 1) {
                    coef = 0;
                } else {
                    coef = coef - ((int) coef);
                }

                double theta = Math.toRadians(180 * coef);
                g2.rotate(theta, xIcon + wIcon / 2, yIcon + hIcon / 2);

                g.drawLine(xIcon - LINE_LENGTH, yIcon - LINE_LENGTH, xIcon, yIcon - LINE_LENGTH);
                g.drawLine(xIcon - LINE_LENGTH, yIcon - LINE_LENGTH, xIcon - LINE_LENGTH, yIcon);
                g.drawLine(xIcon + wIcon + LINE_LENGTH, yIcon + hIcon + LINE_LENGTH, xIcon + wIcon + LINE_LENGTH, yIcon + hIcon);
                g.drawLine(xIcon + wIcon + LINE_LENGTH, yIcon + hIcon + LINE_LENGTH, xIcon + wIcon, yIcon + hIcon + LINE_LENGTH);

                g2.rotate(-theta, xIcon + wIcon / 2, yIcon + hIcon / 2);

            }

        }

        ImageIcon displayIcon = getDisplayIcon();
        if (displayIcon != null) {
            
            wIcon = displayIcon.getIconWidth();
            hIcon = displayIcon.getIconHeight();
            xIcon = m_x+WIDTH-MARGIN-displayIcon.getIconWidth();
            yIcon = m_y+(HEIGHT_MIN+m_extraHeight)-MARGIN-displayIcon.getIconHeight();
            
            
            if (possibleAction == NodeAction.RESULT_ACTION) {
                m_graphNodeAction.setBounds(xIcon - 2, yIcon - 2, wIcon + 4, hIcon + 4);
                m_graphNodeAction.setAction(true);
                m_graphNodeAction.draw(g);
            }

            g.drawImage(displayIcon.getImage(), xIcon, yIcon, null);

            /*if (possibleAction == NodeAction.STEP_ACTION) {

                final int LINE_LENGTH = 3;

                g.setColor(getFrameColor().darker());

                double coef = ((double) (System.currentTimeMillis() - m_startTime)) / 1000.0d;
                coef = coef % 3;
                if (coef >= 1) {
                    coef = 0;
                } else {
                    coef = coef - ((int) coef);
                }

                double theta = Math.toRadians(180 * coef);
                g2.rotate(theta, xIcon + wIcon / 2, yIcon + hIcon / 2);

                g.drawLine(xIcon - LINE_LENGTH, yIcon - LINE_LENGTH, xIcon, yIcon - LINE_LENGTH);
                g.drawLine(xIcon - LINE_LENGTH, yIcon - LINE_LENGTH, xIcon - LINE_LENGTH, yIcon);
                g.drawLine(xIcon + wIcon + LINE_LENGTH, yIcon + hIcon + LINE_LENGTH, xIcon + wIcon + LINE_LENGTH, yIcon + hIcon);
                g.drawLine(xIcon + wIcon + LINE_LENGTH, yIcon + hIcon + LINE_LENGTH, xIcon + wIcon, yIcon + hIcon + LINE_LENGTH);

                g2.rotate(-theta, xIcon + wIcon / 2, yIcon + hIcon / 2);

            }*/

        }
        
        g.setFont(m_fontBold);
        g.setColor(Color.black);
        g.drawString(typeName, xText, yText);
        
        if (dataName != null) {
            if (dataName.length() > 17) {
                dataName = dataName.substring(0, 17) + "."; // limit data name
            }
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
            for (GraphConnector connector : m_outConnector) {
                connector.draw(g);
            }
        }
        
        
    }

    public abstract Color getFrameColor();
    public abstract ImageIcon getIcon();
    public abstract ImageIcon getStatusIcon();
    public abstract ImageIcon getDisplayIcon();
    public abstract NodeAction possibleAction();
    public abstract void doAction(int x, int y);
    public abstract boolean canBeProcessed();
    

    public abstract void process(ProcessCallbackInterface callback);

    public abstract void askDisplay(int index);
    public abstract ArrayList<WindowBox> getDisplayWindowBox(int index);
    public abstract ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess();
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
            Point2D end = new Point2D.Float(m_x, m_y + (HEIGHT_MIN+m_extraHeight));
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
    public AbstractGraphObject inside(int x, int y) {
        
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                AbstractGraphObject object = connector.inside(x, y);
                if (object != null) {
                    return object;
                }
            }
        }
        if (m_outConnector != null) {
            for (GraphConnector connector : m_outConnector) {
                AbstractGraphObject object = connector.inside(x, y);
                if (object != null) {
                    return object;
                }
            }
        }

       
        AbstractGraphObject object = m_graphNodeAction.inside(x, y);
        if (object != null) {

            return object;
        }
        
        object = m_menuAction.inside(x, y);
        if (object != null) {

            return object;
        }
        
        
       
       
        if ((x>=m_x) && (y>=m_y) && (x<=m_x+WIDTH) && (y<=m_y+(HEIGHT_MIN+m_extraHeight))) {
            return this;
        }
        
        return null;
    }
    
    @Override
    public void move(int dx, int dy) {
        m_x += dx;
        m_y += dy;
        setConnectorsPosition();

        if (m_group != null) {
            m_group.objectMoved();
        }
    }
    
    @Override
    public int correctMoveX(int dx) {
        if ((m_x+dx) <0) {
            return -m_x;
        } 
        return dx;
    }
    @Override
    public int correctMoveY(int dy) {
        if ((m_y+dy) <0) {
            return -m_y;
        } 
        return dy;
    }
    
    public boolean hideAction() {
        return m_graphNodeAction.setHighlighted(false) | m_menuAction.setHighlighted(false);
    }
    
    protected void setConnectorsPosition() {
        if (m_inConnectors != null) {
            int nb = m_inConnectors.size();
            if (nb>3) {
                m_extraHeight = (nb-3) * (GraphConnector.HEIGHT+4);
            } else {
                m_extraHeight = 0;
            }
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                int y = m_y + (int) Math.round((((double) (HEIGHT_MIN+m_extraHeight))/(nb+1))*(i+1));
                connector.setRightPosition(m_x, y);
                i++;
            }
        }
        if (m_outConnector != null) {
            int nbOutConnectors = m_outConnector.size();
            int i = 1;
            for (GraphConnector connector : m_outConnector) {
                connector.setPosition(m_x + WIDTH, m_y + (i*(HEIGHT_MIN+m_extraHeight)) / (nbOutConnectors+1));
                i++;
            }
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
            for (GraphConnector connector : m_outConnector) {
                connector.delete();
            }
            m_outConnector.clear();
        }
        
        if (m_group != null) {
            m_group.removeObject(this);
            m_group = null;
        }
    }
    
    @Override
    public void deleteAction() {
        delete();
        m_graphPanel.removeGraphNode(this);
        m_graphPanel.repaint();
    }
    
    @Override
    public JPopupMenu createPopup(final GraphPanel panel) {
        
        int nbConnections = (m_inConnectors == null) ? 0 : m_inConnectors.size();
        
        JPopupMenu popup = new JPopupMenu();
        
        JMenu displayMenu = new JMenu("Display");
        
        LinkedList<AbstractAction> displayActions = new LinkedList();
        int nbDisplay = (m_outConnector == null) ? 1 : m_outConnector.size();
        if (nbDisplay <= 1) {
            displayActions.add(new DisplayBelowAction(this, 0, null));
            displayActions.add(new DisplayInNewWindowAction(this, 0, null));
        } else {
            for (int i=0;i<nbDisplay;i++) {
                displayActions.add(new DisplayBelowAction(this, i, getOutTooltip(i)));
            }
            displayActions.add(null);
            for (int i=0;i<nbDisplay;i++) {
                displayActions.add(new DisplayInNewWindowAction(this, i, getOutTooltip(i)));
            }
        }
        
        boolean enabled = false;
        for (AbstractAction action: displayActions) {
            if (action == null) {
                displayMenu.addSeparator();
            } else {
                enabled |= action.isEnabled();
                displayMenu.add(action);
            }
        }
        displayMenu.setEnabled(enabled);
        
       
        

        popup.add(new SettingsAction(this, nbConnections));
        popup.add(displayMenu);
        popup.add(new ErrorAction(this));
        popup.addSeparator();
        popup.add(new DeleteAction());

        return popup;
    }
 
    public class DeleteAction  extends AbstractAction {

        public DeleteAction() {
            super("Delete");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            deleteAction();
        }
    }
    
    public class DisplayInNewWindowAction extends AbstractAction {

        private GraphNode m_graphNode = null;
        private int m_connectionIndex;

        public DisplayInNewWindowAction(GraphNode graphNode, int connectionIndex, String name) {
            super( (name == null) ? "In New Window" : name+" In New Window");
            m_graphNode = graphNode;
            m_connectionIndex = connectionIndex;
            
            setEnabled(graphNode.calculationDone());

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_graphNode.askDisplay(m_connectionIndex);
            m_graphPanel.repaint(); 
        }
    }
    
    public class DisplayBelowAction extends AbstractAction {

        private GraphNode m_graphNode = null;
        private int m_connectionIndex;

        public DisplayBelowAction(GraphNode graphNode, int connectionIndex, String name) {
            super( (name == null) ? "Below" : name+" Below");
            m_graphNode = graphNode;
            m_connectionIndex = connectionIndex;
            
            setEnabled(graphNode.calculationDone());

        }

        @Override
        public void actionPerformed(ActionEvent e) {

             m_graphPanel.displayBelow(m_graphNode, true, m_graphNode.getFullName(m_connectionIndex), null, m_connectionIndex);
        }
    }
    
    public class SettingsAction extends AbstractAction {

        private GraphNode m_graphNode = null;

        public SettingsAction(GraphNode graphNode, int connectionIndex) {
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
        
        if (m_outConnector == null) {
            return null;
        }
        
        LinkedList<GraphNode> outLinkedGraphNodes = new LinkedList<>();
        for (GraphConnector connector : m_outConnector) {
            connector.getOutLinkedGraphNodes(outLinkedGraphNodes);
        }
        
        return outLinkedGraphNodes;
    }
    
    public abstract void saveGraph(StringBuilder sb);

    
    public abstract String getOutTooltip(int index);


    
}
