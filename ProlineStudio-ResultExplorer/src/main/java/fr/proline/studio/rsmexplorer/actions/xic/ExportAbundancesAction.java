package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.identification.ExportXICAction;
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
public class ExportAbundancesAction extends AbstractRSMAction {
    
    private JMenu m_menu;
    
    private ExportXICAction m_exportXICActionIons;
    private ExportXICAction m_exportXICActionPep;
    private ExportXICAction m_exportXICActionProtSet;
    private ExportXICAction m_exportXICActionProtSetProfile;
    
    
   public ExportAbundancesAction() {
       super(NbBundle.getMessage(ExportAbundancesAction.class,"CTL_ExportXicAction"), AbstractTree.TreeType.TREE_QUANTITATION);
   }
   
   @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_exportXICActionIons = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEP_IONS);
        m_exportXICActionPep =  new ExportXICAction(ExportXICAction.ExportType.MASTER_QPEPS);
        m_exportXICActionProtSet = new  ExportXICAction(ExportXICAction.ExportType.BASIC_MASTER_QPROT_SETS);
        m_exportXICActionProtSetProfile = new ExportXICAction(ExportXICAction.ExportType.MASTER_QPROT_SETS);
       
        JMenuItem exportXICProteinSetProfileItem = new JMenuItem(m_exportXICActionProtSetProfile);
        JMenuItem exportXICProteinSetItem = new JMenuItem(m_exportXICActionProtSet);
        JMenuItem exportXICPeptideSetItem = new JMenuItem(m_exportXICActionPep);
        JMenuItem exportXICPeptideIonItem = new JMenuItem(m_exportXICActionIons);
                
        m_menu.add(exportXICPeptideIonItem);
        m_menu.add(exportXICPeptideSetItem);
        m_menu.add(exportXICProteinSetItem);
        m_menu.add(exportXICProteinSetProfileItem);
        

        return m_menu;
    }
    
   
   @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        if (selectedNodes.length != 1) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.QUANTITATION) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        DDataset d = ((DataSetData) datasetNode.getData()).getDataset();
        QuantitationMethod quantitationMethod = d.getQuantitationMethod();
        if (quantitationMethod == null) {
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        if (quantitationMethod.getAbundanceUnit().compareTo("feature_intensity") != 0) { // XIC
            setEnabled(false);
            m_menu.setEnabled(false);
            return;
        }

        setEnabled(true);
        m_menu.setEnabled(true);
    }
}
