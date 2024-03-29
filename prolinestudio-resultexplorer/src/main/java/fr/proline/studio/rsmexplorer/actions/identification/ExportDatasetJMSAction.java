/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.DownloadProcessedFileTask;
import fr.proline.studio.dpm.task.jms.ExportDatasetTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.GetExportInformationTask;
import fr.proline.studio.export.CustomExportDialog;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.LoadWaitingDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import fr.proline.studio.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a dataset action. The dataset could be identification, SC or XIC
 *
 * @author MB243701
 */
public class ExportDatasetJMSAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<String> m_config;

    public ExportDatasetJMSAction(AbstractTree tree) {
        super("Excel...", tree);
    }

    public ExportDatasetJMSAction(AbstractTree tree, boolean exportTitle) {
        super("Export Excel...", tree);
    }

    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, final int x, final int y) {

        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        m_config = new ArrayList();
        String waitingTxt = "Please wait while loading template to configure export";
        final LoadWaitingDialog loadWaitingDialog = new LoadWaitingDialog(WindowManager.getDefault().getMainWindow(), waitingTxt);

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
                AbstractJMSCallback getExportCallback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        final CustomExportDialog dialog = CustomExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), selectedNodes.length == 1);
                        loadWaitingDialog.setVisible(false);
                        if (success) {
                            if (!m_config.isEmpty()) {
                                String conf = m_config.get(0);
                                boolean mustUpdateConfig = dialog.setDefaultExportConfig(conf);
                                dialog.updateFileExport(mustUpdateConfig);
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

                                            String fileName = dialog.getFileName();
                                            String extension = dialog.getFileExtension();
                                            if (extension != null && !fileName.endsWith("." + extension)) {
                                                fileName += "." + extension;
                                            }
                                            if (_filePath.size() == 1) {
                                                DownloadProcessedFileTask task = new DownloadProcessedFileTask(downloadCallback, fileName, _filePath.get(0), _jmsNodeId.get(0));
                                                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                                            } else {
                                                int nb = 1;
                                                //WARNING : This code parse the returned filename from Proline Server ExportRSM Service : It assume
                                                // the returned name is formated as : <prefix>-<DS_ID>_random.<ext> and for multi export, will be modified as 
                                                // Before <prefix>-<DS_ID>_<DS_name>_index.<ext> 
                                                // After <DS_name>_<DS__ID>_index.<ext> : with <DS_name> limited to 20 characters.
                                                for (String fp : _filePath) {
                                                    int idFileName = fp.lastIndexOf("\\");
                                                    if (idFileName == -1) {
                                                        idFileName = fp.lastIndexOf("/");
                                                    }
                                                    int idUnderscore = fp.lastIndexOf("_");
                                                    int idExtC = fileName.lastIndexOf(".");
                                                    String fn = fileName;
                                                    if (idFileName != -1 && idUnderscore != -1 && idExtC != -1) { 
                                                        //VDS: used ? Could we have more than 1 _filePath and dialog.isFileExportMode 
                                                        fn = fileName.substring(0, idExtC) + "_" + fp.substring(idFileName + 1, idUnderscore) + "." + extension;
                                                    }
                                                    if (!dialog.isFileExportMode()) { //multi export or tsv : VDS fn will be erased
                                                        String dirName = dialog.getFileName();
                                                        if (!dirName.endsWith("\\")) {
                                                            dirName += "\\";
                                                        }
                                                       
                                                        //get DS Id and DS Name from fileName
                                                        Long dsId = getDatasetId(fp.substring(idFileName+1));
                                                        String dsName = getDatasetName(dsId, selectedNodes);
                                                        if (dsName.length()>20) {
                                                            dsName = dsName.substring(0, 20);// keep only 20 first chars
                                                        }
//                                                        String t = fp.substring(fileNameIndex + 1, idUnderscore);
//                                                        int id2 = t.lastIndexOf("/");
//                                                        if (id2 != -1) {
//                                                            t = t.substring(id2 + 1);
//                                                        }
//                                                        fn = dirName+t+dsName+nb+"."+extension;
                                                        fn = dirName + dsName+"_"+ dsId.toString()+"_" + nb + "." + extension;
                                                    }
                                                    nb++;
                                                    DownloadProcessedFileTask task = new DownloadProcessedFileTask(downloadCallback, fn, fp, _jmsNodeId.get(nb - 2));
                                                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
                                List<DDataset> listDataset = new ArrayList();
                                for (AbstractNode node : selectedNodes) {
                                    listDataset.add(((DataSetNode) node).getDataset());
                                }
                                ExportDatasetTask task = new ExportDatasetTask(exportCallback, listDataset, exportConfig, _filePath, _jmsNodeId);
                                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                                return null;
                            }
                        };

                        dialog.setTask(task);
                        dialog.setLocation(x, y);

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                dialog.setVisible(true);
                            }

                        });

                    }
                };
                GetExportInformationTask task = new GetExportInformationTask(getExportCallback, dataSetNode.getDataset(), m_config);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                return null;
            }

        };
        loadWaitingDialog.setTask(loadConfigTasktask);
        loadWaitingDialog.setLocation(x, y);
        loadWaitingDialog.setVisible(true);

    }

    private String getDatasetName(Long dsId, AbstractNode[] selectedNodes) {
        for (AbstractNode node : selectedNodes) {
            if (((DataSetNode) node).getDataset().getId() == dsId) {
                return  ((DataSetNode) node).getDataset().getName();
            }
        }
        return "";
    }
    
    /**
     * Return the DS Id by parsing the server exported fileName
     * @param fileName : name is formated as : <prefix>-<DS_ID>_random.<ext> 
     * @return 
     */
    private Long getDatasetId(String fileName) {        
        Long dsId = -1L;
        int id0 = fileName.indexOf("-");
        int id1 = fileName.lastIndexOf("_");
        if (id0 > -1 && id1 > -1 && id0 < id1) {
            String dsIdStr = fileName.substring(id0 + 1, id1);
            try {
                dsId = Long.parseLong(dsIdStr);
            } catch (NumberFormatException e) {
                dsId = -1L;
            }
        }
        return dsId;
    }
//    public String getDatasetName(String fileName, AbstractNode[] selectedNodes) {
//        int id0 = fileName.indexOf("-");
//        int id1 = fileName.lastIndexOf("_");
//        if (id0 > -1 && id1 > -1 && id0 < id1) {
//            String dsIdStr = fileName.substring(id0 + 1, id1);
//            try {
//                Long dsId = Long.parseLong(dsIdStr);
//                for (AbstractNode node : selectedNodes) {
//                    if (((DataSetNode) node).getDataset().getId() == dsId) {
//                        return "_" + ((DataSetNode) node).getDataset().getName() + "_";
//                    }
//                }
//            } catch (NumberFormatException e) {
//
//            }
//
//        }
//        return "";
//    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        if (nbSelectedNodes < 1) {
            setEnabled(false);
            return;
        }

        int mode = -1;
        if (selectedNodes[0] instanceof DataSetNode) {
            mode = ((DataSetNode) selectedNodes[0]).isQuantSC() ? 1 : (((DataSetNode) selectedNodes[0]).isQuantitation() ? 2 : 0);
        }
        for (AbstractNode node : selectedNodes) {
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

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

            // node must have same nature (identification, xic or sc)
            int currMode = (datasetNode.isQuantSC() ? 1 : (datasetNode.isQuantitation() ? 2 : 0));
            if (currMode != mode) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);
    }
}
