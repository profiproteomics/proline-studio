package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.DownloadFileTask;
import fr.proline.studio.dpm.task.ExportRSMTask;
import fr.proline.studio.export.CustomExportDialog;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a Identification Summary Action
 * 
 * @author VD225637
 */
public class ExportRSMAction extends AbstractRSMAction {
     protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
     
    public ExportRSMAction(){
        super("Export...", AbstractTree.TreeType.TREE_IDENTIFICATION);    
    }
    
        @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        

        final CustomExportDialog dialog = CustomExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), true, true);
        
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
                            if (!fileName.endsWith(".xlsx")) {
                                fileName += ".xlsx";
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

                Boolean isExportAllPSMs = dialog.exportAllPSMs();
                ExportRSMTask task = new ExportRSMTask(exportCallback, dataSetNode.getDataset(),isExportAllPSMs, _filePath);
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

        // Only one at the time
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
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
