package fr.proline.studio.markerbar;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.openide.windows.WindowManager;

public class MarkerBar extends AbstractBar implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    public MarkerBar(MarkerContainerPanel containerPanel) {
        super(containerPanel);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void paint(Graphics g) {

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
        
        actions = new AbstractMarkerBarAction[5];
        actions[0] = new AddAnnotationAction();
        actions[1] = new AddBookmarkAction();
        actions[2] = new RemoveMarkerAction();
        actions[3] = null;
        actions[4] = new RemoveAllMarkersAction();
        
        
        m_popup = new JPopupMenu();
        for (int i=0;i<actions.length;i++) {
            AbstractMarkerBarAction action = actions[i];
            if (action == null) {
                m_popup.addSeparator();
            } else {
                m_popup.add(action);
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
