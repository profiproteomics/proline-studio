package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataboxManager;
import fr.proline.studio.pattern.ParameterDistance;
import fr.proline.studio.table.AbstractTableAction;
import java.awt.Component;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class DisplayMenuAction extends AbstractTableAction {

    private JMenu m_menu = null;

    public DisplayMenuAction() {
        super("Display");
        m_menu = new JMenu("Display");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        // nothing to do
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        setEnabled(true);
    }

    @Override
    public Component getPopupPresenter() {
        return m_menu;
    }


    
    public void populate(AbstractDataBox dataBox) {

        m_menu.removeAll();

        boolean hasSubActions = false;
            
        TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleDataboxList(dataBox, dataBox.getImportantInParameterClass());
        Iterator<ParameterDistance> it = dataBoxMap.descendingKeySet().iterator();
        while (it.hasNext()) {
            AbstractDataBox destDatabox = dataBoxMap.get(it.next());
            DisplayViewInNewWindowAction displayAction = new DisplayViewInNewWindowAction(dataBox, destDatabox);
            JMenuItem displayOptionMenuItem = new JMenuItem(displayAction);
            m_menu.add(displayOptionMenuItem);
            hasSubActions = true;
        }
        
        m_menu.setEnabled(hasSubActions);

    }
    
    
}
