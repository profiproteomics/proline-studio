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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserPanel;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNodeInitListener;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.HelpUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author JM235353
 */
public class SelectRawFilesPanel extends JPanel implements XICRunNodeInitListener {

    private static final String USER_DEFINED_ASSOCIATION = "User Defined Association";
    private static final String MISSING_ASSOCIATION = "Missing Association";
    private static final String AUTOMATIC_ASSOCIATION = "System Proposed Association";
    private static final String PREVIOUS_LINK_ASSOCIATION = "Previous Association Proposed";
    private static final String EXISTING_ASSOCIATION = "Existing Association in Database";
    private static final String NOT_INITIALIZED_ASSOCIATION = "Not Initialized Association";

    private static SelectRawFilesPanel m_singleton = null;

    private FlatDesignTableModel m_model = null;
    private FlatDesignTable m_table = null;
    private XICDropZone m_dropZone;
    private XICDropZoneInfo m_dropZoneInfo;
    private static final String[] SUFFIX = {".raw", ".mzdb", ".wiff"};
    private final TreeFileChooserTransferHandler m_transferHandler;
    private AbstractNode m_rootNode;
    JButton m_cleanAssciatBt;

    public static SelectRawFilesPanel getPanel(AbstractNode rootNode) {
        if (m_singleton == null) {
            m_singleton = new SelectRawFilesPanel();
        }
        m_singleton.setRootNode(rootNode);//init data
        return m_singleton;
    }

    public static SelectRawFilesPanel getPanel() {
        return m_singleton;
    }

    private SelectRawFilesPanel() {
        m_transferHandler = new TreeFileChooserTransferHandler();

        JPanel mainPanel = createMainPanel();

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

    }

    private void setRootNode(AbstractNode rootNode) {
        this.m_rootNode = rootNode;
        m_model.setData(rootNode);
    }

    public boolean check(DefaultDialog dialog) {

        int nbRows = m_model.getRowCount();
        for (int i = 0; i < nbRows; i++) {
            XICRunNode run = m_model.getXICRunNode(i);
            if ((run == null) || (!((RunInfoData) ((AbstractNode) run).getData()).hasRawFile())) {
                dialog.highlight(m_table, m_table.getCellRect(m_table.convertRowIndexToView(i), m_table.convertColumnIndexToView(FlatDesignTableModel.COLTYPE_RAW_FILE), true));
                return false;
            }
        }
        return true;
    }

    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel designTablePanel = createDesignTablePanel();
        JPanel filePathPanel = createFilePathPanel();

        JPanel framePanel = new JPanel(new GridBagLayout());
        framePanel.setBorder(BorderFactory.createTitledBorder(" MS files association "));

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(designTablePanel);
        sp.setRightComponent(filePathPanel);
        sp.setResizeWeight(0.5);

        final GridBagConstraints cFrame = new GridBagConstraints();
        cFrame.insets = new java.awt.Insets(5, 5, 5, 5);

