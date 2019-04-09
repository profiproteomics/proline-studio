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

     // Could be ExportXXXAction or ExportXXXJMSAction
    private AbstractRSMAction m_exportDatasetAction; 
    private AbstractRSMAction m_exportPrideAction;
    private AbstractRSMAction m_exportPeakViewSpectraAction;
    private AbstractRSMAction m_exportSpectronautSpectraAction;
    private AbstractRSMAction m_exportMzIdentMLAction;
    
    private JMenu m_menu;
    
    private AbstractTree.TreeType m_treeType;

    
   public ExportAction(AbstractTree.TreeType treeType) {
       super(NbBundle.getMessage(ExportAction.class, "CTL_ExportAction"), treeType);
       m_treeType = treeType;
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));              

        m_exportDatasetAction = new ExportDatasetJMSAction(m_treeType);

        if (AbstractTree.TreeType.TREE_IDENTIFICATION.equals(m_treeType)) {
            m_exportPrideAction = new ExportRSM2PrideJMSAction(m_treeType);
            m_exportMzIdentMLAction = new ExportMzIdentMLAction(m_treeType);
        } else {
            m_exportPrideAction = null;
            m_exportMzIdentMLAction = null;
        }
        
        
        JMenuItem exportDatasetItem = new JMenuItem(m_exportDatasetAction);
        m_menu.add(exportDatasetItem);
        if(m_exportPrideAction!=null){
            JMenuItem exportPrideItem = new JMenuItem(m_exportPrideAction);
            m_menu.add(exportPrideItem);
        }
        
        if(m_exportMzIdentMLAction!=null){
            JMenuItem exportMzIdentMLItem = new JMenuItem(m_exportMzIdentMLAction);
            m_menu.add(exportMzIdentMLItem);
        }

        JMenu exportMenu = new JMenu(NbBundle.getMessage(ExportAction.class, "CTL_ExportSpectraListAction"));
        m_exportPeakViewSpectraAction = new ExportSpectraListJMSAction(m_treeType, ExportSpectraListJMSAction.FormatCompatibility.PeakView);
        m_exportSpectronautSpectraAction = new ExportSpectraListJMSAction(m_treeType, ExportSpectraListJMSAction.FormatCompatibility.Spectronaut);
        
        JMenuItem exportSpectraItem = new JMenuItem(m_exportPeakViewSpectraAction);        
        exportMenu.add(exportSpectraItem);
        exportSpectraItem = new JMenuItem(m_exportSpectronautSpectraAction);        
        exportMenu.add(exportSpectraItem);
        
        m_menu.add(exportMenu);        

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_exportDatasetAction.updateEnabled(selectedNodes);
        if(m_exportPrideAction!=null){
            m_exportPrideAction.updateEnabled(selectedNodes);
        }
        if(m_exportMzIdentMLAction!=null){
            m_exportMzIdentMLAction.updateEnabled(selectedNodes);
        }        
        
        m_exportSpectronautSpectraAction.updateEnabled(selectedNodes);
        m_exportPeakViewSpectraAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_exportDatasetAction.isEnabled() || 
                (m_exportPrideAction != null && m_exportPrideAction.isEnabled()) || 
                m_exportSpectronautSpectraAction.isEnabled() || 
                m_exportPeakViewSpectraAction.isEnabled() || 
                (m_exportMzIdentMLAction != null && m_exportMzIdentMLAction.isEnabled() );
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}