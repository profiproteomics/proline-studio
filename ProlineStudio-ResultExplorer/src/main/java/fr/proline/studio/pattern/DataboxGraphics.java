package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.GraphicsPanel;

/**
 *
 * @author JM235353
 */
public class DataboxGraphics extends AbstractDataBox  {

    private CompareDataInterface m_values = null;

    private boolean m_defaultLocked = false;
    
    public DataboxGraphics() {
       this(true);
    }
    
    public DataboxGraphics(boolean defaultLocked) {
         super(DataboxType.DataboxGraphics, DataboxStyle.STYLE_UNKNOWN);

         m_defaultLocked = defaultLocked;
         
        // Name of this databox
        m_typeName = "Graphic";
        m_description = "Graphics : Histogram / Scatter Plot";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(CompareDataInterface.class, false);
        registerInParameter(inParameter);
        
    }
    
    @Override
    public void createPanel() {
        GraphicsPanel p = new GraphicsPanel(m_defaultLocked);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final CompareDataInterface values = (m_values!=null) ? m_values : (CompareDataInterface) m_previousDataBox.getData(false, CompareDataInterface.class);
        final CrossSelectionInterface crossSelectionInterface = (m_values!=null) ? null : (CrossSelectionInterface) m_previousDataBox.getData(false, CrossSelectionInterface.class);
        ((GraphicsPanel)m_panel).setData(values, crossSelectionInterface);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_values = (CompareDataInterface) data;
        dataChanged();
    }
    
}
