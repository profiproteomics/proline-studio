package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.id.ProjectId;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import javax.swing.ImageIcon;


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
        
        m_outConnector = new GraphConnector(this, true);
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
    public boolean isConnected() {
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
    public void askDisplay() {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(m_tableInfo.getDataName(), m_tableInfo.getTypeName(), IconManager.IconType.CHALKBOARD, false);
        
        GlobalTableModelInterface model = m_tableInfo.getModel();
        ProjectId projectId = (ProjectId) model.getSingleValue(ProjectId.class);
        long id = (projectId!=null) ? projectId.getId() : -1l;
        
        windowBox.setEntryData(id, model);
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
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
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_tableInfo.getModel();
    }

    @Override
    public boolean canBeProcessed() {
        return true;
    }



    

    
}
