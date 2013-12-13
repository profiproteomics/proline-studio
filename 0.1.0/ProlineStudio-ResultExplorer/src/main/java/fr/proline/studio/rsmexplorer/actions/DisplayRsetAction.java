/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DisplayRsetAction extends AbstractRSMAction {

   private DisplayRsetPeptidesAction m_displayRsetPeptidesAction;

   private JMenu m_menu;
    
   public DisplayRsetAction() {
       super(NbBundle.getMessage(AddAction.class, "CTL_DisplayRsetAction"));
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayRsetPeptidesAction = new DisplayRsetPeptidesAction();
       
        JMenuItem displayRsetPeptidesItem = new JMenuItem(m_displayRsetPeptidesAction);
        
        m_menu.add(displayRsetPeptidesItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
        m_displayRsetPeptidesAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_displayRsetPeptidesAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}