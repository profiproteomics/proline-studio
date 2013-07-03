package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
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

   private JMenu m_menu;
    
   public DisplayRsetAction() {
       super(NbBundle.getMessage(AddAction.class, "CTL_DisplayRsetAction"));
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayRsetPeptidesAction = new DisplayRsetPeptidesAction();
        m_displayRsetProteinMatchesAction = new DisplayRsetProteinMatchesAction();
        m_displayUserWindowAction = new DisplayUserWindowAction(false);
       
        JMenuItem displayRsetPeptidesItem = new JMenuItem(m_displayRsetPeptidesAction);
        JMenuItem displayRsetProteinMatchesItem = new JMenuItem(m_displayRsetProteinMatchesAction);
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        
        m_menu.add(displayRsetPeptidesItem);
        m_menu.add(displayRsetProteinMatchesItem);
        m_menu.addSeparator();
        m_menu.add(displayUserWindowItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
        m_displayRsetPeptidesAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_displayRsetPeptidesAction.isEnabled() || m_displayUserWindowAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}