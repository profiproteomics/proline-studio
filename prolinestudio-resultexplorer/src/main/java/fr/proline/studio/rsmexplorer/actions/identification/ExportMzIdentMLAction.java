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
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.DownloadProcessedFileTask;
import fr.proline.studio.dpm.task.jms.ExportDatasetTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.exporter.Export2MzIdentMLDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.ArrayList;
import java.util.List;

import fr.proline.studio.WindowManager;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class ExportMzIdentMLAction extends AbstractRSMAction {
       
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
      
    public ExportMzIdentMLAction(AbstractTree tree) {
        super("MzIdentML...", tree);
    }
    
        @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, final int x, final int y) {


        //Once export to file is choosen, open MzIdentMLSpecific dialog
        Export2MzIdentMLDialog mzIdentDialog = new Export2MzIdentMLDialog(WindowManager.getDefault().getMainWindow(), selectedNodes.length>1);
        
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
                                if (_filePath.size() == 1) {
                                    if (!fileName.toLowerCase().endsWith(".mzid")) {
                                        fileName += ".mzid";
                                    }
                                    DownloadProcessedFileTask task = new DownloadProcessedFileTask(downloadCallback, fileName, _filePath.get(0), _jmsNodeId.get(0));
                                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                                } else {
                                    int nb = 0;
                                    List<String> prevName = new ArrayList<>();
                                    for (String fp : _filePath) {
                                        String fn = FilenameUtils.getBaseName(fp);
                                        String dirName = mzIdentDialog.getFileName();
                                        if (!dirName.endsWith("\\")) {
                                            dirName += "\\";
                                        }
                                        m_logger.debug(" STEP 1 NEXT EXPORTED FILE "+nb+" => "+fn);
                                        //VDS TODO Get DS name
                                        Long dsId = getDatasetId(fn);
                                        String dsName = getDatasetName(dsId, selectedNodes);
                                        fn = dirName+dsName+"_"+ dsId.toString()+"_" + nb +".mzid" ;
                                        m_logger.debug(" STEP 2 NEXT EXPORTED FILE "+nb+" => "+fn);
                                        DownloadProcessedFileTask task = new DownloadProcessedFileTask(downloadCallback, fn, fp, _jmsNodeId.get(nb));
                                        nb++;
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

                    List<DDataset> dsets = new ArrayList<>();
                    for (AbstractNode node : selectedNodes) {
                        dsets.add(((DataSetNode) node).getDataset());
                    }
                    ExportDatasetTask task = new ExportDatasetTask(exportCallback, dsets, null, _filePath, _jmsNodeId, ExportDatasetTask.ExporterFormat.MZIDENTML, mzIdentDialog.getExportParams());
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                    return null;
                }
            };

        mzIdentDialog.setTask(task);
        mzIdentDialog.setLocation(x, y);
        mzIdentDialog.setVisible(true);
        
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

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        if (nbSelectedNodes < 1) {
            setEnabled(false);
            return;
        }

        for (AbstractNode node : selectedNodes) {
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
        }
            
        setEnabled(true);
    }
}
