package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DataboxMultiGraphics extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<ExtendedTableModelInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;
    private boolean m_displayDoubleYAxis = false;
    
    public DataboxMultiGraphics() {
        this(false, false, false);
    }
    
    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor) {
         this(defaultLocked, canChooseColor, false);
    }

    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor, boolean displayDoubleYAxis) {
        super((displayDoubleYAxis ? DataboxType.DataboxMultiGraphicsDoubleYAxis : DataboxType.DataboxMultiGraphics), DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = defaultLocked;
        m_canChooseColor = canChooseColor;
        m_displayDoubleYAxis = displayDoubleYAxis;
        
        // Name of this databox
        if(m_displayDoubleYAxis) {
            m_typeName = "Linear Plot (two axis)";
            m_description = "Display two sets of data as linear plot using 2 axis";
        } else {
            m_typeName = "Linear Plot";
            m_description = "Display data as linear plot";            
        }

        // Register Possible in parameters
        if(! m_displayDoubleYAxis) {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            registerInParameter(inParameter);
        } else {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            inParameter.addParameter(SecondAxisTableModelInterface.class, true);
            registerInParameter(inParameter);
        }
        
    }
    
    protected boolean isDoubleYAxis(){
        return m_displayDoubleYAxis;
    }

    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor, m_displayDoubleYAxis);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {      
        final List<ExtendedTableModelInterface> dataModelInterfaceSet1 = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        SecondAxisTableModelInterface dataModelInterfaceSet2 = m_displayDoubleYAxis ? (SecondAxisTableModelInterface) m_previousDataBox.getData(false, SecondAxisTableModelInterface.class, true) : null;
        if (dataModelInterfaceSet1 != null)
            ((MultiGraphicsPanel)getDataBoxPanelInterface()).setData(dataModelInterfaceSet1, crossSelectionInterfaceL,dataModelInterfaceSet2);
    }

    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<ExtendedTableModelInterface>) data;
        ((MultiGraphicsPanel)getDataBoxPanelInterface()).setData(m_valuesList, null);
    }

}
