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
    private boolean m_canChooseColor = false;
    
    public DataboxMultiGraphics() {
       this(false, false);
    }
    
    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor) {
         super(DataboxType.DataboxMultiGraphics);

         m_defaultLocked = defaultLocked;
         m_canChooseColor = canChooseColor ;
         
        // Name of this databox
        m_typeName = "Graphic";
        m_description = "Multi Graphics : Linear Plot";

        // Register Possible in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(CompareDataInterface.class, true);
        registerInParameter(inParameter);
        
        inParameter.addParameter(CrossSelectionInterface.class, true);
        registerInParameter(inParameter);
        
    }
    
    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final List<CompareDataInterface> valuesL = (List<CompareDataInterface>) m_previousDataBox.getData(false, CompareDataInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        ((MultiGraphicsPanel)m_panel).setData(valuesL, crossSelectionInterfaceL);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<CompareDataInterface>) data;
        dataChanged();
    }
    
}
