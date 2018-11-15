package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.id.ProjectId;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;


/**
 * Graph Node representing Data
 * @author JM235353
 */
public class DataGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(51,128,200);

    
    private TableInfo m_tableInfo = null;


    
    public DataGraphNode(TableInfo tableInfo, GraphPanel panel) {
        super(panel);
        m_tableInfo = tableInfo;
        
        m_outConnector = new LinkedList<>();
        m_outConnector.add(new GraphConnector(this, true, 0, panel));
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
    
    @Override
    public void propagateSourceChanged() {
        // nothing to do
    }
    
    @Override
    public boolean isConnected(boolean recursive) {
        return true;
    }
    @Override
    public boolean canSetSettings() {
        return false;
    }
    @Override
    public boolean settingsDone() {
        return true;
    }
    @Override
    public boolean calculationDone() {
        return true;
    }

    
    @Override
    public String getFullName() {
        return m_tableInfo.getFullName();
    }
    
    @Override
    public String getDataName() {
        return m_tableInfo.getDataName();
    }

    @Override
    public String getTypeName() {
        return m_tableInfo.getTypeName();
    }

    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }
    
    @Override
    public ImageIcon getIcon() {
        return m_tableInfo.getIcon();
    }
    
    @Override
    public ImageIcon getStatusIcon() {
        return null;
    }
    
    @Override
    public ImageIcon getDisplayIcon() {
        return IconManager.getIcon(IconManager.IconType.TABLE);
    }
    
    @Override
    public NodeAction possibleAction() {
        return NodeAction.RESULT_ACTION;
    }
    
    @Override
    public void doAction(int x, int y) {

        if (m_graphNodeAction.isHighlighted()) {
            m_graphNodeAction.setHighlighted(false);
            m_graphNodeAction.setHighlighted(false);

            // process
            askDisplay(0);
        } else if (m_menuAction.isHighlighted()) {
            m_menuAction.setHighlighted(false);

            JPopupMenu popup = createPopup(m_graphPanel);
            if (popup != null) {
                popup.show(m_graphPanel, x, y);
            }
        }
    }

    @Override
    public void askDisplay(int index) {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(m_tableInfo.getDataName(), m_tableInfo.getTypeName(), IconManager.IconType.CHALKBOARD, false);
        
        GlobalTableModelInterface model = m_tableInfo.getModel();
        ProjectId projectId = (ProjectId) model.getSingleValue(ProjectId.class);
        long id = (projectId!=null) ? projectId.getId() : -1L;
        
        windowBox.setEntryData(id, model);
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
    }

    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(int index) {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(m_tableInfo.getDataName(), m_tableInfo.getTypeName(), IconManager.IconType.CHALKBOARD, false);

        GlobalTableModelInterface model = m_tableInfo.getModel();
        ProjectId projectId = (ProjectId) model.getSingleValue(ProjectId.class);
        long id = (projectId != null) ? projectId.getId() : -1L;

        windowBox.setEntryData(id, model);
        
        ArrayList<WindowBox> windowBoxList = new ArrayList<>(1);
        windowBoxList.add(windowBox);
        return windowBoxList;
    }

    @Override
    public boolean settings() {
        // nothing to do
        return true;
    }

    @Override
    public void process(ProcessCallbackInterface callback) {
        // nothing to do
        callback.finished(this);

    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface(int index) {
        return m_tableInfo.getModel();
    }

    @Override
    public boolean canBeProcessed() {
        return true;
    }

    @Override
    public  ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess() {
        return null;
    }

    @Override
    public String getTooltip(int x, int y) {
        return null;
    }

    @Override
    public String getOutTooltip(int index) {
        return null;
    }
    
    @Override
    public void saveGraph(StringBuilder sb) {
        // we don't save graph node
    }
    
}
