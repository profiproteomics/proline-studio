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
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dock.gui.InfoLabel;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.RetrieveBioSeqTask;
import fr.proline.studio.gui.DefaultDialog;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.lang3.StringUtils;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;
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
        super("Sequence Fasta", tree);
    }

    public ExportFastaAction(AbstractTree tree, boolean exportTitle) {
        super("Export Sequence Fasta", tree);
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

        final DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        FastaExportMulitipleDialog exportMultipleDialog = new FastaExportMulitipleDialog(WindowManager.getDefault().getMainWindow(), selectedNodes);

        ExportFastaProgressTask exportFastaProgressTask = new ExportFastaProgressTask(selectedNodes, exportMultipleDialog);
        exportMultipleDialog.setTask(exportFastaProgressTask);
        exportMultipleDialog.setLocation(x, y);
        exportMultipleDialog.setLocation(x, y);
        exportMultipleDialog.setVisible(true);
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

    public static enum ExportMode {
        SINGLE, MULTIPLE, CONCATENATED
    }

    /**
     * ProgressTask & SwingWorker which realize the export file
     */
    class ExportFastaProgressTask extends DefaultDialog.ProgressTask {

        final String SEQUENCE_RETRIVED = "\"is_coverage_updated\":true";
        long m_projectId;
        //DataSetNode m_selectedNode;
        FastaExportMulitipleDialog m_dialog;
        AbstractNode[] m_selectedNodes;
        ExportMode m_exportMode;
        HashMap<String, DProteinSet> m_proteinSetMap;

        /**
         *
         * @param dataSetNode
         * @param fastaExportDialog : in order to retrive the local export file
         * name
         */
        public ExportFastaProgressTask(AbstractNode[] selectedNodes, FastaExportMulitipleDialog fastaExportDialog) {
            m_selectedNodes = selectedNodes;
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
            DataSetNode firstN = (DataSetNode) m_selectedNodes[0];
            m_projectId = firstN.getDataset().getProject().getId();
            m_exportMode = m_dialog.getExportMode();
            if (m_exportMode.equals(ExportMode.SINGLE)) {//@todo add concatnated mode
                if (m_selectedNodes.length > 1) {
                    m_exportMode = ExportMode.CONCATENATED;
                    m_nombre = 0;
                    m_proteinSetMap = new HashMap();
                    DataSetNode dNode;
                    for (AbstractNode node : m_selectedNodes) {
                        dNode = (DataSetNode) node;
                        m_logger.debug("multiple export for : " + dNode.toString());
                        dNode.setIsChanging(true);
                        treeModel.nodeChanged(dNode);
                        extraitRsmSequenceRetrived(dNode);
                    }
                } else {
                    firstN.setIsChanging(true);
                    treeModel.nodeChanged(firstN);
                    extraitRsmSequenceRetrived(firstN);
                }
            } else {
                DataSetNode dNode;

                for (AbstractNode node : m_selectedNodes) {
                    dNode = (DataSetNode) node;
                    m_logger.debug("multiple export for : " + dNode.toString());
                    dNode.setIsChanging(true);
                    treeModel.nodeChanged(dNode);
                    extraitRsmSequenceRetrived(dNode);
                }

            }
            return null;
        }

        private void extraitRsmSequenceRetrived(DataSetNode node) {
            m_logger.debug("->extraitRsmSequenceRetrived: " + node.toString());
            ResultSummary rsm = node.getResultSummary();
            if (rsm == null) {
                m_logger.debug("->rsm is null");
                long rsmId = node.getResultSummaryId();
                loadResultSummary(node);
            } else {
                m_logger.debug("->rsm is ok");
                String properties = rsm.getSerializedProperties();

                if (properties != null && properties.contains(SEQUENCE_RETRIVED) || node.isBioRetrived()) {
                    m_logger.debug("-->rsm : sequenceRetrived");
                    //write bio sequence
                    extraitProteinSet(node);
                } else {//not sur,perhaps sequenceRetrived, but properties has not updated, need a reloadResultSummay
                    m_logger.debug("-->rsm : sequenceRetrived");
                    retriveBioSequence(node);

                }
            }
        }

        private void loadResultSummary(DataSetNode node) {
            DDataset dataSet = node.getDataset();
            m_logger.debug("->loadResultSummary: " + dataSet.getName());
            boolean isReload;
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    m_logger.debug("->call back loadResultSummary " + dataSet.getName());
                    if (success) {
                        String properties = dataSet.getResultSummary().getSerializedProperties();

                        if (properties != null && properties.contains(SEQUENCE_RETRIVED)) {
                            extraitProteinSet(node);
                        } else {
                            retriveBioSequence(node);
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

        private void retriveBioSequence(DataSetNode node) {
            DDataset dataSet = node.getDataset();
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
                        node.setIsChanging(false);
                        DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
                        treeModel.nodeChanged(node);

                    } else {
                        m_logger.debug("--->retriveBioSequence ok " + dataSet.getName());
                        ResultSummary rsm = dataSet.getResultSummary();
                        //loadResultSummary(dataSet);

                        rsm.clearMemory();//clean in order to have updated ProteinSetArray
                        node.setBioRetrived(true);
                        loadProteinSet(node);
                    }
                }
            };

            RetrieveBioSeqTask task = new RetrieveBioSeqTask(callback, rsmIds, projectId, true);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        }

        private void extraitProteinSet(DataSetNode node) {
            ResultSummary rsm = node.getResultSummary();
            m_logger.debug("-->extraitProteinSet " + rsm.getId());
            ResultSummary.TransientData td = rsm.getTransientData(TransientMemoryCacheManager.getSingleton());
            if (td != null && td.getProteinSetArray() != null) {
                m_logger.debug("--->proteinSetArray is ok, rsm: " + node.toString());
                if (m_exportMode.equals(ExportMode.CONCATENATED)) {
                    addProteinSet(td.getProteinSetArray());
                } else {
                    writeBioSequence(node);
                }
            } else {
                m_logger.debug("--->proteinSetArray is null");
                loadProteinSet(node);
            }
        }

        private void loadProteinSet(DataSetNode node) {

            m_logger.debug("---->loadProteinSet " + node.toString());
            ResultSummary rsm = node.getResultSummary();
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
                        m_logger.debug("----->call back success, rsm: " + rsm.getId());
                        if (m_exportMode.equals(ExportMode.CONCATENATED)) {
                            addProteinSet(proteinSetArray);
                        } else {
                            writeBioSequence(node);
                            setProgress(100);
                        }
                    } else {
                        setProgress(100);
                    }
                }
            };

            // ask asynchronous loading of data
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSets(m_projectId, rsm);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);//push task to run
        }

        private void writeBioSequence(DataSetNode node) {
            DProteinSet[] proteinSetArray = node.getResultSummary().getTransientData(TransientMemoryCacheManager.getSingleton()).getProteinSetArray();
            m_logger.debug("->writeBioSequence : " + proteinSetArray.length + " proteinSet");
            try {
                write(proteinSetArray, node);
            } catch (IOException ex) {
                WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, "IO Exception when write fasta file", ex);
            } finally {
                node.setIsChanging(false);
                DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
                treeModel.nodeChanged(node);
                m_logger.debug("write bio end " + node.toString());
            }

        }

        private void write(DProteinSet[] proteinSetArray, DataSetNode node) throws IOException {
            HashMap<DataSetNode, File> fileNameMap = m_dialog.getDataSetExportFileMap();
            File localFile = fileNameMap.get(node);
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(localFile));
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
                    bWriter.append(">");
                    bWriter.append(name);
                    bWriter.append(" ");
                    bWriter.append(description);
                    bWriter.newLine();
                    start = 0;
                    for (int i = 0; i < seqLength / lineLength; i++) {
                        stop = (i + 1) * lineLength;
                        bWriter.append(sequence.substring(start, stop));
                        bWriter.newLine();
                        start = stop;
                    }
                    if (start < seqLength) {
                        bWriter.append(sequence.substring(start));
                        bWriter.newLine();
                    }
                }
            }
            bWriter.close();

        }

        private void writeBioSequence() {
            m_logger.debug("->writeBioSequence : " + ExportMode.CONCATENATED);
            DProteinSet[] proteinSetArray = new DProteinSet[m_proteinSetMap.size()];
            TreeMap protMapsortedByName = new TreeMap(m_proteinSetMap);//ordered according to the natural ordering of its keys
            Collection<DProteinSet> protSets = protMapsortedByName.values();// returns the values in ascending order of the corresponding keys
            proteinSetArray = protSets.toArray(proteinSetArray);
            DataSetNode firstNode = (DataSetNode) m_selectedNodes[0];
            try {
                write(proteinSetArray, firstNode);
            } catch (IOException ex) {
                WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, "IO Exception when write fasta file", ex);
            } finally {
                for (AbstractNode anode : m_selectedNodes) {
                    DataSetNode node = (DataSetNode) anode;
                    node.setIsChanging(false);
                    DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
                    treeModel.nodeChanged(node);
                }
                m_logger.debug("write bio end ");
            }

        }

        int m_nombre;

        private void addProteinSet(DProteinSet[] proteinSetArray) {
            for (DProteinSet prot : proteinSetArray) {
                String accession = prot.getTypicalProteinMatch().getAccession();
                if (m_proteinSetMap.get(accession) == null) {
                    m_proteinSetMap.put(accession, prot);
                }
            }
            m_nombre++;
            if (m_nombre == m_selectedNodes.length) {
                m_logger.debug("write " + ExportMode.CONCATENATED + " fasta file : " + m_proteinSetMap.size() + " ProteinSet");
                writeBioSequence();
            }
        }

    }

    class FastaExportMulitipleDialog extends DefaultDialog {

        JFileChooser _fchooser;
        final static String EXT_FASTA = "fasta";
        final static int DATASET_NAME_LENGTH = 20; //length to be shown in label
        final static int MAX_FILENAME_LENGTH = 80;//not include directory length, default windows max path = 260, 
        FileNameExtensionFilter FILTER_FASTA = new FileNameExtensionFilter("Fasta File (." + EXT_FASTA + ")", EXT_FASTA);
        private ExportFastaProgressTask _progressTask = null;
        AbstractNode[] _nodes;
        File _localFile;
        File _localDirectory;
        JTextField _pathTextField;
        /**
         * key=dataset name, value = JTextField for fileName
         */
        HashMap<DataSetNode, JTextField> _dataSetFileNameMap;
        HashMap<DataSetNode, File> _dataSetExportFileMap;
        JCheckBox _multipleButton;

        private FastaExportMulitipleDialog(Window parent, AbstractNode[] selectedNodes) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);
            _nodes = selectedNodes;
            setResizable(true);
            setTitle("Export Fasta Multiple");

            Preferences preferences = NbPreferences.root();
            String lastPath = preferences.get("DefaultFastaExportPath", System.getProperty("user.home"));

            _dataSetFileNameMap = new HashMap();
            String fn = ((DataSetNode) _nodes[0]).toString() + "." + EXT_FASTA;
            int nbNode = selectedNodes.length;
            if (nbNode > 1) {
                fn = "";
                for (int i = 0; i < nbNode; i++) {
                    DataSetNode node = (DataSetNode) selectedNodes[i];
                    fn += node.getDataset().getName();
                    if (i < nbNode - 1) {
                        fn += "-";
                    }
                }
                if (fn.length() > MAX_FILENAME_LENGTH) {
                    fn = "";
                    int blocLength = MAX_FILENAME_LENGTH / nbNode;
                    for (int j = 0; j < nbNode; j++) {
                        DataSetNode node = (DataSetNode) selectedNodes[j];
                        String n = node.getDataset().getName();
                        fn += StringUtils.right(n, blocLength);
                        if (j < nbNode - 1) {
                            fn += "-";
                        }
                    }
                }
                fn += "." + EXT_FASTA;
            }
            _localFile = new File(lastPath + File.separator + fn);
            _fchooser = new JFileChooser(_localFile);//longtime
            _fchooser.addChoosableFileFilter(FILTER_FASTA);
            _fchooser.setMultiSelectionEnabled(false);
            _multipleButton = new JCheckBox("Mutiple Export: One fasta file for one DataSet");
            setInternalComponent(initComponent(_localFile));
            setButtonName(BUTTON_OK, "Export");

        }

        /**
         * @param defaultExportPath
         * @return
         */
        private JPanel initComponent(File defaultExportPath) {
            final JPanel insidePanel = new JPanel(new GridBagLayout());

            JPanel pathPanel = new JPanel(new GridBagLayout());
            {
                GridBagConstraints cp = new GridBagConstraints();
                cp.anchor = GridBagConstraints.NORTHWEST;
                cp.fill = GridBagConstraints.BOTH;
                cp.insets = new java.awt.Insets(5, 5, 5, 5);

                cp.gridx = 0;
                cp.gridy = 0;
                cp.weightx = 0;
                JLabel lblExportToFile = new JLabel("Export to file/Path:");
                pathPanel.add(lblExportToFile, cp);

                cp.gridx++;
                cp.weightx = 1;
                _pathTextField = new JTextField(50);
                _pathTextField.setText(defaultExportPath.getAbsolutePath());
                pathPanel.add(_pathTextField, cp);
                cp.weightx = 0;

                final JButton chooseFileBt = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
                chooseFileBt.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String textFile = _pathTextField.getText().trim();
                        if (textFile.length() > 0) {
                            File currentFile = new File(textFile);
                            _fchooser.setSelectedFile(currentFile);
                        }
                        int result = _fchooser.showOpenDialog(chooseFileBt);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            _pathTextField.setText(_fchooser.getSelectedFile().getAbsolutePath());
                        }
                    }
                });
                cp.gridx++;
                pathPanel.add(chooseFileBt, cp);
            }

            _multipleButton.setSelected(false);
            JPanel optionPane = new JPanel(new GridBagLayout());
            optionPane.setVisible(false);
            if (_nodes.length <= 1) {
                _multipleButton.setVisible(false);
                _multipleButton.setEnabled(false);
            }
            _multipleButton.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {

                        if (optionPane != null) {//normal
                            optionPane.setVisible(false);
                        }
                        String directory = _pathTextField.getText().trim();
                        String fileName = _localFile.getName();
                        File f = new File(directory + File.separator + fileName);
                        _fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        _fchooser.addChoosableFileFilter(FILTER_FASTA);
                        _pathTextField.setText(f.getAbsolutePath());

                    } else {//pass to multiple
                        String txt = _pathTextField.getText().trim();
                        _localFile = new File(txt);//keep 
                        String directory;
                        if (_localFile.isDirectory()) {//if it is an existing directory
                            directory = txt;
                        } else {
                            directory = (_localFile.getParentFile().isDirectory()) ? _localFile.getParent() : txt;
                        }
                        _localDirectory = new File(directory);
                        _pathTextField.setText(directory);
                        _fchooser.removeChoosableFileFilter(FILTER_FASTA);
                        _fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        optionPane.removeAll();
                        GridBagConstraints co = new GridBagConstraints();
                        co.anchor = GridBagConstraints.NORTHWEST;
                        co.fill = GridBagConstraints.BOTH;
                        co.insets = new java.awt.Insets(5, 5, 5, 5);
                        boolean isFirstExpand = !_dataSetFileNameMap.isEmpty();
                        co.gridy = 0;
                        int i = 0;
                        for (AbstractNode node : _nodes) {
                            DataSetNode dNode = (DataSetNode) node;
                            String dataSetName = dNode.toString();
                            JLabel lb2File = new JLabel(dataSetName + ", Export to file:");
                            co.gridx = 0;
                            optionPane.add(lb2File, co);
                            JTextField fNameField;
                            if (isFirstExpand) {
                                fNameField = _dataSetFileNameMap.get(dNode);
                            } else {
                                fNameField = new JTextField(40);
                                StringUtils.right(dataSetName, DATASET_NAME_LENGTH);
                                fNameField.setText(dataSetName + "." + EXT_FASTA);
                                _dataSetFileNameMap.put(dNode, fNameField);
                            }
                            co.gridx++;
                            optionPane.add(fNameField, co);
                            co.gridy++;
                            i++;
                        }
                        optionPane.setVisible(true);
                    }
                    FastaExportMulitipleDialog.this.revalidate();
                    FastaExportMulitipleDialog.this.repack();
                }
            });

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            insidePanel.add(pathPanel, c);
            c.gridy++;
            insidePanel.add(_multipleButton, c);
            c.gridy++;
            insidePanel.add(optionPane, c);
            return insidePanel;
        }

        @Override
        protected boolean okCalled() {
            _localFile = new File(_pathTextField.getText().trim());
            Preferences preferences = NbPreferences.root();
            String dir = _localFile.isDirectory() ? _localFile.getAbsolutePath() : _localFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            preferences.put("DefaultFastaExportPath", dir);
            _dataSetExportFileMap = new HashMap<>();
            if (!_multipleButton.isSelected()) {//single export

                if (_localFile.isDirectory()) {//not a file
                    setStatus(true, "You should give the file name to export");
                    highlight(_pathTextField);
                    return false;
                }
                if (overwriteExistFile(_localFile) == false) {
                    return false;
                }
                _dataSetExportFileMap.put((DataSetNode) this._nodes[0], _localFile);
            } else {
                String path = _pathTextField.getText().trim();
                _localDirectory = new File(path);
                if (!_localDirectory.isDirectory()) {//must an exist directory
                    setStatus(true, "Should not be a file / directory should exist");
                    highlight(_pathTextField);
                    return false;
                }

                for (DataSetNode node : _dataSetFileNameMap.keySet()) {
                    JTextField field = _dataSetFileNameMap.get(node);
                    String fName = field.getText().trim();
                    if (fName.contains(File.separator) || fName.contains("\\") || fName.contains("/")) {
                        setStatus(true, "You should give the file name without directory");
                        highlight(field);
                        return false;
                    }
                    File aFile = new File(_localDirectory + File.separator + fName);
                    if (overwriteExistFile(aFile) == false) {
                        return false;
                    }
                    _dataSetExportFileMap.put(node, aFile);
                }
            }
            _progressTask.execute();
            return true;
        }

        private boolean overwriteExistFile(File file) {
            if (file.exists()) {
                String message = "The file " + file.getName() + " already exists. Do you want to overwrite it ?";
                String title = "Overwrite ?";
                String[] options = {"Yes", "No"};
                int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                if (reply != JOptionPane.YES_OPTION) {
                    setStatus(true, "File " + file.getName() + " already exists.");
                    return false;
                }
            }
            return true;
        }

        public void setTask(ExportFastaProgressTask task) {
            _progressTask = task;
        }

        public ExportMode getExportMode() {
            if (_multipleButton.isSelected()) {
                return ExportMode.MULTIPLE;
            } else {
                return ExportMode.SINGLE;
            }
        }

        /**
         *
         * @return Hashmap key =DataSetNode, value = Export LocalFile to write
         * in
         */
        public HashMap getDataSetExportFileMap() {
            return _dataSetExportFileMap;
        }
    }

}
