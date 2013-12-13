package fr.proline.studio.markerbar;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.*;
import org.openide.windows.WindowManager;

public class MarkerBar extends AbstractBar implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    private static final int TEXT_PAD = 4;
    private static Font m_lineNumbersFont = null;
    private static FontMetrics m_lineNumbersFontMercrics = null;
    private static int m_fontAscent = 0;
    private static int m_fontDescent = 0;
    
    private boolean m_displayLineNumbers = false;
    private int m_maxLineNumber = -1;
    
    public MarkerBar(MarkerContainerPanel containerPanel) {
        super(containerPanel);

        addMouseListener(this);
        addMouseMotionListener(this);

    }

    public boolean isLineNumbersDisplayed() {
        return m_displayLineNumbers;
    }
    
    public void setLineNumbersDisplayed(boolean displayLineNumbers) {
        m_displayLineNumbers = displayLineNumbers;
    }

    public boolean setMaxLineNumber(int maxLineNumber) {
        boolean changed = (m_maxLineNumber != maxLineNumber);
        m_maxLineNumber = maxLineNumber;
        return changed;
    }
    
    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if ((m_displayLineNumbers) && (m_maxLineNumber != -1) && (m_lineNumbersFont != null)) {
            String numberString = String.valueOf(m_maxLineNumber);
            dim.width = m_lineNumbersFontMercrics.stringWidth(numberString)+TEXT_PAD*2;
        }

        return dim;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if ((m_displayLineNumbers) && (m_maxLineNumber != -1) && (m_lineNumbersFont != null)) {
            String numberString = String.valueOf(m_maxLineNumber);
            int minWidth = m_lineNumbersFontMercrics.stringWidth(numberString)+TEXT_PAD*2;
            if (dim.width<minWidth) {
                dim.width = minWidth;
            }
        }

        return dim;
    }
    
    @Override
    public void paint(Graphics g) {

        initFontForLineNumbers(g); // must be done even if line numbers are not displayed at the beginning
        
        g.setColor(m_almostWhiteColor);
        g.fillRect(1, 1, getWidth()-2, getHeight()-2);
        
        g.setColor(Color.lightGray);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        
        MarkerComponentInterface componentInterface = containerPanel.getMarkerComponentInterface();

        int firstVisibleRow = componentInterface.getFirstVisibleRow();
        int lastVisibleRow = componentInterface.getLastVisibleRow();

        
 
        TreeMap<Integer, ArrayList<AbstractMarker>> markerMap = containerPanel.getMarkerArray();
        Iterator<Integer> itRow = markerMap.keySet().iterator();
        while (itRow.hasNext()) {
            Integer row = itRow.next();
            int rowInt = componentInterface.convertRowIndexToView(row.intValue());

            if ((rowInt < firstVisibleRow) || (rowInt > lastVisibleRow)) {
                continue;
            }

            
            
            ArrayList<AbstractMarker> markersArrayList = markerMap.get(row);
            int size = markersArrayList.size();
            for (int i = 0; i < size; i++) {
                AbstractMarker marker = markersArrayList.get(i);

                int y1 = componentInterface.getRowYStart(rowInt);
                int y2 = componentInterface.getRowYStop(rowInt);

                MarkerRendererInterface renderer = containerPanel.getRenderer(marker);

                renderer.paint(AbstractBar.BarType.MARKER_BAR, g, 0, y1, getWidth(), y2 - y1);
            }
            


        }
        
        // display lines
        if ((m_displayLineNumbers) && (firstVisibleRow!=-1)) {
            
            g.setColor(Color.black);
            for (int i = firstVisibleRow; i <= lastVisibleRow; i++) {
                int y1 = componentInterface.getRowYStart(i);
                int y2 = componentInterface.getRowYStop(i);

                String numberString = String.valueOf(i + 1);
                int xText = getWidth() - m_lineNumbersFontMercrics.stringWidth(numberString) - TEXT_PAD;
                int yText = (y1 + ((y2 + 1 - y1) / 2)) - ((m_fontAscent + m_fontDescent) / 2) + m_fontAscent;

                g.drawString(numberString, xText, yText);

            }
        }
    }
    private static final Color m_almostWhiteColor = new Color(248,248,248);

    private void initFontForLineNumbers(Graphics g) {
        if (m_lineNumbersFont == null) {
            m_lineNumbersFont = new Font("SansSerif", Font.PLAIN, 10);
            m_lineNumbersFontMercrics = g.getFontMetrics(m_lineNumbersFont);
            m_fontAscent = m_lineNumbersFontMercrics.getAscent();
            m_fontDescent = m_lineNumbersFontMercrics.getDescent();
        }
    }
    
    
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        MarkerComponentInterface componentInterface = containerPanel.getMarkerComponentInterface();
        rowClicked = componentInterface.getRowInModel(e.getY());
                
        if (e.isPopupTrigger()) {
            
            
            
            Point p = ((JComponent)e.getSource()).getLocationOnScreen();
            
            popupX = e.getX()+p.x;
            popupY = e.getY()+p.y;
            
            getPopup(rowClicked).show( (JComponent)e.getSource(), e.getX(), e.getY());
            return;
        }

        
        // remove all existing markers if they exist 
        if (containerPanel.removeAllMarkers(rowClicked)) {
            return;
        }
        
        // add bookmark marker
        BookmarkMarker marker = new BookmarkMarker(rowClicked);
        containerPanel.addMarker(marker);
  
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        MarkerComponentInterface componentInterface = containerPanel.getMarkerComponentInterface();
        int row = componentInterface.getRowInModel(e.getY());
        
        AnnotationMarker marker = (AnnotationMarker) containerPanel.getMarker(row, DefaultMarker.ANNOTATION_MARKER);
        
        if (marker == null) {
            setToolTipText(null);
        } else {
            setToolTipText(marker.getText());
        }
        
    }
    
    
    private JPopupMenu getPopup(int row) {
        if (m_popup != null) {
            updateActions(row);
            return m_popup;
        }
        
        actions = new AbstractMarkerBarAction[7];
        actions[0] = new DisplayLineNumbersAction();
        actions[1] = null;
        actions[2] = new AddAnnotationAction();
        actions[3] = new AddBookmarkAction();
        actions[4] = new RemoveMarkerAction();
        actions[5] = null;
        actions[6] = new RemoveAllMarkersAction();
        
        
        m_popup = new JPopupMenu();
        for (int i=0;i<actions.length;i++) {
            AbstractMarkerBarAction action = actions[i];
            if (action == null) {
                m_popup.addSeparator();
            } else {
                m_popup.add(action.getPopupPresenter());
            }
        }
        
        updateActions(row);
        
        return m_popup;
    }
    private void updateActions(int row) {
        for (int i=0;i<actions.length;i++) {
            AbstractMarkerBarAction action = actions[i];
            if (action != null) {
                action.updateEnabled(row);
            }
        }
    }
    JPopupMenu m_popup = null;
    AbstractMarkerBarAction[] actions = null;
    private int rowClicked = -1;
    private int popupX = -1;
    private int popupY = -1;


    
    private abstract class AbstractMarkerBarAction extends AbstractAction {
        
        public AbstractMarkerBarAction(String name) {
            super(name);
        }
        
        public abstract void updateEnabled(int row);
        
        public JMenuItem getPopupPresenter() {
            return new JMenuItem(this);
        }
    }
    
    
    private class DisplayLineNumbersAction extends AbstractMarkerBarAction {

        private JCheckBoxMenuItem m_checkboxMenuItem = null;
        
        public DisplayLineNumbersAction() {
            super("Display Line Numbers");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLineNumbersDisplayed(!isLineNumbersDisplayed());
            containerPanel.revalidate();
            containerPanel.repaint();
        }

        @Override
        public void updateEnabled(int row) {
            setEnabled(true);
            ((JCheckBoxMenuItem)getPopupPresenter()).setSelected(m_displayLineNumbers);
        }
        
        @Override
         public JMenuItem getPopupPresenter() {
            if (m_checkboxMenuItem == null) {
                m_checkboxMenuItem = new JCheckBoxMenuItem(this);
            }
            return m_checkboxMenuItem;
        }
    }
    
    
    private class AddAnnotationAction extends AbstractMarkerBarAction {

        public AddAnnotationAction() {
            super("Add Annotation...");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            AnnotationMarkerDialog dialog = AnnotationMarkerDialog.getDialog(WindowManager.getDefault().getMainWindow());
            dialog.setLocation(popupX, popupY);
            dialog.setVisible(true);
            if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                String description = dialog.getDescriptionField();
                AnnotationMarker marker = new AnnotationMarker(rowClicked, description);
                containerPanel.addMarker(marker);
            }

        }

        @Override
        public void updateEnabled(int row) {
            setEnabled(! containerPanel.hasMarker(row));
        }
        
        
    }
    
    private class AddBookmarkAction extends AbstractMarkerBarAction {

        public AddBookmarkAction() {
            super("Add Bookmark");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {

            // add bookmark marker
            BookmarkMarker marker = new BookmarkMarker(rowClicked);
            containerPanel.addMarker(marker);
        }

        @Override
        public void updateEnabled(int row) {
            setEnabled(! containerPanel.hasMarker(row));
        }
    }
    
    private class RemoveMarkerAction extends AbstractMarkerBarAction {

        public RemoveMarkerAction() {
            super("Remove");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {

            BookmarkMarker marker = new BookmarkMarker(rowClicked);
            containerPanel.removeAllMarkers(rowClicked);
        }

        @Override
        public void updateEnabled(int row) {
            setEnabled(containerPanel.hasMarker(row));
        }
        
    }
    
    private class RemoveAllMarkersAction extends AbstractMarkerBarAction {

        public RemoveAllMarkersAction() {
            super("Remove All");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {

            containerPanel.removeAllMarkers();
        }

        @Override
        public void updateEnabled(int row) {
            setEnabled(containerPanel.hasMarkers());
        }
    }
    
    
}
