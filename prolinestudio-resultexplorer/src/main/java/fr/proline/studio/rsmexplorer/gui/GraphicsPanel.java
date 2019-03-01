package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.graphics.BaseGraphicsPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.event.ActionListener;

/**
 *
 * @author JM235353
 */
public class GraphicsPanel extends BaseGraphicsPanel implements DataBoxPanelInterface {

    
    private AbstractDataBox m_dataBox;
    
    public GraphicsPanel(boolean dataLocked) {
        super(dataLocked);
    }
    
    @Override
    public void addSingleValue(Object v) {
        // should not be called
    }
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }


}
