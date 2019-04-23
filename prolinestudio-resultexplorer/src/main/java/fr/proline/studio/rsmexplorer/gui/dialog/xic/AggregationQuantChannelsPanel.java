/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.QCMappingTreeTableModel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.DQuantitationChannelMapping;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.QCMappingTransferHandler;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;

/**
 *
 * @author CB205360
 */
public class AggregationQuantChannelsPanel extends JPanel {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private static AggregationQuantChannelsPanel m_singleton;
    private MappingTreeTable m_treeTable;
    private QCMappingTreeTableModel m_treeTableModel;
    private JScrollPane m_tableScrollPane;
    private JTabbedPane m_tabbedPane;

    public static AggregationQuantChannelsPanel getPanel(AbstractNode rootNode, List<DDataset> datasets) {
        if (m_singleton == null) {
            m_singleton = new AggregationQuantChannelsPanel();
        }
        m_singleton.setMapping(rootNode, datasets);

        return m_singleton;
    }

    private AggregationQuantChannelsPanel() {
        setLayout(new BorderLayout());
        add(new WizardPanel("<html><b>Step 2:</b> Define mapping between quantitation channels.</html>"), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel framePanel = new JPanel(new GridBagLayout());

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(createQCMappingPanel());
        sp.setRightComponent(createDatasetsPanel());
        sp.setDividerLocation(0.70);
        sp.setResizeWeight(0.5);

        final GridBagConstraints frameConstraints = new GridBagConstraints();
        frameConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        frameConstraints.gridx = 0;
        frameConstraints.gridy = 0;
        frameConstraints.gridwidth = 2;
        frameConstraints.weightx = 1;
        frameConstraints.weighty = 0;
        frameConstraints.anchor = GridBagConstraints.NORTH;
        frameConstraints.fill = GridBagConstraints.NONE;

        JPanel mappingToolBar = new QCMappingToolbar();
        framePanel.add(mappingToolBar, frameConstraints);
        //framePanel.add(new JLabel("Drag & Drop", IconManager.getIcon(IconManager.IconType.DRAG_AND_DROP), JLabel.LEADING), frameConstraints);

        frameConstraints.anchor = GridBagConstraints.NORTHWEST;
        frameConstraints.fill = GridBagConstraints.BOTH;
        frameConstraints.gridwidth = 1;
        frameConstraints.gridy++;
        frameConstraints.weighty = 1;
        framePanel.add(sp, frameConstraints);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }

    /**
     * left Aggregation Panel
     *
     * @return
     */
    private JComponent createQCMappingPanel() {
        m_tableScrollPane = new JScrollPane();
        m_tableScrollPane.getViewport().setBackground(Color.white);
        return m_tableScrollPane;
    }

    /**
     * right tabbed panel, multi quantitation
     *
     * @return
     */
    private JComponent createDatasetsPanel() {
        m_tabbedPane = new JTabbedPane();

        return m_tabbedPane;
    }

    /**
     * set data in left m_tableScrollPane, right m_tabbedPane, create for each
     * tab a QuantExperimentalDesignTree using DDataset(=Quantitation).
     *
     * @param rootNode, used to create Aggragation QCMappingTreeTableModel &
     * MappingTreeTable
     * @param datasets
     */
    public void setMapping(AbstractNode rootNode, List<DDataset> datasets) {
        m_treeTableModel = new QCMappingTreeTableModel(rootNode, datasets);
        m_treeTable = createTreeTable(m_treeTableModel);
        m_tableScrollPane.setViewportView(m_treeTable);
        m_tabbedPane.removeAll();

        for (DDataset ds : datasets) {
            DataSetData datasetData = DataSetData.createTemporaryQuantitation(ds.getName()); //new DataSetData(ds.getName(), Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
            datasetData.setDataset(ds);
            DataSetNode dsNode = new DataSetNode(datasetData);
            QuantExperimentalDesignTree dsDesignTree = new QuantExperimentalDesignTree(dsNode, true);
            QuantExperimentalDesignTree.displayExperimentalDesign(ds, dsNode, dsDesignTree, true, true);
            m_tabbedPane.add(ds.getName(), dsDesignTree);
        }
        validate();
        repaint();
    }

    /**
     * create the TreeTable shown in left m_tableScrollPane
     *
     * @param model
     * @return
     */
    private MappingTreeTable createTreeTable(QCMappingTreeTableModel model) {
        MappingTreeTable treeTable = new MappingTreeTable(model);
        // rendering of the tree
        treeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        treeTable.setRowHeight(18);

        IdentificationTree.RSMTreeRenderer renderer = new IdentificationTree.RSMTreeRenderer();
        treeTable.setTreeCellRenderer(renderer);
        treeTable.expandAll();
        TransferHandler handler = new QCMappingTransferHandler(treeTable);

        treeTable.setTransferHandler(handler);
        treeTable.setDropMode(DropMode.ON);
        treeTable.setRootVisible(true);
        treeTable.setShowGrid(true, true);

        return treeTable;

    }

    public List<Map<String, Object>> getQuantChannelsMatching() {
        List<Map<String, Object>> mappingList = new ArrayList<>();
        for (DQuantitationChannelMapping entry : m_treeTableModel.getMapping().values()) {
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("quant_channel_number", entry.getParentQCNumber());
            Map<Long, Long> map = entry.getMappedQuantChannels().entrySet().stream().filter(e -> (e.getValue() != null)).collect(Collectors.toMap(
                    e -> e.getKey().getMasterQuantitationChannels().get(0).getId(),
                    e -> e.getValue().getId()
            ));
            mapping.put("quant_channels_matching", map);
            mappingList.add(mapping);
        }

        return mappingList;
    }

    class QCMappingToolbar extends JPanel {

        JButton _removeBt;
        JButton _upBt;
        JButton _downBt;

        public QCMappingToolbar() {
            super();
            createRemoveButton();
            createUpButton();
            createDownButton();
            setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.insets = new java.awt.Insets(5, 5, 5, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;

            this.add(_removeBt, c);
            c.gridx++;
            this.add(_upBt, c);
            c.gridx++;
            this.add(_downBt, c);
            c.gridx++;
            this.add(new JLabel("Drag & Drop", IconManager.getIcon(IconManager.IconType.DRAG_AND_DROP), JLabel.LEADING), c);
        }

        void createRemoveButton() {
            _removeBt = new JButton("remove");
            _removeBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.removeAssociateChannel();
                }
            });
        }

