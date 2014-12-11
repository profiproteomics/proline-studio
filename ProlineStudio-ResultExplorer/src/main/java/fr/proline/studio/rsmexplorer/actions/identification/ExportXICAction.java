package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.DownloadFileTask;
import fr.proline.studio.dpm.task.ExportXICTask;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve previously computed spectral count and display it
 *
 * @author VD225637
 *
 */
public class ExportXICAction extends AbstractRSMAction {

    public enum ExportType {

        MASTER_QPEP_IONS("master_quant_peptide_ions", "Quant Peptides ions"), MASTER_QPEPS("master_quant_peptides", "Quant Peptides"), BASIC_MASTER_QPROT_SETS("basic_master_quant_protein_sets", "Quant Proteins Sets"), MASTER_QPROT_SETS("master_quant_protein_sets", "Quant Proteins Sets Profiles");

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
        super(NbBundle.getMessage(ExportXICAction.class, "CTL_PrefixExportXICAction") + type.getAsLabel() + "...", AbstractTree.TreeType.TREE_QUANTITATION);
        m_exportType = type;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        final int posx = x;
        final int posy = y;
        // Only Ref should be selected to compute SC         
        final DataSetNode xicDatasetNode = (DataSetNode) selectedNodes[0];
        //dataset
        final DDataset dataSet = xicDatasetNode.getDataset();

        // call back for loading masterQuantChannel
        AbstractDatabaseCallback masterQuantChannelCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    // check if profilizer has already been launched
                    if ((m_exportType.equals(ExportType.MASTER_QPROT_SETS)) && dataSet.getPostQuantProcessingConfig() == null) {
                        String message = "No Compute Quantitative Profiles has been launched on this XIC.\nSome data may be empty.\nDo you want to continue?";
                        OptionDialog yesNoDialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Export profile", message);
                        yesNoDialog.setLocation(posx, posy);
                        yesNoDialog.setVisible(true);
                        if (yesNoDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                            return;
                        }
                    }
                    final ExportDialog dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), false, ExporterFactory.EXPORT_XIC);

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
                                        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
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

                            String templateName = "";
                            String fileName = dialog.getFileName();
                            boolean isXLSX = fileName.endsWith(".xlsx");
                            if (m_exportType.equals(ExportType.BASIC_MASTER_QPROT_SETS)) {
                                templateName = isXLSX ? "BASIC_MASTER_QUANT_PROTEIN_SETS_XLSX" : "BASIC_MASTER_QUANT_PROTEIN_SETS_TSV";
                            } else if (m_exportType.equals(ExportType.MASTER_QPROT_SETS)) {
                                templateName = isXLSX ? "MASTER_QUANT_PROTEIN_SETS_XLSX" : "MASTER_QUANT_PROTEIN_SETS_TSV";
                            } else if (m_exportType.equals(ExportType.MASTER_QPEPS)) {
                                templateName = isXLSX ? "MASTER_QUANT_PEPTIDES_XLSX" : "MASTER_QUANT_PEPTIDES_TSV";
                            } else if (m_exportType.equals(ExportType.MASTER_QPEP_IONS)) {
                                templateName = isXLSX ? "MASTER_QUANT_PEPTIDE_IONS_XLSX" : "MASTER_QUANT_PEPTIDE_IONS_TSV";
                            }
                            ExportXICTask task = new ExportXICTask(exportCallback, xicDatasetNode.getDataset(), m_exportType.getAsParameter(), _filePath, templateName);
                            AccessServiceThread.getAccessServiceThread().addTask(task);

                            return null;
                        }
                    };

                    dialog.setTask(task);
                    dialog.setLocation(posx, posy);
                    dialog.setVisible(true);
                }
            }

        };
        DatabaseDataSetTask loadTask = new DatabaseDataSetTask(masterQuantChannelCallback);
        loadTask.initLoadQuantitation(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject(), dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(loadTask);

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
