package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxMultiGraphics extends AbstractDataBox  {

    private List<CompareDataInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    
    public DataboxMultiGraphics() {
       this(false);
    }
    
    public DataboxMultiGraphics(boolean defaultLocked) {
         super(DataboxType.DataboxMultiGraphics);

         m_defaultLocked = defaultLocked;
         
        // Name of this databox
        m_name = "Graphic";
        m_description = "Graphics : Linear Plot";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(List.class, false);
        registerInParameter(inParameter);
        
    }
    
    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final List<CompareDataInterface> valuesL = (List<CompareDataInterface>) m_previousDataBox.getData(false, List.class);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class);
        ((MultiGraphicsPanel)m_panel).setData(valuesL, crossSelectionInterfaceL);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<CompareDataInterface>) data;
        dataChanged();
    }
    
}
