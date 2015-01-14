/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
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
    
   public DisplayXICAction() {
       super(NbBundle.getMessage(DisplayXICAction.class,"CTL_DisplayXicAction"), AbstractTree.TreeType.TREE_QUANTITATION);
   }
   
   @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayXICProteinSetAction = new DisplayXICProteinSetAction();
        m_displayXICPeptideSetAction = new DisplayXICPeptideSetAction();
        m_displayXICPeptideIonAction = new DisplayXICPeptideIonAction();
       
        JMenuItem displayXICProteinSetItem = new JMenuItem(m_displayXICProteinSetAction);
        JMenuItem displayXICPeptideSetItem = new JMenuItem(m_displayXICPeptideSetAction);
        JMenuItem displayXICPeptideIonItem = new JMenuItem(m_displayXICPeptideIonAction);
                
        m_menu.add(displayXICPeptideIonItem);
        m_menu.add(displayXICPeptideSetItem);
        m_menu.add(displayXICProteinSetItem);
        

        return m_menu;
    }
    
   
   @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // only one node selected
        if (selectedNodes.length != 1) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        // the node must not be in changing state
        if (node.isChanging()) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        // must be a dataset 
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        // must be a quantitation XIC
        if (! datasetNode.isQuantXIC()) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        setEnabled(true);
        m_menu.setEnabled(true);
    }
}
