package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree.TreeType;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action for the menu to display data for Identification Summary
 * @author JM235353
 */
public class DisplayRsmAction extends AbstractRSMAction {

    private DisplayRsmPSMAction m_displayRsmPSMAction;
    private DisplayRsmPeptidesAction m_displayRsmPeptidesAction;
    private DisplayRsmProteinSetsAction m_displayRsmProteinSetsAction;
    private DisplayAdjacencyMatrixAction m_displayAdjacencyMatrixAction;
    
    private DisplayUserWindowAction m_displayUserWindowAction;
    private ManageUserWindowsAction m_manageUserWindowsAction;
    private ArrayList<DisplaySavedWindowAction> m_displaySavedWindowActionList;
    
    private JMenu m_menu;
    private TreeType m_treeType;
    
    public DisplayRsmAction(TreeType treeType) {
        super(NbBundle.getMessage(AddAction.class, "CTL_DisplayRsmAction"), treeType);
        m_treeType = treeType;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_displayRsmPSMAction = new DisplayRsmPSMAction(m_treeType);
        m_displayRsmPeptidesAction = new DisplayRsmPeptidesAction(m_treeType);
        m_displayRsmProteinSetsAction = new DisplayRsmProteinSetsAction(m_treeType);
        m_displayAdjacencyMatrixAction = new DisplayAdjacencyMatrixAction(m_treeType);
        
        m_manageUserWindowsAction = new ManageUserWindowsAction(WindowSavedManager.SAVE_WINDOW_FOR_RSM, m_treeType);
        m_displayUserWindowAction = new DisplayUserWindowAction(WindowSavedManager.SAVE_WINDOW_FOR_RSM, m_treeType);
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        m_displaySavedWindowActionList = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (!WindowSavedManager.hasResultSummaryParameter(wndSaved)) {
                continue;
            }
            String name = WindowSavedManager.getWindowName(wndSaved);
            m_displaySavedWindowActionList.add(new DisplaySavedWindowAction(name, i, m_treeType));
        }


        JMenuItem displayRsmPSMItem = new JMenuItem(m_displayRsmPSMAction);
        JMenuItem displayRsmPeptidesItem = new JMenuItem(m_displayRsmPeptidesAction);
        JMenuItem displayRsmProteinSetsItem = new JMenuItem(m_displayRsmProteinSetsAction);
        JMenuItem displayAdjacencyMatrixItem = new JMenuItem(m_displayAdjacencyMatrixAction);
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        JMenuItem manageUserWindowsItem = new JMenuItem(m_manageUserWindowsAction);


        m_menu.add(displayRsmPSMItem);
        m_menu.add(displayRsmPeptidesItem);
        m_menu.add(displayRsmProteinSetsItem);
        //m_menu.add(displayAdjacencyMatrixItem); //JPM : remove comment to see adjacency matrix
        m_menu.addSeparator();
        m_menu.add(displayUserWindowItem);
        m_menu.add(manageUserWindowsItem);
        int nbUserWindows = m_displaySavedWindowActionList.size();
        if (nbUserWindows>0) {
            m_menu.addSeparator();
        }
         for (int i = 0; i <nbUserWindows ; i++) {
            m_menu.add(new JMenuItem(m_displaySavedWindowActionList.get(i)));
        }

        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_displayRsmPSMAction.updateEnabled(selectedNodes);
        m_displayRsmPeptidesAction.updateEnabled(selectedNodes);
        m_displayRsmProteinSetsAction.updateEnabled(selectedNodes);
        m_displayAdjacencyMatrixAction.updateEnabled(selectedNodes);
        m_displayUserWindowAction.updateEnabled(selectedNodes);
        m_manageUserWindowsAction.updateEnabled(selectedNodes);
        
        boolean listEnabled = false;
        for (int i=0;i<m_displaySavedWindowActionList.size();i++) {
            m_displaySavedWindowActionList.get(i).updateEnabled(selectedNodes);
            listEnabled |= m_displaySavedWindowActionList.get(i).isEnabled();
        }

        boolean isEnabled = m_displayRsmPSMAction.isEnabled() || m_displayRsmPeptidesAction.isEnabled() || m_displayRsmProteinSetsAction.isEnabled() || m_displayUserWindowAction.isEnabled()|| m_manageUserWindowsAction.isEnabled() || listEnabled;
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);

    }
}