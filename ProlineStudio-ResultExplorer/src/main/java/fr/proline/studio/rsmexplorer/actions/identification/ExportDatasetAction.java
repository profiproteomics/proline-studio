/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.DownloadFileTask;
import fr.proline.studio.dpm.task.ExportDatasetTask;
import fr.proline.studio.dpm.task.GetExportInformationTask;
import fr.proline.studio.export.CustomExportDialog;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.LoadWaitingDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.ArrayList;
import java.util.List;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a dataset action. The dataset could be identification, SC or XIC
 *
 * @author MB243701
 */
public class ExportDatasetAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<String> m_config;

    public ExportDatasetAction(AbstractTree.TreeType treeType) {
        super("Export...", treeType);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, final int x, final int y) {

        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        m_config = new ArrayList();
        final LoadWaitingDialog loadWaitingDialog = new LoadWaitingDialog(WindowManager.getDefault().getMainWindow());

        DefaultDialog.ProgressTask loadConfigTasktask = new DefaultDialog.ProgressTask() {
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
                AbstractServiceCallback exportCallback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        final CustomExportDialog dialog = CustomExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), false);
                        loadWaitingDialog.setVisible(false);
                        if (success) {
                            if (!m_config.isEmpty()) {
                                String conf = m_config.get(0);
                                dialog.setDefaultExportConfig(conf);
                            }
                        } else {
                            // nothing to do
                            // failed
                        }

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
                                final List<String> _filePath = new ArrayList();

                                AbstractServiceCallback exportCallback = new AbstractServiceCallback() {

                                    @Override
                                    public boolean mustBeCalledInAWT() {
                                        return true;
                                    }

                                    @Override
                                    public void run(boolean success) {
                                        if (success) {

                                            String fileName = dialog.getFileName();
                                            String extension = dialog.getFileExtension();
                                            if (extension != null && !fileName.endsWith("."+extension)) {
                                                fileName += "."+extension;
                                            }
                                            if (_filePath.size() == 1){
                                                DownloadFileTask task = new DownloadFileTask(downloadCallback, fileName, _filePath.get(0));
                                                AccessServiceThread.getAccessServiceThread().addTask(task);
                                            }else {
                                                for (String _filePath1 : _filePath) {
                                                    int id = _filePath1.lastIndexOf("\\");
                                                    int idUnderscore = _filePath1.lastIndexOf("_");
                                                    int idExtC = fileName.lastIndexOf(".");
                                                    String fn = fileName;
                                                    if (id != -1 && idUnderscore != -1 && idExtC != -1 ){
                                                        fn = fileName.substring(0, idExtC) +"_"+_filePath1.substring(id+1, idUnderscore)+"."+extension;
                                                    }
                                                    DownloadFileTask task = new DownloadFileTask(downloadCallback, fn, _filePath1);
                                                    AccessServiceThread.getAccessServiceThread().addTask(task);
                                                }
                                            }

                                        } else {
                                            // nothing to do
                                            // failed
                                            setProgress(100);
                                        }
                                    }
                                };

                                String exportConfig = dialog.getExportConfig();
                                ExportDatasetTask task = new ExportDatasetTask(exportCallback, dataSetNode.getDataset(), exportConfig, _filePath);
                                AccessServiceThread.getAccessServiceThread().addTask(task);

                                return null;
                            }
                        };

                        dialog.setTask(task);
                        dialog.setLocation(x, y);
                        dialog.setVisible(true);
                    }
                };
                GetExportInformationTask task = new GetExportInformationTask(exportCallback, dataSetNode.getDataset(), m_config);
                AccessServiceThread.getAccessServiceThread().addTask(task);
                return null;
            }

        };
        loadWaitingDialog.setTask(loadConfigTasktask);
        loadWaitingDialog.setLocation(x, y);
        loadWaitingDialog.setVisible(true);

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // Only one at the time
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        AbstractNode.NodeTypes nodeType = node.getType();
        if (nodeType != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        // We can only export a RSM
        DataSetNode datasetNode = (DataSetNode) node;
        if (!datasetNode.hasResultSummary()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
