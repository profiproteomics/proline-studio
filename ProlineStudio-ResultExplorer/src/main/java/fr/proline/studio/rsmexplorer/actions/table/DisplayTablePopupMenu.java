package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.table.TablePopupMenu;

/**
 *
 * @author JM235353
 */
public class DisplayTablePopupMenu extends TablePopupMenu {
    
    private DataBoxPanelInterface m_databoxProvider;
    private final DisplayMenuAction m_displayMenuAction;
    
    public DisplayTablePopupMenu(DataBoxPanelInterface databoxProvider) {
        super(true);

        m_databoxProvider = databoxProvider;

        m_displayMenuAction = new DisplayMenuAction();
        addAction(m_displayMenuAction);

        addAction(null);

        addAction(new RestrainAction() {
            @Override
            public void filteringDone() {
                m_databoxProvider.getDataBox().propagateDataChanged(CompareDataInterface.class);
            }
        });
        addAction(new ClearRestrainAction() {
            @Override
            public void filteringDone() {
                m_databoxProvider.getDataBox().propagateDataChanged(CompareDataInterface.class);
            }
        });
    }
    

    
    public void prepostPopupMenu() {
                
        AbstractDataBox databox = m_databoxProvider.getDataBox();
        m_displayMenuAction.populate( databox);
    }
}
