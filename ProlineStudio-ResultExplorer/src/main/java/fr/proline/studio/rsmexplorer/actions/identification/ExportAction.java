package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Add Action (menu for sub-actions identification and aggregation)
 * @author JM235353
 */
public class ExportAction extends AbstractRSMAction {

    private AbstractRSMAction m_exportDatasetAction; // Could be ExportDatasetAction or ExportDatasetJMSAction
    private AbstractRSMAction m_exportPrideAction;
    
    private JMenu m_menu;
    
    private AbstractTree.TreeType m_treeType;
    
    private boolean m_isJMSDefined;
    
   public ExportAction(AbstractTree.TreeType treeType, boolean isJMSDefined) {
       super(NbBundle.getMessage(ExportAction.class, "CTL_ExportAction"), treeType);
       m_treeType = treeType;
       m_isJMSDefined = isJMSDefined;
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));              
    
        if (m_isJMSDefined) {
            m_exportDatasetAction = new ExportDatasetJMSAction(m_treeType);
        } else {
            m_exportDatasetAction = new ExportDatasetAction(m_treeType);
        }
        
        
        if (m_isJMSDefined) {
            m_exportPrideAction = new ExportRSM2PrideJMSAction();
        } else {
            m_exportPrideAction = new ExportRSM2PrideAction();
        }

        JMenuItem exportDatasetItem = new JMenuItem(m_exportDatasetAction);
        JMenuItem exportPrideItem = new JMenuItem(m_exportPrideAction);
        
        m_menu.add(exportDatasetItem);
        m_menu.add(exportPrideItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_exportDatasetAction.updateEnabled(selectedNodes);
        m_exportPrideAction.updateEnabled(selectedNodes);
        
        
        boolean isEnabled = m_exportDatasetAction.isEnabled() ||  m_exportPrideAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}