        cFrame.gridx = 0;
        cFrame.gridy = 0;
        cFrame.weightx = 1;
        cFrame.weighty = 1;
        cFrame.anchor = GridBagConstraints.NORTHWEST;
        cFrame.fill = GridBagConstraints.BOTH;
        cFrame.gridwidth = 1;
        framePanel.add(sp, cFrame);

        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }

    private JPanel createDesignTablePanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(2, 1));

        JPanel designTablePanel = new JPanel();

        designTablePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 0, 5, 0);

        c.gridx = 0;
        c.gridy = 0;
        {
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;

            m_cleanAssciatBt = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
            m_cleanAssciatBt.setFocusPainted(false);
            m_cleanAssciatBt.setOpaque(true);
            m_cleanAssciatBt.setToolTipText("Disassociate mzDB Raw file - Mascot dat file");
            m_cleanAssciatBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Point p = m_cleanAssciatBt.getLocationOnScreen();
                    cleanRawFileDialog(p.x, p.y);
                }
            });

            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.add(m_cleanAssciatBt);
            designTablePanel.add(toolbar, c);//set it top 
            c.gridy++;
            designTablePanel.add(Box.createVerticalBox(), c);
            c.gridy = 0;
            c.gridheight = 2;
            c.gridx++;
        }

        c.weightx = 1;
        c.weighty = 1;

        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.getViewport().setBackground(Color.white);

        m_table = new FlatDesignTable();
        m_model = new FlatDesignTableModel();
        m_model.setXICDropZone(m_dropZone);
        m_table.setModel(m_model);
        //m_table.getColumnModel().getColumn(FlatDesignTableModel.COLTYPE_ASSOCIATION_SOURCE).setCellRenderer(new LinkAssociationRenderer());
        m_table.setColumnsVisibility();
        tableScrollPane.setViewportView(m_table);

        designTablePanel.add(tableScrollPane, c);

        panel.add(designTablePanel);
        panel.add(createDropZonePanel());

        return panel;
    }

    private JPanel createDropZonePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2, 5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Automatic MS file association"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        m_dropZoneInfo = new XICDropZoneInfo();
        m_dropZone = new XICDropZone(m_transferHandler);
        m_dropZone.setTable(m_table);
        m_dropZone.setDropZoneInfo(m_dropZoneInfo);
        panel.add(m_dropZoneInfo);
        panel.add(m_dropZone);
        return panel;
    }

    public void resetDropZonePanel() {
        if (m_dropZone != null) {
            m_dropZone.clearDropZone();
        }
    }

    public void pruneDesignTree() {
        Enumeration<TreeNode> e = this.m_rootNode.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            // TODO avoid this cast due to AbstractNode API modification (jdk13)
            AbstractNode currentElement = (AbstractNode)e.nextElement();
            if (currentElement.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                XICBiologicalSampleAnalysisNode sampleAnalysisNode = (XICBiologicalSampleAnalysisNode) currentElement;
                sampleAnalysisNode.removeAllChildren();
            }
        }
    }

    private JPanel createFilePathPanel() {
        JPanel fileFilePathPanel = new JPanel();

        fileFilePathPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        TreeFileChooserPanel tree = new TreeFileChooserPanel(ServerFileSystemView.getServerFileSystemView(), m_transferHandler);
        tree.initTree();
        tree.restoreTree(TreeUtils.TreeType.XIC);
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(tree);

        fileFilePathPanel.add(treeScrollPane, c);

        return fileFilePathPanel;
    }

    @Override
    public void initCompleted(XICRunNode node) {
        if (m_model != null && m_model.getRowCount() > 0) {
            m_dropZone.updateTable(); //Ne faudrait-il pas un updateRow que updateTable => update complet a chaque ligne ? 
        }
    }

    private void cleanRawFileDialog(int posX, int posY) {
        OptionDialog yesNoDialog = new OptionDialog(null, "Clean Raw File", "Erase all associated Row file except DB-Linded");
        yesNoDialog.setIconImage(IconManager.getImage(IconManager.IconType.ERASER));
        yesNoDialog.setLocation(posX, posY);
        yesNoDialog.setVisible(true);
        if (yesNoDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            m_model.cleanRawFile();
            resetDropZonePanel();
            repaint();
        }
    }

    private class FlatDesignTable extends DecoratedMarkerTable implements MouseListener {

        public FlatDesignTable() {
            setDragEnabled(true);
            setDropMode(DropMode.ON);
            setTransferHandler(m_transferHandler);
            addMouseListener(this);
        }

        private void setColumnsVisibility() {
            List<TableColumn> columns = getColumns(true);
            TableColumnExt columnExt = (TableColumnExt) columns.get(FlatDesignTableModel.COLTYPE_RAW_FILE_DIRECTORY);
            if (columnExt != null) {
                columnExt.setVisible(false);
            }
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            int columnM = this.convertColumnIndexToModel(column);
            switch (columnM) {
                case FlatDesignTableModel.COLTYPE_ASSOCIATION_SOURCE:
                    return new LinkAssociationRenderer();
                default:
                    return super.getCellRenderer(convertRowIndexToModel(row), columnM);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {

                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());

                if (row == -1 || col == -1) {
                    return;
                }

                XICRunNode runNode = m_model.getXICRunNode(convertRowIndexToModel(row), convertColumnIndexToModel(col));

                if (runNode == null || runNode.isChanging()) {
                    return;
                }

                RunInfoData runInfoData = (RunInfoData) runNode.getData();

                if (runInfoData.getLinkedRawFile() != null) {
                    return; // already a registered raw file, the user can no longer change it
                }

                HashMap<String, RawFile> potentialRawFiles = runInfoData.getPotentialRawFiles();
                if (potentialRawFiles == null) {
                    return;
                }

                SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateQuantitationDialog.getDialog(null));
                selectRawFileDialog.init(potentialRawFiles, runInfoData);
                //selectRawFileDialog.centerToWindow(CreateQuantitationDialog.getDialog(null));
                int x = (int) (CreateQuantitationDialog.getDialog(null).getLocationOnScreen().getX() + CreateQuantitationDialog.getDialog(null).getWidth() / 2);
                int y = (int) (CreateQuantitationDialog.getDialog(null).getLocationOnScreen().getY());
                selectRawFileDialog.setLocation(x, y);
                selectRawFileDialog.setVisible(true);
                if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                    if (rawFile != null) {
                        runInfoData.setSelectedRawFile(rawFile);
                        runInfoData.setStatus(RunInfoData.Status.USER_DEFINED);
                        runInfoData.setRun(rawFile.getRuns().get(0));

                        m_model.fireTableDataChanged();
                        m_model.updatePotentialsListForMissings();
                    }
                }

            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }

    protected static class FlatDesignTableModel extends DecoratedTableModel implements TreeFileChooserTableModelInterface {

        public static final int COLTYPE_GROUP = 0;
        public static final int COLTYPE_SAMPLE = 1;
        public static final int COLTYPE_SAMPLE_ANALYSIS = 2;
        public static final int COLTYPE_RAW_FILE = 3;
        public static final int COLTYPE_RAW_FILE_DIRECTORY = 4;
        public static final int COLTYPE_PEAKLIST = 5;
        public static final int COLTYPE_ASSOCIATION_SOURCE = 6;

        private static final String[] columnNames = {"Group", "Sample", "Sample Analysis", "mzDB File", "mzDB File Path", "Peaklist", "Association Source"};

        private XICDropZone m_dropZone = null;

        private final ArrayList<NodeModelRow> m_dataList = new ArrayList<>();
        /**
         * HashMap<rowIndex, FileName String set>
         */
        private final HashMap<Integer, HashSet<String>> m_potentialFileNameForMissings;

        public FlatDesignTableModel() {
            m_potentialFileNameForMissings = new HashMap<>();
        }

        public void setXICDropZone(XICDropZone dropZone) {
            m_dropZone = dropZone;
        }

        public XICDropZone getXICDropZone() {
            return m_dropZone;
        }
        
        public void setData(AbstractNode rootNode) {
            m_dataList.clear();
            parseTree(rootNode);//init tableModel data
            fireTableDataChanged();

        }

        private void parseTree(AbstractNode node) {

            if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) {
                parseGroup((XICBiologicalGroupNode) node);
            } else {
                int nbChildren = node.getChildCount();
                for (int i = 0; i < nbChildren; i++) {
                    AbstractNode child = (AbstractNode) node.getChildAt(i);
                    parseTree(child);
                }
            }
        }

        private void parseGroup(XICBiologicalGroupNode groupNode) {
            int nbChildren = groupNode.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                AbstractNode child = (AbstractNode) groupNode.getChildAt(i);
                if (child.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) {
                    parseSample(groupNode, (XICBiologicalSampleNode) child);
                }
            }
        }

        private void parseSample(XICBiologicalGroupNode groupNode, XICBiologicalSampleNode sampleNode) {
            int nbChildren = sampleNode.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                AbstractNode child = (AbstractNode) sampleNode.getChildAt(i);
                if (child.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {

                    XICBiologicalSampleAnalysisNode sampleAnalysisNode = (XICBiologicalSampleAnalysisNode) child;

                    if (sampleAnalysisNode.getXicRunNode() == null) {
                        XICRunNode runNode = new XICRunNode(new RunInfoData(), (DefaultTreeModel) IdentificationTree.getCurrentTree().getModel());
                        runNode.addXICRunNodeInitListener(m_singleton);
                        //HACK new method so that runNode is available later when we want to retrieve the potential matching raw files! calls super.add()
                        sampleAnalysisNode.addXicRunNode(runNode, true);
                        runNode.init(sampleAnalysisNode.getDataset(), this);//retrive data from database
                    } else {
                        sampleAnalysisNode.add(sampleAnalysisNode.getXicRunNode());
                    }

                    parseRun(groupNode, sampleNode, (XICBiologicalSampleAnalysisNode) child);

                }
            }
        }

        private void parseRun(XICBiologicalGroupNode groupNode, XICBiologicalSampleNode sampleNode, XICBiologicalSampleAnalysisNode sampleAnalysisNode) {

            int nbChildren = sampleAnalysisNode.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                AbstractNode child = (AbstractNode) sampleAnalysisNode.getChildAt(i);
                if (child.getType() == AbstractNode.NodeTypes.RUN) {
                    m_dataList.add(new NodeModelRow(groupNode, sampleNode, sampleAnalysisNode, (XICRunNode) child));
                }
            }
        }

        @Override
        public int getRowCount() {
            if (m_dataList == null) {
                return 0;
            }
            return m_dataList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case COLTYPE_GROUP:
                case COLTYPE_SAMPLE:
                case COLTYPE_SAMPLE_ANALYSIS:
                case COLTYPE_RAW_FILE:
                case COLTYPE_RAW_FILE_DIRECTORY:
                case COLTYPE_PEAKLIST: {
                    return String.class;
                }
                case COLTYPE_ASSOCIATION_SOURCE: {
                    return RunInfoData.Status.class;
                }
                default:
                    return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            NodeModelRow nodeModelRow = m_dataList.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_GROUP: {
                    return nodeModelRow.m_group.toString();
                }
                case COLTYPE_SAMPLE: {
                    return nodeModelRow.m_sample.toString();
                }
                case COLTYPE_SAMPLE_ANALYSIS: {
                    return nodeModelRow.m_sampleAnalysis.toString();
                }
                case COLTYPE_RAW_FILE: {
                    return nodeModelRow.m_run.toString();
                }
                case COLTYPE_RAW_FILE_DIRECTORY: {
                    RunInfoData infoD = (RunInfoData) nodeModelRow.m_run.getData();
                    String directory = "";
                    if (infoD != null) {
                        if (infoD.getLinkedRawFile() != null) { //SYSTEM_PROPOSED = database
                            directory = infoD.getLinkedRawFile().getRawFileDirectory();
                        } else if (infoD.getRawFileOnDisk() != null) {
                            File f = infoD.getRawFileOnDisk(); //USER_DEFINED
                            //need not to test
                            directory = Paths.get(f.getPath()).getParent().toString();
                        } else if (infoD.getSelectedRawFile() != null) {//LAST_DEFINED
                            directory = infoD.getSelectedRawFile().getRawFileDirectory();
                        }
                    }
                    return directory;
                }
                case COLTYPE_PEAKLIST: {
                    //VDS Use cache in NodeModelRow
                    if (nodeModelRow == null) {
                        return "<html><font color='#FF0000'>NodeModelRow is null</font></html>";
                    } else {
                        return nodeModelRow.getName();
                    }
                }
                case COLTYPE_ASSOCIATION_SOURCE: {

                    RunInfoData info = (RunInfoData) nodeModelRow.m_run.getData();

                    if (info != null) {
                        return info.getStatus();
                    } else {
                        return RunInfoData.Status.MISSING;
                    }

                }

            }

            return null; // should not happen
        }

        public XICRunNode getXICRunNode(int row) {
            NodeModelRow nodeModelRow = m_dataList.get(row);
            return nodeModelRow.m_run;
        }

        public XICRunNode getXICRunNode(int row, int col) {
            NodeModelRow nodeModelRow = m_dataList.get(row);
            return nodeModelRow.m_run;
        }

        public XICBiologicalSampleAnalysisNode getXICBiologicalSampleAnalysisNode(int row) {
            NodeModelRow nodeModelRow = m_dataList.get(row);
            return nodeModelRow.m_sampleAnalysis;
        }

        @Override
        public String getToolTipForHeader(int col) {
            return getColumnName(col);
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;
        }

        /**
         * In order to Drag & Down used by
         * TreeFileChooserTableModelInterface.importData() used also by
         * XICDropZone
         *
         * @param fileList
         * @param rowIndex
         */
        @Override
        public void setFiles(ArrayList<File> fileList, int rowIndex) {
            int nbFiles = fileList.size();
            int nbRows = getRowCount();
            for (int i = rowIndex; i < rowIndex + nbFiles; i++) {
                if (i >= nbRows) {
                    break;
                }
                NodeModelRow nodeModelRow = m_dataList.get(i);

                final int _curRow = i;
                ((RunInfoData) nodeModelRow.m_run.getData()).setStatus(RunInfoData.Status.USER_DEFINED);
                nodeModelRow.m_run.setRawFile(fileList.get(i - rowIndex), new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fireTableRowsUpdated(_curRow, _curRow);
                        updatePotentialsListForMissings();
                    }
                });
            }
        }

        public void updatePotentialsListForMissings() {

            m_potentialFileNameForMissings.clear();

            int numberOfRows = this.getRowCount();

            for (int i = 0; i < numberOfRows; i++) {

                NodeModelRow nodeModelRow = m_dataList.get(i);
                XICRunNode currentXicNode = nodeModelRow.m_run;
                if (currentXicNode != null) {

                    RunInfoData info = (RunInfoData) currentXicNode.getData();

                    if (info.getStatus() == RunInfoData.Status.MISSING) {
                        HashMap<String, RawFile> rawFiles = info.getPotentialRawFiles();
                        HashSet<String> set = new HashSet<>();

                        if (info.hasPotentialRawFiles()) {
                            for (String key : rawFiles.keySet()) {
                                set.add(HelpUtils.getFileName(key.toLowerCase(), SUFFIX));
                            }
                        } else if (info.getPeakListPath() != null) {
                            set.add(HelpUtils.getFileName(info.getPeakListPath().toLowerCase(), SUFFIX));
                        }
                        m_potentialFileNameForMissings.put(i, set);
                    }

                }

            }
        }

        public HashMap<Integer, HashSet<String>> getPotentialFilenamesForMissings() {
            return m_potentialFileNameForMissings;
        }

        @Override
        public boolean shouldConfirmCorruptFiles(ArrayList<Integer> indices) {
            for (int i = 0; i < indices.size(); i++) {
                RunInfoData.Status status = ((RunInfoData) getXICRunNode(indices.get(i)).getData()).getStatus();
                if (status == RunInfoData.Status.LAST_DEFINED) { // || status == RunInfoData.Status.SYSTEM_PROPOSED){
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canSetFiles(ArrayList<Integer> indices) {
            for (int i = 0; i < indices.size(); i++) {
                if (((RunInfoData) getXICRunNode(indices.get(i)).getData()).getStatus() == RunInfoData.Status.LINKED_IN_DATABASE) {
                    return false;
                }
            }
            return true;
        }

        private void cleanRawFile() {
            if (this.m_dataList != null) {
                for (NodeModelRow node : m_dataList) {
                    RunInfoData userObject = (RunInfoData) node.m_run.getData();
                    RunInfoData.Status status = userObject.getStatus();
                    if (status != RunInfoData.Status.LINKED_IN_DATABASE) {//else not allowed
                        RawFile linkedFile = userObject.getSelectedRawFile();
                        if (linkedFile != null) {
                            userObject.addPotentialRawFiles(linkedFile);
                        }
                        userObject.setStatus(RunInfoData.Status.MISSING);
                        userObject.setSelectedRawFile(null);
                        userObject.setMessage("<html><font color='#FF0000'>Missing Raw File</font></html>");
                    }
                }
                updatePotentialsListForMissings();
                fireTableDataChanged();
            }
        }

    }

    private class LinkAssociationRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            this.setHorizontalTextPosition(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.BOTTOM);

            if (value instanceof RunInfoData.Status) {

                RunInfoData.Status status = (RunInfoData.Status) value;

                switch (status) {
                    case MISSING:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.DELETE));
                        this.setToolTipText(MISSING_ASSOCIATION);
                        break;
                    case LAST_DEFINED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.TARGET));
                        this.setToolTipText(PREVIOUS_LINK_ASSOCIATION);
                        break;
                    case SYSTEM_PROPOSED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.TARGET));
                        this.setToolTipText(AUTOMATIC_ASSOCIATION);
                        break;
                    case USER_DEFINED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.USER));
                        this.setToolTipText(USER_DEFINED_ASSOCIATION);
                        break;
                    case LINKED_IN_DATABASE:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.DATABASES_RELATION));
                        this.setToolTipText(EXISTING_ASSOCIATION);
                        break;
                    case NOT_INITIALIZED:
                        this.setIcon(IconManager.getIcon(IconManager.IconType.DELETE));
                        this.setToolTipText(NOT_INITIALIZED_ASSOCIATION);
                        break;
                }

            }

            this.setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
                this.setForeground(Color.WHITE);
                //this.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
            } else {
                this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
                this.setForeground(Color.BLACK);
                //this.setBorder(BorderFactory.createEmptyBorder());
            }

            return this;
        }

    }

    private static class NodeModelRow {

        private XICBiologicalGroupNode m_group;
        private XICBiologicalSampleNode m_sample;
        private XICBiologicalSampleAnalysisNode m_sampleAnalysis;
        private XICRunNode m_run;
        private String m_name;

        public NodeModelRow(XICBiologicalGroupNode group, XICBiologicalSampleNode sample, XICBiologicalSampleAnalysisNode sampleAnalysis, XICRunNode run) {
            m_group = group;
            m_sample = sample;
            m_sampleAnalysis = sampleAnalysis;
            m_run = run;
            m_name = HelpUtils.getFileName(this.m_sampleAnalysis.getResultSet().getMsiSearch().getPeaklist().getPath(), SUFFIX);
        }

        public XICBiologicalSampleAnalysisNode getXICBiologicalSampleAnalysisNode() {
            return m_sampleAnalysis;
        }

        public String getName() {
            return m_name;
        }
    }

}
