package fr.proline.studio.tabs;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author MB243701
 */
public class PanelAsGrid extends JPanel implements ILayoutPanel{

    private final static int DEFAULT_NB_COL = 2;
    
    // list of panels managed
    private List<IWrappedPanel> m_panels;
    private Map<GridPanel, IWrappedPanel> m_mapGridPanel;
    
    
    private int m_nbRows = 1;
    private int m_nbCols = 1;
    
    
    public PanelAsGrid() {
        initComponents();
        m_panels = new ArrayList();
    }
    
    private void initComponents(){
        this.setLayout(new GridLayout(m_nbRows, m_nbCols));
        m_mapGridPanel = new HashMap();
    }
    
    @Override
    public JPanel getComponent(){
        return this;
    }
    
    @Override
    public IWrappedPanel getSelectedPanel() {
        for (Map.Entry<GridPanel, IWrappedPanel> entrySet : m_mapGridPanel.entrySet()) {
            GridPanel gridPanel = entrySet.getKey();
            if (gridPanel.isSelected()){
                return entrySet.getValue();
            }
        }
        return null;
    }

    @Override
    public void setPanels(List<IWrappedPanel> panels, Integer nbCols) {
        this.m_panels = panels;
        this.m_nbCols = (nbCols == null || nbCols <1)? Math.max((int) Math.ceil(Math.sqrt(panels.size())), 1): nbCols;
        this.m_nbRows = panels.size() % m_nbCols > 0 ? panels.size() / m_nbCols +1 : panels.size() / m_nbCols ;
        displayPanels();
    }
    
    @Override
    public void addPanel(IWrappedPanel panel){
        GridPanel gridPanel = new GridPanel(this, panel);
        m_panels.add(panel);
        m_mapGridPanel.put(gridPanel, panel);
        this.add(gridPanel);
        if (panel.getTabHeaderComponent() != null){
            setTabHeaderComponentAt(m_panels.size()-1, panel.getTabHeaderComponent());
        }
        updateLayout();
    }
    
    @Override
    public void removePanel(int id){
        IWrappedPanel panel = m_panels.get(id);
        GridPanel gp = getGridPanel(panel);
        if (gp != null){
            m_mapGridPanel.remove(gp);
        }
        this.remove(gp);
        updateLayout();
    }
    
    @Override
    public void setSelectedPanel(IWrappedPanel panel){
        GridPanel gridPanel = getGridPanel(panel);
        changeSelectionPanel();
        if (gridPanel != null){
            gridPanel.setSelected(true);
        }
        fireStateChanged();
    }

    private void displayPanels() {
        this.removeAll();
        m_mapGridPanel = new HashMap();
        this.setLayout(new GridLayout(m_nbRows, m_nbCols));
        int id=0;
        for(IWrappedPanel panel: m_panels){
            GridPanel gridPanel = new GridPanel(this, panel);
            m_mapGridPanel.put(gridPanel, panel);
            this.add(gridPanel);
            if (panel.getTabHeaderComponent() != null){
                setTabHeaderComponentAt(id++, panel.getTabHeaderComponent());
            }
        }
    }
    
    private void updateLayout(){
        this.m_nbCols = Math.max((int) Math.ceil(Math.sqrt(m_panels.size())), 1);
        this.m_nbRows = m_panels.size() % m_nbCols > 0 ? m_panels.size() / m_nbCols +1 : m_panels.size() / m_nbCols  ;
        this.setLayout(new GridLayout(m_nbRows, m_nbCols));
        revalidate();
        repaint();
    }
    
    private GridPanel getGridPanel(IWrappedPanel panel){
        for (Map.Entry<GridPanel, IWrappedPanel> entrySet : m_mapGridPanel.entrySet()) {
            GridPanel gridPanel = entrySet.getKey();
            IWrappedPanel wrappedPanel = entrySet.getValue();
            if (wrappedPanel.getId().equals(panel.getId())){
                return gridPanel;
            }
        }
        return null;
    }
    
    public void changeSelectionPanel(){
        for (Map.Entry<GridPanel, IWrappedPanel> entrySet : m_mapGridPanel.entrySet()) {
            GridPanel grP = entrySet.getKey();
            grP.setSelected(false);
        }
        fireStateChanged();
    }
    
    @Override
    public void setTabHeaderComponentAt(int index, Component c){
        if (index > -1 && index < m_panels.size()){
            IWrappedPanel p = m_panels.get(index);
            if (p != null){
                GridPanel gridP = getGridPanel(p);
                if (gridP != null){
                    gridP.setTabHeaderComponent(c);
                }
            }
        }
    }
    
    @Override
    public int indexOfTabHeaderComponent(Component c){
        for (int i=0; i<m_panels.size(); i++){
            IWrappedPanel p = m_panels.get(i);
            GridPanel gridP = getGridPanel(p);
            if (gridP != null){
                if (gridP.indexOfTabHeaderComponent(c) != -1){
                    return i;
                }
            }
        }
        return -1;
    }
    
    @Override
    public void  addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class, l);
    }
    
    private void fireStateChanged() {
 
         // Guaranteed to return a non-null array
         Object[] listeners = listenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length-2; i>=0; i-=2) {
             if (listeners[i]==ChangeListener.class) {
                 ChangeEvent    changeEvent = new ChangeEvent(this);
                 ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
             }
         }
     }

    
}
