package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.DownloadFileTask;
import fr.proline.studio.dpm.task.ExportXICTask;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve previously computed spectral count and display it
 * @author VD225637
 * 
 */
public class ExportXICAction extends AbstractRSMAction {
 
  public enum ExportType {
        MASTER_QPEP_IONS("master_quant_peptide_ions", "Quant Peptides ions"),MASTER_QPEPS("master_quant_peptides", "Quant Peptides"), MASTER_QPROT_SETS("master_quant_protein_sets", "Quant Proteins Sets");
        
        private String m_value;
        private String m_label;

        public String getAsParameter() {
            return m_value;
        }
        
        public String getAsLabel() {
            return m_label;
        }

        private ExportType(String value, String label) {
            this.m_value = value;
            this.m_label = label;    
        }
        
    };
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private ExportType m_exportType = null;
    
    public ExportXICAction(ExportType type) {
        super(NbBundle.getMessage(ExportXICAction.class, "CTL_PrefixExportXICAction")+type.getAsLabel(), AbstractTree.TreeType.TREE_QUANTITATION);
        m_exportType = type; 
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) { 
        
        // Only Ref should be selected to compute SC         

        final DataSetNode xicDatasetNode = (DataSetNode) selectedNodes[0];
        

        final ExportDialog dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(),false);
        
        DefaultDialog.ProgressTask task = new DefaultDialog.ProgressTask() {

            @Override
            public int getMinValue() {
                return 0;
            }

            @Override
            public int getMaxValue() {
                return 100;
            }

            @Override
            protected Object doInBackground() throws Exception {


                final AbstractServiceCallback downloadCallback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {

                            setProgress(100);

                        } else {
                            // nothing to do
                            // failed
                            setProgress(100);
                        }
                    }
                };

                // used as out parameter for the service
                final String[] _filePath = new String[1];

                AbstractServiceCallback exportCallback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {

                            String fileName = dialog.getFileName();
                            if (!fileName.endsWith(".xls")) {
                                fileName += ".xls";
                            }
                            DownloadFileTask task = new DownloadFileTask(downloadCallback, fileName, _filePath[0]);
                            AccessServiceThread.getAccessServiceThread().addTask(task);

                        } else {
                            // nothing to do
                            // failed
                            setProgress(100);
                        }
                    }
                };


                ExportXICTask task = new ExportXICTask(exportCallback, xicDatasetNode.getDataset(), m_exportType.getAsParameter(), _filePath);
                AccessServiceThread.getAccessServiceThread().addTask(task);

                

                return null;
            }
        };


        dialog.setTask(task);
        dialog.setLocation(x, y);
        dialog.setVisible(true);

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
         if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;
        
        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.QUANTITATION) {
            setEnabled(false);
            return;
        }
        
        DDataset d = ((DataSetData) datasetNode.getData()).getDataset();
        QuantitationMethod quantitationMethod = d.getQuantitationMethod();
        if (quantitationMethod == null) {
            setEnabled(false);
            return;
        }

        if (quantitationMethod.getAbundanceUnit().compareTo("feature_intensity") != 0) { // XIC
            setEnabled(false);
            return;
        }
        
        setEnabled(true);        
    }

}
