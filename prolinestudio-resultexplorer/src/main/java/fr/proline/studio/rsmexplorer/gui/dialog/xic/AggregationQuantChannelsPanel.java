/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.QCMappingTreeTable;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author CB205360
 */
public class AggregationQuantChannelsPanel extends JPanel {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static AggregationQuantChannelsPanel m_singleton;
    private QCMappingTreeTable m_treeTable;
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
        add(createMainPanel(), BorderLayout.CENTER);
    }

    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(createQCMappingPanel());
        sp.setRightComponent(createDatasetsPanel());
        sp.setDividerLocation(0.70);
        sp.setResizeWeight(0.5);

        JToolBar mappingToolBar = new QCMappingToolbar();
        mappingToolBar.setFloatable(false);
        mappingToolBar.setRollover(true);

        JPanel toolbarPanel = new JPanel();

        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS));
        toolbarPanel.add(Box.createHorizontalGlue());
        toolbarPanel.add(mappingToolBar);
        toolbarPanel.add(Box.createHorizontalGlue());
        toolbarPanel.add(Box.createHorizontalGlue());

        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(sp, BorderLayout.CENTER);
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
        //left
        m_treeTableModel = new QCMappingTreeTableModel(rootNode, datasets);
        m_treeTable = createTreeTable(m_treeTableModel);
        m_tableScrollPane.setViewportView(m_treeTable);
        //right
        m_tabbedPane.removeAll();

        for (DDataset ds : datasets) {
            DataSetData datasetData = DataSetData.createTemporaryQuantitation(ds.getName()); //new DataSetData(ds.getName(), Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
            datasetData.setDataset(ds);
            DataSetNode dsNode = new DataSetNode(datasetData);
            QuantExperimentalDesignTree dsDesignTree = new QuantExperimentalDesignTree(dsNode, true);
            QuantExperimentalDesignTree.displayExperimentalDesign(ds, dsNode, dsDesignTree, false, true);
            JScrollPane sPane = new JScrollPane(dsDesignTree);
            m_tabbedPane.add(ds.getName(), sPane);
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
    private QCMappingTreeTable createTreeTable(QCMappingTreeTableModel model) {
        QCMappingTreeTable treeTable = new QCMappingTreeTable(model);
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
        treeTable.getColumnModel().getColumn(0).setPreferredWidth(160);//first column more large   
        if (treeTable.getColumnCount(false) > 3) {
            treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//in order to have the Horizontal scroll bar if need
        }
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

    class QCMappingToolbar extends JToolBar {

        JButton m_removeBt;
        JButton m_upBt;
        JButton m_downBt;
        JButton m_insertUpBt;
        JButton m_insertDownBt;

        public QCMappingToolbar() {
            super();
            createRemoveButton();
            createUpButton();
            createDownButton();
            createInsertUpButton();
            createInsertDownButton();

            this.add(m_removeBt);
            this.addSeparator();
            this.add(m_upBt);
            this.add(m_downBt);
            this.addSeparator();
            this.add(m_insertUpBt);
            this.add(m_insertDownBt);
        }

        private void createRemoveButton() {
            m_removeBt = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
            m_removeBt.setToolTipText(QCMappingTreeTable.ERRASE);
            m_removeBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.removeAssociateChannel();
                }
            });
        }

        private void createUpButton() {
            m_upBt = new JButton();
            m_upBt.setIcon(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_UP));
            m_upBt.setToolTipText(QCMappingTreeTable.MOVE_UP);
            m_upBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.moveUp();
                }
            });

        }

        private void createDownButton() {
            m_downBt = new JButton();
            m_downBt.setIcon(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_DOWN));
            m_downBt.setToolTipText(QCMappingTreeTable.MOVE_DOWN);
            m_downBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    m_treeTable.moveDown();
                }
            });
        }

        void createInsertUpButton() {
            m_insertUpBt = new JButton(IconManager.getIcon(IconManager.IconType.ARROW_INSERT_UP));
            m_insertUpBt.setToolTipText(QCMappingTreeTable.INSERT_UP);
            m_insertUpBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.moveInsertUp();
                }
            });
        }

        void createInsertDownButton() {
            m_insertDownBt = new JButton(IconManager.getIcon(IconManager.IconType.ARROW_INSERT_DOWN));
            m_insertDownBt.setToolTipText(QCMappingTreeTable.INSERT_DOWN);
            m_insertDownBt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_treeTable.moveInsertDown();
                }
            });
        }

    }

}