        void createUpButton() {
            _upBt = new JButton();
            _upBt.setIcon(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_UP));
            _upBt.setToolTipText("up");
            _upBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.moveUp();
                }
            });

        }

        void createDownButton() {
            _downBt = new JButton();
            _downBt.setIcon(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_DOWN));
            _downBt.setToolTipText("down");
            _downBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    m_treeTable.moveDown();
                }
            });
        }

    }

    class MappingTreeTable extends JXTreeTable {
        //private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.AggregationQuant");

        private QCMappingTreeTableModel m_model;

        public MappingTreeTable(QCMappingTreeTableModel treeModel) {
            super(treeModel);
            m_model = treeModel;
            setCellSelectionEnabled(true);
            this.addMouseListener(new PopupAdapter());
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            int column = columnAtPoint(event.getPoint());
            if (column > 0 && column < getColumnCount()) {
                int row = rowAtPoint(event.getPoint());
                if (row < getRowCount()) {
                    return m_model.getToolTipText(getNodeForRow(row), column);
                }
            }
            return super.getToolTipText(event);
        }

        public Object getNodeForRow(int row) {
            TreePath path = getPathForRow(row);
            return path != null ? path.getLastPathComponent() : null;
        }

        protected void removeAssociateChannel() {
            int[] rowList = getSelectedRows();
            int[] columnList = getSelectedColumns();
            for (int row : rowList) {
                for (int column : columnList) {
                    m_model.remove(row, column);
                }
            }
            this.repaint();
        }

        protected void moveUpDown(int weight) {
            List<Integer> newSelectedRows = new ArrayList();
            int[] rows = getSelectedRows();
            List<Integer> rowList = Arrays.stream(rows).boxed().collect(Collectors.toList());
            if (weight == -1) {
                Collections.sort(rowList);//lower element move first

            } else {
                Collections.sort(rowList, Collections.reverseOrder());//higher element move first
            }
            if (m_model.isEndChannel(rowList, weight)) {
                return;
            }
            int[] columnList = getSelectedColumns();
            int row, targetRow;
            for (int i = 0; i < rowList.size(); i++) {
                //for (int row : rowList) {
                row = rowList.get(i);
                for (int column : columnList) {
                    targetRow = m_model.moveUpDown(row, column, weight);
                    if (targetRow != -1) {
                        newSelectedRows.add(targetRow);
                    }
                }
            }
            if (newSelectedRows.size() > 0) {
                Collections.sort(newSelectedRows);
                int firstRow = newSelectedRows.get(0);
                for (int i = 0; i < newSelectedRows.size(); i++) {
                    row = newSelectedRows.get(i);
                    if (i == 0) {
                        setRowSelectionInterval(row, row);
                    } else {
                        addRowSelectionInterval(row, row);;
                    }
                }
                this.repaint();
            }
        }

        private void moveUp() {
            moveUpDown(-1);
        }

        private void moveDown() {
            moveUpDown(1);
        }

        //select column, row
        protected void manageSelectionOnRightClick(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Point p = new Point(x, y);
            int Coloumn = columnAtPoint(p);
            int row = rowAtPoint(p);
            int[] selectedRows = this.getSelectedRows();
            int[] selectedCols = this.getSelectedColumns();
            if (selectedRows.length != 0) {
                List<Integer> selectedRowList = Arrays.stream(selectedRows).boxed().collect(Collectors.toList());
                if (selectedRowList.contains(row)) {
                    List<Integer> selectedColList = Arrays.stream(selectedCols).boxed().collect(Collectors.toList());
                    if (selectedColList.contains(Coloumn)) {
                        if (m_model.isChannelSelected(selectedRows, selectedCols)) {
                            triggerPopup(e);
                        }
                    }
                }
            }
        }

        //remove, up, down action
        private void triggerPopup(MouseEvent e) {
            JPopupMenu popup;
            popup = new JPopupMenu();
            ActionListener menuListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String command = event.getActionCommand();
                    if (command.equals("remove")){
                        removeAssociateChannel();
                    }else if (command.equals("up")){
                        moveUp();
                    }else if (command.equals("down")){
                        moveDown();
                    }
                }
            };
            JMenuItem item;
            popup.add(item = new JMenuItem("remove"));
            item.setHorizontalTextPosition(JMenuItem.RIGHT);
            item.addActionListener(menuListener);
            popup.addSeparator();
            popup.add(item = new JMenuItem("up", IconManager.getIcon(IconManager.IconType.ARROW_MOVE_UP)));
            item.setHorizontalTextPosition(JMenuItem.RIGHT);
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("down", IconManager.getIcon(IconManager.IconType.ARROW_MOVE_DOWN)));
            item.setHorizontalTextPosition(JMenuItem.RIGHT);
            item.addActionListener(menuListener);

            popup.show((JComponent) e.getSource(), e.getX(), e.getY());
        }

        class PopupAdapter extends MouseAdapter {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
//                if (SwingUtilities.isRightMouseButton(e)) {
//                    manageSelectionOnRightClick(e);
//                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    manageSelectionOnRightClick(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        }
    }

}
