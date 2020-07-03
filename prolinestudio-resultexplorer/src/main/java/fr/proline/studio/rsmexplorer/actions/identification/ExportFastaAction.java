/* 
 * Copyright (C) 2019 VD225637
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

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.RetrieveBioSeqTask;
import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_HELP;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author KX257079
 */
public class ExportFastaAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ExportFastaAction");
    //private LoadWaitingDialog m_loadWaitingDialog;

    public ExportFastaAction(AbstractTree tree) {
        super(NbBundle.getMessage(ExportDatasetJMSAction.class, "CTL_ExportFastaAction"), tree);
    }

    public ExportFastaAction(AbstractTree tree, boolean exportTitle) {
        super(NbBundle.getMessage(ExportFastaAction.class, "CTL_ExportAction") + " " + NbBundle.getMessage(ExportFastaAction.class, "CTL_ExportFastaAction"), tree);
    }

    /**
     * open a dialog to define de export file location
     *
     * @param selectedNodes
     * @param x
     * @param y
     */
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, final int x, final int y) {
        String waitingTxt = "Please wait while loading template to configure export";
//        m_loadWaitingDialog = new LoadWaitingDialog(WindowManager.getDefault().getMainWindow(), waitingTxt);
//        m_loadWaitingDialog.setLocation(x, y);
//        m_loadWaitingDialog.setVisible(true);

        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        FastaExportDialog fastaExportDialog = new FastaExportDialog(WindowManager.getDefault().getMainWindow(), dataSetNode);
        DefaultDialog.ProgressTask exportFastaProgressTask = new ExportFastaProgressTask(dataSetNode, fastaExportDialog);
        fastaExportDialog.setTask(exportFastaProgressTask);
        fastaExportDialog.setLocation(x, y);
        fastaExportDialog.setVisible(true);
//        m_loadWaitingDialog.setVisible(false);
    }

    /**
     * enable which node can has export fasta action
     *
     * @param selectedNodes
     */
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        if (nbSelectedNodes != 1) {//don't allow multiple
            setEnabled(false);
            return;
        }

        int mode = -1;
        if (selectedNodes[0] instanceof DataSetNode) {
            mode = ((DataSetNode) selectedNodes[0]).isQuantSC() ? 1 : (((DataSetNode) selectedNodes[0]).isQuantXIC() ? 2 : 0);
        }
        for (AbstractNode node : selectedNodes) {
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            AbstractNode.NodeTypes nodeType = node.getType();
            //if (nodeType != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
            if (node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
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
            int currMode = (datasetNode.isQuantSC() ? 1 : (datasetNode.isQuantXIC() ? 2 : 0));
            if (currMode != mode) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);

    }

    /**
     * ProgressTask & SwingWorker which realize the export file
     */
    class ExportFastaProgressTask extends DefaultDialog.ProgressTask {

        long m_projectId;
        DataSetNode m_selectedNode;
        File m_localFile;
        BufferedWriter m_writer;
        FastaExportDialog m_dialog;

        /**
         *
         * @param dataSetNode
         * @param fastaExportDialog : in order to retrive the local export file
         * name
         */
        public ExportFastaProgressTask(DataSetNode dataSetNode, FastaExportDialog fastaExportDialog) {
            m_selectedNode = dataSetNode;
            m_dialog = fastaExportDialog;
        }

        @Override
        public int getMinValue() {
            return 0;
        }

        @Override
        public int getMaxValue() {
            return 100;
        }

        /**
         * Main work here
         *
         * @return
         * @throws Exception
         */
        @Override
        protected Object doInBackground() throws Exception {
            DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
            m_selectedNode.setIsChanging(true);
            m_projectId = m_selectedNode.getDataset().getProject().getId();
            treeModel.nodeChanged(m_selectedNode);
            m_localFile = m_dialog.getExportFile();
            DDataset dataSet = ((DataSetData) m_selectedNode.getData()).getDataset();
            extraitRsmSequenceRetrived();
            //retriveBioSequence(dataSet);
            return null;
        }
        final String SEQUENCE_RETRIVED = "\"is_coverage_updated\":true";

        private void extraitRsmSequenceRetrived() {
            DDataset dataSet = ((DataSetData) m_selectedNode.getData()).getDataset();
            ResultSummary rsm = dataSet.getResultSummary();
            if (rsm == null) {
                m_logger.debug("->rsm is null");
                long rsmId = dataSet.getResultSummaryId();
                loadResultSummary(dataSet);
            } else {
                m_logger.debug("->rsm is ok");
                String properties = rsm.getSerializedProperties();

                if (properties.contains(SEQUENCE_RETRIVED) || m_selectedNode.isBioRetrived()) {
                    m_logger.debug("-->rsm : sequenceRetrived");
                    //write bio sequence
                    extraitProteinSet(rsm);
                } else {//not sur,perhaps sequenceRetrived, but properties has not updated, need a reloadResultSummay
                    m_logger.debug("-->rsm : sequenceRetrived");
                    retriveBioSequence(dataSet);

                }
            }
        }

        private void loadResultSummary(DDataset dataSet) {
            m_logger.debug("->loadResultSummary");
            boolean isReload;
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    m_logger.debug("->call back loadResultSummary");
                    if (success) {
                        String properties = dataSet.getResultSummary().getSerializedProperties();

                        if (properties.contains(SEQUENCE_RETRIVED)) {
                            extraitProteinSet(dataSet.getResultSummary());
                        } else {
                            retriveBioSequence(dataSet);
                        }
                    } else {
                        setProgress(100);
                    }

                }
            };

            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        }

        private void retriveBioSequence(DDataset dataSet) {
            Long projectId = dataSet.getProject().getId();
            List rsmIds = new ArrayList();
            rsmIds.add(dataSet.getResultSummaryId());
            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (!success) {
                        String msg = this.getTaskError().getErrorTitle();

                        InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "JMSTask Error",
                                "Error: " + msg + "\nnProtein Sequence can't be retrived, verify that the Sequence Repository module is installed and started");
                        errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
                        errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                        errorDialog.setVisible(true);
                        m_selectedNode.setIsChanging(false);
                        DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
                        treeModel.nodeChanged(m_selectedNode);

                    } else {
                        m_logger.debug("--->retriveBioSequence ok");
                        ResultSummary rsm = dataSet.getResultSummary();
                        //loadResultSummary(dataSet);

                        rsm.clearMemory();//clean in order to have updated ProteinSetArray
                        m_selectedNode.setBioRetrived(true);
                        loadProteinSet(rsm);
                    }
                }
            };

            RetrieveBioSeqTask task = new RetrieveBioSeqTask(callback, rsmIds, projectId, true);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        }

        private void extraitProteinSet(ResultSummary rsm) {
            m_logger.debug("-->extraitProteinSet");
            ResultSummary.TransientData td = rsm.getTransientData(TransientMemoryCacheManager.getSingleton());

            DProteinSet[] proteinSetArray;
            if (td != null && td.getProteinSetArray() != null) {
                m_logger.debug("--->proteinSetArray is ok");
                writeBioSequence(td.getProteinSetArray());
            } else {
                m_logger.debug("--->proteinSetArray is null");
                loadProteinSet(rsm);
            }
        }

        private void loadProteinSet(ResultSummary rsm) {
            m_logger.debug("---->loadProteinSet");
            AbstractDatabaseCallback callback;
            callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    m_logger.debug("----->call back loadProteinSet");
                    if (success) {

                        DProteinSet[] proteinSetArray = rsm.getTransientData(TransientMemoryCacheManager.getSingleton()).getProteinSetArray();
                        String p = rsm.getSerializedProperties();
                        writeBioSequence(proteinSetArray);
                        setProgress(100);
                    } else {
                        setProgress(100);
                        //unregisterTask(taskId);
                        //addDataChanged(ExtendedTableModelInterface.class);
                    }
                }
            };

            //DatabaseProteinMatchesTask task = new DatabaseProteinMatchesTask(callback, m_projectId, _rset); //this can retreive protein bio sequence 
            // ask asynchronous loading of data
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSets(m_projectId, rsm);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);//push task to run
        }

        private void writeBioSequence(DProteinSet[] proteinSetArray) {
            m_logger.debug("->writeBioSequence");
            if (m_localFile.exists()) {
                //@todo prompt rewrite
            }

            try {
                m_writer = new BufferedWriter(new FileWriter(m_localFile));
                int lineLength = 60;
                for (DProteinSet prot : proteinSetArray) {

                    DProteinMatch typicalProtM = prot.getTypicalProteinMatch();
                    if (typicalProtM != null) {
                        String name = typicalProtM.getAccession();
                        String description = typicalProtM.getDescription();
                        DBioSequence bioSequence = typicalProtM.getDBioSequence();
                        String sequence = "";
                        if (bioSequence != null) {
                            sequence = bioSequence.getSequence();
                        }
                        int seqLength = sequence.length();
                        int start, stop;
                        m_writer.append(">");
                        m_writer.append(name);
                        m_writer.append(" ");
                        m_writer.append(description);
                        m_writer.newLine();
                        start = 0;
                        for (int i = 0; i < seqLength / lineLength; i++) {
                            stop = (i + 1) * lineLength;
                            m_writer.append(sequence.substring(start, stop));
                            m_writer.newLine();
                            start = stop;
                        }
                        if (start < seqLength) {
                            m_writer.append(sequence.substring(start));
                            m_writer.newLine();
                        }
                    }
                }
                m_writer.close();

            } catch (IOException ex) {
                //@todo Exceptions.printStackTrace(ex);
            } finally {
                m_selectedNode.setIsChanging(false);
                DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
                treeModel.nodeChanged(m_selectedNode);
            }

        }

    }

    /**
     * Dialog to configurer export file name
     */
    class FastaExportDialog extends DefaultDialog {

        JFileChooser _fchooser;
        String EXT_FASTA = "fasta";
        private DefaultDialog.ProgressTask m_task = null;
        DataSetNode _dataSetN;
        File _localFile;
        JTextField _fileTextField;

        private FastaExportDialog(Window parent, DataSetNode dataSetNode) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);
            _dataSetN = dataSetNode;
            setResizable(true);
            setTitle("Export Fasta");
            setButtonVisible(BUTTON_HELP, true);
            //@todo 
            setDocumentationSuffix("id.37m2jsg");

            Preferences preferences = NbPreferences.root();
            _localFile = new File(preferences.get("DefaultFastaExportPath", System.getProperty("user.home"))
                    + File.separator + _dataSetN.toString() + "." + EXT_FASTA);

            _fchooser = new JFileChooser(_localFile);//longtime
            //m_loadWaitingDialog.setVisible(false);
            FileNameExtensionFilter FILTER_FASTA = new FileNameExtensionFilter("Fasta File (." + EXT_FASTA + ")", EXT_FASTA);

            _fchooser.addChoosableFileFilter(FILTER_FASTA);
            _fchooser.setMultiSelectionEnabled(false);
            setInternalComponent(initComponent(_localFile));

            setButtonName(BUTTON_OK, "Export");

        }

        /**
         * @param defaultExportPath
         * @return
         */
        private JPanel initComponent(File defaultExportPath) {
            final JPanel insidePanel = new JPanel(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            JLabel lblExportToFile = new JLabel("Export to file:");
            insidePanel.add(lblExportToFile, c);

            c.gridx++;
            c.weightx = 1;
            _fileTextField = new JTextField(50);
            _fileTextField.setText(defaultExportPath.getAbsolutePath());
            insidePanel.add(_fileTextField, c);
            c.weightx = 0;

            final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
            addFileButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String textFile = _fileTextField.getText().trim();
                    if (textFile.length() > 0) {
                        File currentFile = new File(textFile);
                        if (currentFile.isDirectory()) {
                            _fchooser.setCurrentDirectory(currentFile);
                        }
                    }

                    int result = _fchooser.showOpenDialog(addFileButton);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        _fileTextField.setText(_fchooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            c.gridx++;
            insidePanel.add(addFileButton, c);
            return insidePanel;
        }

        @Override
        protected boolean okCalled() {
            _localFile = new File(_fileTextField.getText().trim());
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultFastaExportPath", _localFile.getAbsoluteFile().getParentFile().getAbsolutePath());
            m_task.execute();
            return true;
        }

        public void setTask(DefaultDialog.ProgressTask task) {
            m_task = task;
        }

        public File getExportFile() {
            return _localFile;
        }
    }
}
