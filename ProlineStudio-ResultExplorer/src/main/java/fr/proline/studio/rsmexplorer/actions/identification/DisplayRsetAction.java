package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree.TreeType;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action with a Menu for the Display of Rset (Search Result)
 * @author JM235353
 */
public class DisplayRsetAction extends AbstractRSMAction {

   private DisplayRsetPeptidesAction m_displayRsetPeptidesAction;
   private DisplayUserWindowAction m_displayUserWindowAction;
   private DisplayRsetProteinMatchesAction m_displayRsetProteinMatchesAction;
   private GenerateMSDiagReportAction m_msDiagReportAction;
   private ManageUserWindowsAction m_manageUserWindowsAction;
   private ArrayList<DisplaySavedWindowAction> m_displaySavedWindowActionList;

   private JMenu m_menu;
   private TreeType m_treeType;
   private Boolean m_isJMSDefined;
    
   public DisplayRsetAction(TreeType treeType, boolean isJMSDefined) {
       super(NbBundle.getMessage(DisplayRsetAction.class, "CTL_DisplayRsetAction"), treeType);
       m_treeType = treeType;
       m_isJMSDefined = isJMSDefined;
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayRsetPeptidesAction = new DisplayRsetPeptidesAction(m_treeType);
        m_displayRsetProteinMatchesAction = new DisplayRsetProteinMatchesAction(m_treeType);
        m_msDiagReportAction = new GenerateMSDiagReportAction(m_treeType, m_isJMSDefined); 
        m_manageUserWindowsAction = new ManageUserWindowsAction(WindowSavedManager.SAVE_WINDOW_FOR_RSET, m_treeType);
        m_displayUserWindowAction = new DisplayUserWindowAction(WindowSavedManager.SAVE_WINDOW_FOR_RSET, m_treeType);
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        m_displaySavedWindowActionList = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (!WindowSavedManager.hasResultSetParameter(wndSaved)) {
                continue;
            }
            String name = WindowSavedManager.getWindowName(wndSaved);
            m_displaySavedWindowActionList.add(new DisplaySavedWindowAction(name, i, m_treeType));
        }
       
        JMenuItem displayRsetPeptidesItem = new JMenuItem(m_displayRsetPeptidesAction);
        JMenuItem displayRsetProteinMatchesItem = new JMenuItem(m_displayRsetProteinMatchesAction);
        JMenuItem displayMsDiagReportItem = new JMenuItem(m_msDiagReportAction);
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        JMenuItem manageUserWindowsItem = new JMenuItem(m_manageUserWindowsAction);
                
        m_menu.add(displayRsetPeptidesItem);
        m_menu.add(displayRsetProteinMatchesItem);
        m_menu.addSeparator();
        m_menu.add(displayMsDiagReportItem);
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
        
        m_displayRsetPeptidesAction.updateEnabled(selectedNodes);
        m_displayRsetProteinMatchesAction.updateEnabled(selectedNodes);
        m_msDiagReportAction.updateEnabled(selectedNodes);
        m_displayUserWindowAction.updateEnabled(selectedNodes);
        m_manageUserWindowsAction.updateEnabled(selectedNodes);
        
        boolean listEnabled = false;
        for (int i=0;i<m_displaySavedWindowActionList.size();i++) {
            m_displaySavedWindowActionList.get(i).updateEnabled(selectedNodes);
            listEnabled |= m_displaySavedWindowActionList.get(i).isEnabled();
        }
        
        boolean isEnabled = m_displayRsetPeptidesAction.isEnabled() || m_displayRsetProteinMatchesAction.isEnabled() || m_msDiagReportAction.isEnabled() || m_displayUserWindowAction.isEnabled() || m_manageUserWindowsAction.isEnabled() || listEnabled;
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}