package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class DataboxMultiGraphicsDoubleYAxis extends AbstractDataBox {

    //private static final Logger m_logger = LoggerFactory.getLogger(DataboxMultiGraphicsDoubleYAxis.class);

    private List<ExtendedTableModelInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;

    public DataboxMultiGraphicsDoubleYAxis() {
        super(DataboxType.DataboxMultiGraphicsDoubleYAxis, DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = false;
        m_canChooseColor = false;

        // Name of this databox
        m_typeName = "Graphic double Y Axis";
        m_description = "Linear Plot";

        // Register Possible in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerInParameter(inParameter);

        inParameter.addParameter(CrossSelectionInterface.class, true);
        registerInParameter(inParameter);

//        inParameter.addParameter(SecondAxisTableModelInterface.class, false);
//        registerInParameter(inParameter);
    }

    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor, true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    /**
     * in the case of DisplayMapAlignement action, m_previousDataBox is
     * DataboxMapAlignment
     */
    @Override
    public void dataChanged() {
        final List<ExtendedTableModelInterface> valuesL = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL = (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        SecondAxisTableModelInterface valueOnYAxis2 = (SecondAxisTableModelInterface) m_previousDataBox.getData(false, SecondAxisTableModelInterface.class, false);
        if (valueOnYAxis2 != null) {
            ((MultiGraphicsPanel) getDataBoxPanelInterface()).setData(valuesL, crossSelectionInterfaceL, valueOnYAxis2);
        }
    }

    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<ExtendedTableModelInterface>) data;
        dataChanged();
    }

}
