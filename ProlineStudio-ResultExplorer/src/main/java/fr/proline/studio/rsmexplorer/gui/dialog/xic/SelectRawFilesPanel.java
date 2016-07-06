package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.TreeFileChooserPanel;
import fr.proline.studio.gui.TreeFileChooserTableModelInterface;
import fr.proline.studio.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.MiscellaneousUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class SelectRawFilesPanel extends JPanel {

    private static SelectRawFilesPanel m_singleton = null;

    private FlatDesignTableModel m_model = null;
    private FlatDesignTable m_table = null;
    private Hashtable<String, XICBiologicalSampleAnalysisNode> m_hashtable;
    private XICDropZone m_dropZone;

    public static SelectRawFilesPanel getPanel(AbstractNode rootNode) {
        if (m_singleton == null) {
            m_singleton = new SelectRawFilesPanel();
        }

        m_singleton.setRootNode(rootNode);

        return m_singleton;
    }

    public static SelectRawFilesPanel getPanel() {
        return m_singleton;
    }

    private SelectRawFilesPanel() {
        m_hashtable = new Hashtable<String, XICBiologicalSampleAnalysisNode>();

        JPanel wizardPanel = createWizardPanel();
        JPanel mainPanel = createMainPanel();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(wizardPanel, c);

        c.gridy++;
        c.weighty = 1;

        add(mainPanel, c);

    }

    private void setRootNode(AbstractNode rootNode) {
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
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel designTablePanel = createDesignTablePanel();
        JPanel filePathPanel = createFilePathPanel();

        JPanel framePanel = new JPanel(new GridBagLayout());
        framePanel.setBorder(BorderFactory.createTitledBorder(" XIC Design "));

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(designTablePanel);
        sp.setRightComponent(filePathPanel);
        sp.setResizeWeight(0.5);

        final GridBagConstraints cFrame = new GridBagConstraints();
        cFrame.insets = new java.awt.Insets(5, 5, 5, 5);

        cFrame.gridx = 0;
        cFrame.gridy = 0;
        cFrame.gridwidth = 2;
        cFrame.weightx = 1;
        cFrame.weighty = 0;
        cFrame.anchor = GridBagConstraints.NORTH;
        cFrame.fill = GridBagConstraints.NONE;
        framePanel.add(new JLabel("Drag & Drop", IconManager.getIcon(IconManager.IconType.DRAG_AND_DROP), JLabel.LEADING), cFrame);

        cFrame.anchor = GridBagConstraints.NORTHWEST;
        cFrame.fill = GridBagConstraints.BOTH;
        cFrame.gridwidth = 1;
        cFrame.gridy++;
        cFrame.weighty = 1;
        framePanel.add(sp, cFrame);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }

    private JPanel createWizardPanel() {
        JPanel wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel wizardLabel = new JLabel("<html><b>Step 2:</b> Drag and Drop File Path.</html>");
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);

        return wizardPanel;
    }

    private JPanel createDesignTablePanel() {
        JPanel panel = new JPanel();
        //panel.setLayout(new BorderLayout());

        panel.setLayout(new GridLayout(2, 1));

        JPanel designTablePanel = new JPanel();
        //designTablePanel.setBorder(BorderFactory.createTitledBorder("Samples"));

        designTablePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.getViewport().setBackground(Color.white);

        m_table = new FlatDesignTable();
        m_model = new FlatDesignTableModel();
        m_table.setModel(m_model);

        tableScrollPane.setViewportView(m_table);

        designTablePanel.add(tableScrollPane, c);

        

        panel.add(designTablePanel);
        panel.add(this.createDropZonePanel());

        return panel;
    }
    
    private JPanel createDropZonePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        m_dropZone = new XICDropZone();
        m_dropZone.setTable(m_table);
        
        panel.add(m_dropZone, BorderLayout.CENTER);
        
        return panel;
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

        TreeFileChooserPanel tree = new TreeFileChooserPanel(ServerFileSystemView.getServerFileSystemView());
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(tree);

        fileFilePathPanel.add(treeScrollPane, c);

        return fileFilePathPanel;
    }

    private class FlatDesignTable extends DecoratedMarkerTable implements MouseListener {

        //private JPopupMenu m_popup = null;
        public FlatDesignTable() {
            setDragEnabled(true);
            setDropMode(DropMode.ON);
            setTransferHandler(new TreeFileChooserTransferHandler());

            addMouseListener(this);
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        @Override
        public TablePopupMenu initPopupMenu() {

            /*TablePopupMenu popupMenu = new TablePopupMenu();
             popupMenu.addAction(new RsetAllPanel.PropertiesFromTableAction());
            
             return popupMenu;*/
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

                if (runNode.isChanging()) {
                    return;
                }

                RunInfoData runInfoData = (RunInfoData) runNode.getData();

                if (runInfoData.getRawFileSouce().getLinkedRawFile() != null) {
                    return; // already a registered raw file, the user can no longer change it
                }

                ArrayList<RawFile> potentialRawFiles = runInfoData.getPotentialRawFiles();
                if (potentialRawFiles == null) {
                    return;
                }

                SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateXICDialog.getDialog(null));
                selectRawFileDialog.init(potentialRawFiles, runInfoData.getRawFileSouce());
                //selectRawFileDialog.centerToWindow(CreateXICDialog.getDialog(null));
                int x = (int) (CreateXICDialog.getDialog(null).getLocationOnScreen().getX() + CreateXICDialog.getDialog(null).getWidth() / 2);
                int y = (int) (CreateXICDialog.getDialog(null).getLocationOnScreen().getY());
                selectRawFileDialog.setLocation(x, y);
                selectRawFileDialog.setVisible(true);
                if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                    if (rawFile != null) {
                        runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);

                        runInfoData.setRun(rawFile.getRuns().get(0));
                        //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());  //JPM.RUNINFODATA
                        //runInfoData.setRunInfoInDatabase(true);
                        runNode.warnParent(false);
                        //((DefaultTreeModel)getModel()).nodeChanged(runNode);
                        m_model.fireTableDataChanged();
                        return;
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
        private static final String[] columnNames = {"Group", "Sample", "Sample Analysis", "Raw File"};

        private final ArrayList<NodeModelRow> m_dataList = new ArrayList<>();

        public void setData(AbstractNode rootNode) {
            m_dataList.clear();
            parseTree(rootNode);
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
        public Class getColumnClass(int col) {
            return String.class;
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

            }
            return null; // should not happen
        }

        public XICRunNode getXICRunNode(int row) {
            NodeModelRow nodeModelRow = m_dataList.get(row);
            return nodeModelRow.m_run;
        }

        public XICRunNode getXICRunNode(int row, int col) {
            if (col != COLTYPE_RAW_FILE) {
                return null;
            }
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
                nodeModelRow.m_run.setRawFile(fileList.get(i - rowIndex), new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fireTableRowsUpdated(_curRow, _curRow);
                    }
                });
            }

        }

        public Hashtable<String, Integer> getModelShortages() {
            Hashtable<String, Integer> shortagesTable = new Hashtable<String, Integer>();

            int numberOfRows = this.getRowCount();
            int numberOfColumns = this.getColumnCount();

            for (int i = 0; i < numberOfRows; i++) {
                if (this.getValueAt(i, numberOfColumns - 1).toString().contains("No Raw File")) {
                    XICBiologicalSampleAnalysisNode currentAnalysisNode = this.getXICBiologicalSampleAnalysisNode(i);
                    String[] suffix = {"raw", ".mzdb"};
                    shortagesTable.put(MiscellaneousUtils.getFileName(currentAnalysisNode.getResultSet().getMsiSearch().getPeaklist().getPath(), suffix), i);
                }
            }
            
            return shortagesTable;
        }

    }

    private static class NodeModelRow {

        private XICBiologicalGroupNode m_group;
        private XICBiologicalSampleNode m_sample;
        private XICBiologicalSampleAnalysisNode m_sampleAnalysis;
        private XICRunNode m_run;

        public NodeModelRow(XICBiologicalGroupNode group, XICBiologicalSampleNode sample, XICBiologicalSampleAnalysisNode sampleAnalysis, XICRunNode run) {
            m_group = group;
            m_sample = sample;
            m_sampleAnalysis = sampleAnalysis;
            m_run = run;
        }

        public XICBiologicalSampleAnalysisNode getXICBiologicalSampleAnalysisNode() {
            return m_sampleAnalysis;
        }
    }

}
