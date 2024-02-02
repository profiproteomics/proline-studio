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

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.xic.*;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to create the Quantitation Experimental Design by drag and drop
 *
 * @author JM235353
 */
public class QuantExperimentalDesignPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer");

    private AbstractNode m_rootNode;
    private final IdentificationTree m_selectionTree;
    private QuantExperimentalDesignTree m_experimentalDesignTree;
    private final AbstractNode m_initialRootNode;

    private final QuantitationMethod.Type m_quantitationType;
    private final JPanel m_selectionPanel;
    private JComboBox<QuantitationMethod> m_methodsCbx;

    private JCheckBox m_multiBatchCB;
    private boolean m_isMultiQuantPanel;
    private final boolean m_isMultiQuantAllowed;
    private final boolean m_isMultiBatchAllowed;
    private QuantitationMethod m_selectedMethod;
    private XICTransferHandler m_expDesignTransferTreeHandler;

    public QuantExperimentalDesignPanel(AbstractNode rootNode, IdentificationTree selectionTree, QuantitationMethod.Type quantitationType) {
        m_initialRootNode = rootNode;
        AbstractNode treeRoot;
        m_isMultiQuantAllowed =quantitationType.equals(QuantitationMethod.Type.ISOBARIC_TAGGING);
        if(m_isMultiQuantAllowed){
            treeRoot = new DataSetNode(DataSetData.createTemporaryFolder("All Quantifications"));
        } else
            treeRoot = rootNode;
        m_isMultiQuantPanel = m_isMultiQuantAllowed;
        m_isMultiBatchAllowed = m_isMultiQuantAllowed;

        m_rootNode = treeRoot;
        m_selectionTree = selectionTree;
        m_experimentalDesignTree = m_isMultiQuantPanel ? new QuantExperimentalDesignTree(m_rootNode, true, false) : new QuantExperimentalDesignTree(m_rootNode, true, true);
        m_expDesignTransferTreeHandler = (XICTransferHandler) m_experimentalDesignTree.getTransferHandler();

        m_quantitationType = quantitationType;

        m_selectionPanel = createSelectionTreePanel();
        /*
        Create method JCombo box but display only for some types.
        Only one choice for label free, no panel needed.
         */
        initializeMethodComponents();
        setLayout(new BorderLayout());
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private void reinitPanel(){
        AbstractNode treeRoot;
        //Reinit Tree
        if(m_isMultiQuantPanel){
            treeRoot = new DataSetNode(DataSetData.createTemporaryFolder("All Quantifications"));
        } else
            treeRoot = m_initialRootNode;
        m_rootNode = treeRoot;
        m_experimentalDesignTree = m_isMultiQuantPanel ? new QuantExperimentalDesignTree(m_rootNode, true, false) : new QuantExperimentalDesignTree(m_rootNode, true, true);
        m_expDesignTransferTreeHandler = (XICTransferHandler) m_experimentalDesignTree.getTransferHandler();
        fireExpDesignTreeChanges();
        //Reload Panel components
        removeAll();
        setLayout(new BorderLayout());
        add(createMainPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public boolean isMultiQuantPanel(){
        return m_isMultiQuantPanel;
    }

    public boolean isMultiBatchSelected(){
        return m_multiBatchCB.isSelected();
    }

    protected final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel designTreePanel = createDesignTreePanel();
        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(designTreePanel);
        sp.setRightComponent(m_selectionPanel);
        sp.setResizeWeight(0.5);

        JPanel framePanel = new JPanel(new GridBagLayout());
        framePanel.setBorder(BorderFactory.createTitledBorder(" Experimental Design "));
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

        /*
        Only one choice for label free, no panel needed.
         */
        switch (m_quantitationType) {
            case ISOBARIC_TAGGING:
            case RESIDUE_LABELING: {
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weighty = 0;
                mainPanel.add(createMethodPanel(), c);
                c.gridx = 0;
                c.gridy++;
            }
        }
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }

    private void initializeMethodComponents() {
        QuantitationMethod[] quantMethods = retrieveQuantMethods(m_quantitationType);
        QuantitationMethod[] allMethods;
        if(m_quantitationType.equals(QuantitationMethod.Type.LABEL_FREE)) {
            allMethods = quantMethods;
        } else {
            allMethods = new QuantitationMethod[quantMethods.length+1];
            allMethods[0] = null;
            for(int i = 1; i<= quantMethods.length; i++){
                allMethods[i] = quantMethods[i-1];
            }
        }

        m_methodsCbx = new JComboBox<>(allMethods);
        m_selectedMethod = ((QuantitationMethod) m_methodsCbx.getSelectedItem());
        m_methodsCbx.addItemListener((e) -> {
            m_selectedMethod = ((QuantitationMethod) m_methodsCbx.getSelectedItem());
        });

        if(m_isMultiBatchAllowed) {
            m_multiBatchCB = new JCheckBox("Multi Batch Quantitation");
            m_multiBatchCB.addActionListener(e -> {
                m_isMultiQuantPanel = m_isMultiQuantAllowed && !m_multiBatchCB.isSelected();
                reinitPanel();
            });
        }
    }


    private JPanel createMethodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" Quantitation Method "));

        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;

        JLabel label = new JLabel("Name:");
        panel.add(label, c);

        c.gridx++;
        panel.add(m_methodsCbx, c);

        if(m_isMultiBatchAllowed) {
            c.gridx++;
            panel.add(m_multiBatchCB, c);
        }

        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JPanel(), c);

        return panel;
    }

    private QuantitationMethod[] retrieveQuantMethods(QuantitationMethod.Type quantitationType) {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            TypedQuery<QuantitationMethod> query = entityManagerUDS.createNamedQuery("findQuantMethodForType", QuantitationMethod.class);
            query.setParameter("searchType", quantitationType.toString());
            List<QuantitationMethod> results = query.getResultList();
            results.forEach(method -> Hibernate.initialize(method.getLabels()));
            return results.toArray(new QuantitationMethod[0]);
        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", e);
        } finally {
            entityManagerUDS.close();
        }

        return new QuantitationMethod[0];
    }

    private JPanel createDesignTreePanel() {
        JPanel designTreePanel = new JPanel();

        if(!m_isMultiQuantPanel) {
            designTreePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;

            JScrollPane treeScrollPane = new JScrollPane();
            treeScrollPane.setViewportView(m_experimentalDesignTree);

            designTreePanel.add(treeScrollPane, c);
        } else {

            JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            sp.setResizeWeight(0.5);

            JPanel dndPanel = new JPanel();
            dndPanel.setLayout(new GridBagLayout());
            GridBagConstraints c2 = new GridBagConstraints();
            c2.anchor = GridBagConstraints.NORTHWEST;
            c2.fill = GridBagConstraints.NONE;
            c2.insets = new java.awt.Insets(5, 5, 5, 5);
            c2.gridx = 0;
            c2.gridy = 0;
            c2.weightx = 0;
            c2.weighty = 0;
            JLabel quantTitle = new JLabel("Drag&Drop datasets to quantify here : ");
            dndPanel.add(quantTitle, c2);

            c2.gridx++;
            c2.anchor = GridBagConstraints.EAST;
            JButton clearButton = new JButton();
            clearButton.setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
            clearButton.setToolTipText("Clear All");
            clearButton.addActionListener(e ->  {
                m_isMultiQuantPanel = m_isMultiQuantAllowed;
                reinitPanel();
            });
            dndPanel.add(clearButton, c2);

            c2.gridx=0;
            c2.gridy++;
            c2.weightx = 1;
            c2.weighty = 1;
            c2.gridwidth=2;
            c2.fill = GridBagConstraints.BOTH;
            JList<String> datasetToQuantList = new JList<>(new DefaultListModel<>());
            datasetToQuantList.setTransferHandler( new ListTransferHandler(m_experimentalDesignTree, m_initialRootNode, m_expDesignTransferTreeHandler));
            JScrollPane listPane = new JScrollPane(datasetToQuantList);
            dndPanel.add(listPane, c2);

            JScrollPane treeScrollPane = new JScrollPane();
            treeScrollPane.setViewportView(m_experimentalDesignTree);

            sp.setTopComponent(dndPanel);
            sp.setBottomComponent(treeScrollPane);

            designTreePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            designTreePanel.add(sp, c);
        }

        return designTreePanel;
    }

    private JPanel createSelectionTreePanel() {
        JPanel selectionTreePanel = new JPanel();
        boolean isCorrect = true;
        Enumeration<TreeNode> childEnum = m_rootNode.children();
        while (childEnum.hasMoreElements()) {
            AbstractNode childNode = (AbstractNode) childEnum.nextElement();
            if (childNode instanceof DatasetReferenceNode) {
                isCorrect = !((DatasetReferenceNode) childNode).isInvalidReference();
            }
        }

        selectionTreePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        if (isCorrect) {
            AbstractNode rootSelectionNode;
            if (m_selectionTree == null) {
                rootSelectionNode = IdentificationTree.getCurrentTree().copyRootNodeForSelection();
            } else {
                rootSelectionNode = m_selectionTree.copyRootNodeForSelection();
            }

            IdentificationSelectionTree tree = new IdentificationSelectionTree(rootSelectionNode, true);
            JScrollPane treeScrollPane = new JScrollPane();
            treeScrollPane.setViewportView(tree);

            c.gridy++;
            selectionTreePanel.add(treeScrollPane, c);
        } else {
            selectionTreePanel = new JPanel(new BorderLayout());
            JLabel errMsgLabel = new JLabel("<html>Invalid Reference Dataset Specified in XIC (may have been revalidated).<br> Same data will be used, no change in experimental design is allowed.</html>", IconManager.getIcon(IconManager.IconType.EXCLAMATION), JLabel.CENTER);
            errMsgLabel.setForeground(Color.red);
            selectionTreePanel.add(errMsgLabel, BorderLayout.CENTER);
        }

        return selectionTreePanel;
    }

    public QuantExperimentalDesignTree getExperimentalDesignTree() {
        return m_experimentalDesignTree;
    }

    public List<Long> getQuantifiedRsmIds() {
        ArrayList<Long> rsmids = new ArrayList<>();
        if(m_isMultiQuantPanel){
            Enumeration<TreeNode> enumeration = m_rootNode.children();
            while (enumeration.hasMoreElements()) {
                rsmids.addAll(m_experimentalDesignTree.getQuantifiedRsmIds((AbstractNode) enumeration.nextElement()));
            }
        } else
            rsmids.addAll(m_experimentalDesignTree.getQuantifiedRsmIds(m_initialRootNode));
        return rsmids;
    }

    public Component getQuantMethodComponent(){
        return m_methodsCbx;
    }

    public QuantitationMethod getQuantitationMethod() {
        return m_selectedMethod;
    }

    //
    //  Managing Listeners
    //
    private List<QuantExpDesignTreeListener> listenerList;   // List of listeners

    /**
     * Adds a QuantExpDesignTreeListener that will be notified when experimentalDesignTree has changed
     *
     * @param   l   the QuantExpDesignTreeListener
     */
    public void addQuantExpDesignTreeListener(QuantExpDesignTreeListener l) {
        if(listenerList == null)
            listenerList = new ArrayList<>();
        listenerList.add(l);
    }

    /**
     * Removes a QuantExpDesignTreeListener from the list that's notified when
     * experimentalDesignTree has changed
     *
     * @param   l   the QuantExpDesignTreeListener
     */
    public void removeQuantExpDesignTreeListener(QuantExpDesignTreeListener l) {
        listenerList.remove( l);
    }

    private void fireExpDesignTreeChanges() {
        for (QuantExpDesignTreeListener l :  listenerList) {
            l.experiementalTreeChange(m_experimentalDesignTree);
        }
    }


    static class ListTransferHandler extends TransferHandler {

        private final XICTransferHandler m_xicHandler;
        private final QuantExperimentalDesignTree m_experimentalDesignTrees;
        private final AbstractNode m_templateNode;

        protected ListTransferHandler(QuantExperimentalDesignTree experimentalDesignTrees, AbstractNode template, XICTransferHandler xicHandler) {
            super();
            m_experimentalDesignTrees = experimentalDesignTrees;
            m_templateNode = template;
            m_xicHandler = xicHandler;
        }


        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport support) {
            m_logger.info(" Import data to List "+support.toString());
            if (canImport(support)) {
                try {
                    XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                    XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());

                    JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                    DefaultListModel<String> listModel = (DefaultListModel<String>) ((JList) support.getComponent()).getModel();

                    AbstractNode root = (AbstractNode) m_experimentalDesignTrees.getModel().getRoot();
                    int nbC = root.getChildCount()+1;
                    XICSelectionTransferable.TransferData singleData = new  XICSelectionTransferable.TransferData(data.getSourceId());

                    int index = Math.max(0, dropLocation.getIndex());
                    for (DataSetNode  item : data.getDatasetList()) {
                        listModel.add(index++, item.getData().getName());

                        DataSetNode newChild = new DataSetNode(DataSetData.createTemporaryQuantitation("Quant-"+nbC));
                        m_templateNode.copyChildren(newChild);
                        ((DefaultTreeModel)m_experimentalDesignTrees.getModel()).insertNodeInto(newChild, root, nbC-1);
                        nbC++;
                        ArrayList<DataSetNode> singleDatanode = new ArrayList<>();
                        singleDatanode.add(item);
                        singleData.setDatasetList(singleDatanode);
                        m_xicHandler.importDirectNodeToQuant(newChild, singleData);
                    }

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

}
