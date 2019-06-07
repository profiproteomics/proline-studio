package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.identification.DisplaySavedWindowAction;
import fr.proline.studio.rsmexplorer.actions.identification.DisplayUserWindowAction;
import fr.proline.studio.rsmexplorer.actions.identification.ManageUserWindowsAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author MB243701
 */
public class DisplayXICAction extends AbstractRSMAction {
    
    private JMenu m_menu;
    
    private DisplayXICProteinSetAction m_displayXICProteinSetAction;
    private DisplayXICPeptideSetAction m_displayXICPeptideSetAction;
    private DisplayXICPeptideIonAction m_displayXICPeptideIonAction;
    private DisplayXICPTMSitesAction m_displayXICPtmSiteProteinAction;
    private DisplayXICPTMClusterAction m_displayXICPtmClusterProteinAction;
    private DisplayUserWindowAction m_displayUserWindowAction;
    private ManageUserWindowsAction m_manageUserWindowsAction;
    private ArrayList<DisplaySavedWindowAction> m_displaySavedWindowActionList;
    
   public DisplayXICAction(AbstractTree tree) {
       super(NbBundle.getMessage(DisplayXICAction.class,"CTL_DisplayXicAction"), tree);
   }
   
   @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayXICProteinSetAction = new DisplayXICProteinSetAction(getTree());
        m_displayXICPeptideSetAction = new DisplayXICPeptideSetAction(getTree());
        m_displayXICPeptideIonAction = new DisplayXICPeptideIonAction(getTree());
        m_displayXICPtmSiteProteinAction = new DisplayXICPTMSitesAction(getTree());
        m_displayXICPtmClusterProteinAction = new DisplayXICPTMClusterAction(getTree());
        m_manageUserWindowsAction = new ManageUserWindowsAction(WindowSavedManager.SAVE_WINDOW_FOR_QUANTI, getTree());
        m_displayUserWindowAction = new DisplayUserWindowAction(WindowSavedManager.SAVE_WINDOW_FOR_QUANTI, getTree());
       
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        m_displaySavedWindowActionList = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (!WindowSavedManager.hasQuantiParameter(wndSaved)) {
                continue;
            }
            String name = WindowSavedManager.getWindowName(wndSaved);
            m_displaySavedWindowActionList.add(new DisplaySavedWindowAction(name, i, getTree()));
        }
        
        
        JMenuItem displayXICProteinSetItem = new JMenuItem(m_displayXICProteinSetAction);
        JMenuItem displayXICPeptideSetItem = new JMenuItem(m_displayXICPeptideSetAction);
        JMenuItem displayXICPeptideIonItem = new JMenuItem(m_displayXICPeptideIonAction);
        JMenuItem displayXICPtmSiteProteinItem = new JMenuItem(m_displayXICPtmSiteProteinAction);
        JMenuItem displayXICPtmClusterProteinItem = new JMenuItem(m_displayXICPtmClusterProteinAction);
        
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        JMenuItem manageUserWindowsItem = new JMenuItem(m_manageUserWindowsAction);
                
        m_menu.add(displayXICPeptideIonItem);
        m_menu.add(displayXICPeptideSetItem);
        m_menu.add(displayXICProteinSetItem);
        m_menu.addSeparator();
        m_menu.add(displayXICPtmClusterProteinItem);
        m_menu.add(displayXICPtmSiteProteinItem);
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

        m_displayXICProteinSetAction.updateEnabled(selectedNodes);
        m_displayXICPeptideSetAction.updateEnabled(selectedNodes);
        m_displayXICPeptideIonAction.updateEnabled(selectedNodes);
        m_displayXICPtmSiteProteinAction.updateEnabled(selectedNodes);
        m_displayXICPtmClusterProteinAction.updateEnabled(selectedNodes);
        m_displayUserWindowAction.updateEnabled(selectedNodes);
        m_manageUserWindowsAction.updateEnabled(selectedNodes);
        
        boolean listEnabled = false;
        for (DisplaySavedWindowAction m_displaySavedWindowActionList1 : m_displaySavedWindowActionList) {
            m_displaySavedWindowActionList1.updateEnabled(selectedNodes);
            listEnabled |= m_displaySavedWindowActionList1.isEnabled();
        }
        
        boolean isEnabled = m_displayXICProteinSetAction.isEnabled() || m_displayXICPtmSiteProteinAction.isEnabled()|| m_displayXICPtmClusterProteinAction.isEnabled()  || m_displayXICPeptideSetAction.isEnabled() || m_displayXICPeptideIonAction.isEnabled() || m_displayUserWindowAction.isEnabled() || m_manageUserWindowsAction.isEnabled() || listEnabled;
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}
