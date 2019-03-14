package fr.proline.studio.pattern.xic;

import java.util.List;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceGraphicPanel;

/**
 *
 * @author Karine XUE
 */
public class DataboxXicAbundanceGraphic extends AbstractDataBox {

//    private List<ExtendedTableModelInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;

    public DataboxXicAbundanceGraphic() {        
        super(DataboxType.DataboxXicAbundanceGraphic, DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = false;
        m_canChooseColor = false;

        // Name of this databox
        m_typeName = "Graphic";
        m_description = "Linear Plot";

        // Register Possible in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class, true);
        inParameter.addParameter(CrossSelectionInterface.class, true);
        inParameter.addParameter(XicAbundanceProteinTableModel.class, false);
        registerInParameter(inParameter);

    }

    @Override
    public void createPanel() {
        XicAbundanceGraphicPanel p = new XicAbundanceGraphicPanel(m_defaultLocked, m_canChooseColor);
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
        
        XicAbundanceProteinTableModel proteinAbundance = (XicAbundanceProteinTableModel) m_previousDataBox.getData(false, XicAbundanceProteinTableModel.class);

        ((XicAbundanceGraphicPanel) getDataBoxPanelInterface()).setData(valuesL, crossSelectionInterfaceL, proteinAbundance);
    }

//    @Override
//    public void setEntryData(Object data) {
//        m_valuesList = (List<ExtendedTableModelInterface>) data;
//        dataChanged();
//    }

}
