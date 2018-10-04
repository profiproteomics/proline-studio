/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.DownloadFileTask;
import fr.proline.studio.dpm.task.jms.ExportDatasetTask;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.exporter.Export2MzIdentMLDialog;
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
public class ExportMzIdentMLAction extends AbstractRSMAction {
       
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
      
    public ExportMzIdentMLAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(ExportMzIdentMLAction.class, "CTL_ExportMzIdentMLAction") , treeType);
    }
    
        @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, final int x, final int y) {
        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];

        //Once export to file is choosen, open MzIdentMLSpecific dialog
        Export2MzIdentMLDialog mzIdentDialog = new Export2MzIdentMLDialog(WindowManager.getDefault().getMainWindow());
        
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
                    final List<String> _filePath = new ArrayList();
                    final List<String> _jmsNodeId = new ArrayList();

                    AbstractJMSCallback exportCallback = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {
                            if (success) {

                                String fileName = mzIdentDialog.getFileName();
                                //TODO use ExportFactory getList ... 
                                if (!fileName.toLowerCase().endsWith(".mzid")) {
                                    fileName += ".mzid";
                                }
                                if (_filePath.size() == 1) {
                                    DownloadFileTask task = new DownloadFileTask(downloadCallback, fileName, _filePath.get(0), _jmsNodeId.get(0));
                                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
                    ExportDatasetTask task = new ExportDatasetTask(exportCallback, dsets, null, _filePath, _jmsNodeId, ExportDatasetTask.ExporterFormat.MZIDENTML, mzIdentDialog.getExportParams());
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                    return null;
                }
            };

        mzIdentDialog.setTask(task);
        mzIdentDialog.setLocation(x, y);
        mzIdentDialog.setVisible(true);
        
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
