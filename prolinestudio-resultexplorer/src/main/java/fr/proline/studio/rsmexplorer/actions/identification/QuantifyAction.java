package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree.TreeType;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action for the menu to display data for Identification Summary
 * @author JM235353
 */
public class QuantifyAction extends AbstractRSMAction {

    private SpectralCountAction m_spectralCountAction;
    private CreateQuantitationAction m_createXICAction;
    private CreateQuantitationAction m_createSILACAction;
    private CreateQuantitationAction m_createTMTAction;
    
    private JMenu m_menu;
    private TreeType m_treeType;
    
    public QuantifyAction(TreeType treeType) {
        super(NbBundle.getMessage(QuantifyAction.class, "CTL_QuantifyAction"), treeType);
        m_treeType = treeType;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_spectralCountAction = new SpectralCountAction();
        JMenuItem spectralCountItem = new JMenuItem(m_spectralCountAction);
        m_menu.add(spectralCountItem);
        
        m_createXICAction = new CreateQuantitationAction(m_treeType, QuantitationMethod.Type.LABEL_FREE);
        JMenuItem createXICItem = new JMenuItem(m_createXICAction);
        m_menu.add(createXICItem);

        m_createSILACAction = new CreateQuantitationAction(m_treeType, QuantitationMethod.Type.RESIDUE_LABELING);
        JMenuItem createSILACItem = new JMenuItem(m_createSILACAction);
        m_menu.add(createSILACItem);

        m_createTMTAction = new CreateQuantitationAction(m_treeType, QuantitationMethod.Type.ISOBARIC_TAGGING);
        JMenuItem createTMTItem = new JMenuItem(m_createTMTAction);
        m_menu.add(createTMTItem);
        
        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_spectralCountAction.updateEnabled(selectedNodes);
        m_createXICAction.updateEnabled(selectedNodes);
        m_createSILACAction.updateEnabled(selectedNodes);
        m_createTMTAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_spectralCountAction.isEnabled() || m_createXICAction.isEnabled() || m_createSILACAction.isEnabled() || m_createTMTAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}