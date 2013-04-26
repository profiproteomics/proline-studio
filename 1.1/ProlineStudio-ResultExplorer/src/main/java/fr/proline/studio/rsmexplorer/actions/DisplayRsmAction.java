package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DisplayRsmAction extends AbstractRSMAction {

    private  DisplayRsmPSMAction m_displayRsmPSMAction;
    private DisplayRsmPeptidesAction m_displayRsmPeptidesAction;
    private DisplayRsmProteinSetsAction m_displayRsmProteinSetsAction;

    private JMenu m_menu;
    
    public DisplayRsmAction() {
        super(NbBundle.getMessage(AddAction.class, "CTL_DisplayRsmAction"));
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_displayRsmPSMAction = new DisplayRsmPSMAction();
        m_displayRsmPeptidesAction = new DisplayRsmPeptidesAction();
        m_displayRsmProteinSetsAction = new DisplayRsmProteinSetsAction();

        JMenuItem displayRsmPSMItem = new JMenuItem(m_displayRsmPSMAction);
        JMenuItem displayRsmPeptidesItem = new JMenuItem(m_displayRsmPeptidesAction);
        JMenuItem displayRsmProteinSetsItem = new JMenuItem(m_displayRsmProteinSetsAction);

        m_menu.add(displayRsmPSMItem);
        m_menu.add(displayRsmPeptidesItem);
        m_menu.add(displayRsmProteinSetsItem);

        return m_menu;
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        m_displayRsmPeptidesAction.updateEnabled(selectedNodes);
        m_displayRsmProteinSetsAction.updateEnabled(selectedNodes);

        boolean isEnabled = m_displayRsmPeptidesAction.isEnabled() || m_displayRsmProteinSetsAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);

    }
}