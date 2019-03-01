package fr.proline.studio.pattern.xic;

import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.pattern.*;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PTMProteinSiteTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceGraphicPanel;

/**
 *
 * @author Karine XUE
 */
public class DataboxXicAbundanceGraphic extends DataboxMultiGraphics  {
 
    private List<ExtendedTableModelInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;
    
    public DataboxXicAbundanceGraphic() {
       this(false, false);
    }
    
    public DataboxXicAbundanceGraphic(boolean defaultLocked, boolean canChooseColor) {
         super(defaultLocked, canChooseColor);
    }
    
    @Override
    public void createPanel() {
        XicAbundanceGraphicPanel p = new XicAbundanceGraphicPanel(m_defaultLocked, m_canChooseColor);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    /**
     * in the case of DisplayMapAlignement action, m_previousDataBox is DataboxMapAlignment
     */
    @Override
    public void dataChanged() {
        final List<ExtendedTableModelInterface> valuesL = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        XicAbundanceProteinTableModel proteinAbundance =  (XicAbundanceProteinTableModel) m_previousDataBox.getData(false, XicAbundanceProteinTableModel.class, false);
        
        ((XicAbundanceGraphicPanel)getDataBoxPanelInterface()).setData(valuesL, crossSelectionInterfaceL, proteinAbundance);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<ExtendedTableModelInterface>) data;
        dataChanged();
    }
    
}
