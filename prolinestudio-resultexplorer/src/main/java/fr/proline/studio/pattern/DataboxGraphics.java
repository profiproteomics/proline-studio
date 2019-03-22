package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.GraphicsPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataboxGraphics extends AbstractDataBox  {

    private ExtendedTableModelInterface m_values = null;

    private boolean m_defaultLocked = false;
    
    public DataboxGraphics() {
       this(true);
    }
    
    public DataboxGraphics(boolean defaultLocked) {
         super(DataboxType.DataboxGraphics, DataboxStyle.STYLE_UNKNOWN);

         m_defaultLocked = defaultLocked;
         
        // Name of this databox
        m_typeName = "Customisable Graphical Display";
        m_description = "Plots data as Histogram / Scatter Plot / Venn Diagram / Parallel Coordinates";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class, false);
        registerInParameter(inParameter);
        
    }
    
    protected void setDefaultLocked(boolean defaultLocked){
        m_defaultLocked = defaultLocked;
    }
    
    protected boolean isDefaultLocked(){
        return m_defaultLocked;
    }
    
    @Override
    public void createPanel() {
        GraphicsPanel p = new GraphicsPanel(m_defaultLocked);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final ExtendedTableModelInterface values = (m_values!=null) ? m_values : (ExtendedTableModelInterface) m_previousDataBox.getData(false, ExtendedTableModelInterface.class);
        final CrossSelectionInterface crossSelectionInterface = (m_values!=null) ? null : (CrossSelectionInterface) m_previousDataBox.getData(false, CrossSelectionInterface.class);
        ((GraphicsPanel)getDataBoxPanelInterface()).setData(values, crossSelectionInterface);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_values = (ExtendedTableModelInterface) data;
        dataChanged();
    }
    
}
