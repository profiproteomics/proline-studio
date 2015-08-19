package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action to display the experimental design: menu with exp. design parameters
 * and map alignment
 *
 * @author MB243701
 */
public class DisplayExperimentalDesignAction extends AbstractRSMAction {

    private AbstractRSMAction m_xicParamAction;
    private AbstractRSMAction m_mapAlignmentAction;

    private JMenu m_menu;

    public DisplayExperimentalDesignAction() {
        super(NbBundle.getMessage(DisplayExperimentalDesignAction.class, "CTL_DisplayExperimentalDesignAction"), AbstractTree.TreeType.TREE_QUANTITATION);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));              
    
        m_xicParamAction = new DisplayXicParamAction();
        m_mapAlignmentAction = new DisplayMapAlignmentAction();

        JMenuItem xicParamItem = new JMenuItem(m_xicParamAction);
        JMenuItem mapAlignItem = new JMenuItem(m_mapAlignmentAction);
        
        m_menu.add(xicParamItem);
        m_menu.add(mapAlignItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_xicParamAction.updateEnabled(selectedNodes);
        m_mapAlignmentAction.updateEnabled(selectedNodes);
        
        
        boolean isEnabled = m_xicParamAction.isEnabled() ||  m_mapAlignmentAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}
