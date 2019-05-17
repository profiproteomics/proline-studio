package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.DownloadFileTask;
import fr.proline.studio.dpm.task.jms.ExportDatasetTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.exporter.ExportPrideDialog;
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
 * @author VD225637
 */
public class ExportRSM2PrideJMSAction extends AbstractRSMAction {
   
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
 
    public ExportRSM2PrideJMSAction(AbstractTree tree){
        super(NbBundle.getMessage(ExportRSM2PrideJMSAction.class, "CTL_ExportPrideAction"), tree);
    }
    
     @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];        

        final ExportPrideDialog  dialog = new ExportPrideDialog(WindowManager.getDefault().getMainWindow());
        
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


                final AbstractJMSCallback downloadCallback = new AbstractJMSCallback() {

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
                final List<String>_filePath = new ArrayList();             
                final List<String> _JMSNodeId = new ArrayList();

                AbstractJMSCallback exportCallback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {

                            String fileName = dialog.getFileName();
                            if (!fileName.endsWith(".xml") && !fileName.endsWith(".XML")) {
                                fileName += ".xml";
                            }
                            DownloadFileTask task = new DownloadFileTask(downloadCallback, fileName, _filePath.get(0), _JMSNodeId.get(0));
                            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                        } else {
                            // nothing to do
                            // failed
                            setProgress(100);
                        }
                    }
                };
                
                List<DDataset> dsets = new ArrayList<>();
                dsets.add(dataSetNode.getDataset());
                ExportDatasetTask exportTask = new ExportDatasetTask(exportCallback, dsets, null, _filePath, _JMSNodeId, ExportDatasetTask.ExporterFormat.PRIDE, dialog.getExportParams());
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(exportTask);

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
        if (nodeType != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
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
