/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.DownloadFileTask;
import fr.proline.studio.dpm.task.ExportDatasetTask;
import fr.proline.studio.dpm.task.ExportDatasetTask.ExporterFormat;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * * Export Spectra Lib from DataSet Using WC
 * @author VD225637
 */
public class ExportSpectraListAction extends AbstractRSMAction {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

     
    public ExportSpectraListAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(ExportSpectraListJMSAction.class, "CTL_ExportSpectraListAction"), treeType);
    }
    
    public ExportSpectraListAction(AbstractTree.TreeType treeType, boolean exportTitle) {
        super(NbBundle.getMessage(ExportSpectraListJMSAction.class, "CTL_ExportAction")+" "+NbBundle.getMessage(ExportSpectraListJMSAction.class, "CTL_ExportSpectraListAction") , treeType);
    }
    
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, final int x, final int y) {
        
        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        final ExportDialog  dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), false, ExporterFactory.EXPORT_SPECTRA);
    
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
                final List<String>  _filePath = new ArrayList();;

                AbstractServiceCallback exportCallback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {

                            String fileName = dialog.getFileName();
                            if (!fileName.endsWith(".tsv") && !fileName.endsWith(".TSV")) {
                                fileName += ".tsv";
                            }
                            if (_filePath.size() == 1) {
                                DownloadFileTask task = new DownloadFileTask(downloadCallback, fileName, _filePath.get(0));
                                AccessServiceThread.getAccessServiceThread().addTask(task);
                            }                            

                        } else {
                            // nothing to do
                            // failed
                            setProgress(100);
                        }
                    }
                };

                
                List<DDataset> dsets = new ArrayList<>();
                dsets.add(dataSetNode.getDataset());
                ExportDatasetTask task = new ExportDatasetTask(exportCallback, dsets, null, _filePath, ExporterFormat.SPECTRA_LIST);
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
        int nbSelectedNodes = selectedNodes.length;

        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if (nodeType != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
            setEnabled(false);
            return;
        }

        if (node.isChanging()) {
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
