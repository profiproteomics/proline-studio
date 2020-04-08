/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;

/**
 *
 * @author KX257079
 */
public class CheckDesignTreeDialog extends DefaultDialog {

    String groupName4checkDesignStructure;
    QuantExperimentalDesignPanel m_experimentalDesignPanel;
    QuantExperimentalDesignTree m_experimentalDesignTree;

    public CheckDesignTreeDialog(Window parent, ModalityType modalityType) {
        super(parent, modalityType);
    }

    /**
     * check also length of 1.XICNode,group, 2.sample, 3.sample Analysis,
     * 4.group+sample name, according Database restriction, length should < 100
     *
     * @param parentNode
     * @param duplicates
     * @return
     */
    public boolean checkDesignStructure(AbstractNode parentNode, Set<String> duplicates) {
        String nodeName = parentNode.toString();
        if (nodeName.length() > 100) {
            showErrorOnNode(parentNode, "Name Length should less than 100.");
            return false;
        }
        AbstractNode.NodeTypes type = parentNode.getType();
        switch (type) {
            case DATA_SET:
                groupName4checkDesignStructure = "";
                if (parentNode.isLeaf()
                        || (parentNode.getChildCount() == 1 && ((AbstractNode) parentNode.getChildAt(0)).getType() == AbstractNode.NodeTypes.DATASET_REFERENCE)) {
                    showErrorOnNode(parentNode, "Your Experimental Design is empty.");
                    return false;

                }
                break;
            case BIOLOGICAL_GROUP:
                groupName4checkDesignStructure = nodeName;
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Biological Sample for each Biological Group.");
                    return false;
                }
                break;
            case BIOLOGICAL_SAMPLE:
                if ((groupName4checkDesignStructure.length() + nodeName.length()) > 100) {
                    showErrorOnNode(parentNode, "Group name + sample name, the length should less than 100.");
                    return false;
                }
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Identification for each Biological Sample.");
                    return false;
                }
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS:
                if (parentNode.isLeaf()) {
                    if (duplicates.contains(parentNode.toString())) {
                        showErrorOnNode(parentNode, "Biological Sample Analysis is a duplicate. Please rename using right click!");
                        return false;
                    } else {
                        duplicates.add(parentNode.toString());
                        return true;
                    }
                }
                break;
        }

        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            if (!checkDesignStructure(rsmNode, duplicates)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Issue 13023: check the biological group names and biological sample
     * names: must be unique
     *
     * @param parentNode
     * @return
     */
    public boolean checkBiologicalGroupName(AbstractNode parentNode) {
        List<String> listBiologicalGroupName = new ArrayList();
        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            AbstractNode.NodeTypes type = rsmNode.getType();
            switch (type) {
                case BIOLOGICAL_GROUP: {
                    String gName = rsmNode.getData().getName();
                    if (listBiologicalGroupName.contains(gName)) {
                        showErrorOnNode(rsmNode, "The Biological Group name must be unique.");
                        return false;
                    }
                    listBiologicalGroupName.add(rsmNode.getData().getName());
                    List<String> listBiologicalSampleName = new ArrayList();
                    //Iterate over Samples
                    Enumeration childrenS = rsmNode.children();
                    while (childrenS.hasMoreElements()) {
                        AbstractNode sampleNode = (AbstractNode) childrenS.nextElement();
                        AbstractNode.NodeTypes typeS = sampleNode.getType();
                        switch (typeS) {
                            case BIOLOGICAL_SAMPLE: {//in one group, the sample name should unique
                                if (listBiologicalSampleName.contains(sampleNode.getData().getName())) {
                                    showErrorOnNode(sampleNode, "The Biological Sample name must be unique.");
                                    return false;
                                }
                                listBiologicalSampleName.add(sampleNode.getData().getName());
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    protected void showErrorOnNode(AbstractNode node, String error) {
        if (m_experimentalDesignPanel != null) {
            m_experimentalDesignTree = m_experimentalDesignPanel.getExperimentalDesignTree();
        }
        // expand parentnode if needed
        AbstractNode parentNode = (AbstractNode) node.getParent();
        if (parentNode != null) {
            TreePath pathToExpand = new TreePath(parentNode.getPath());
            if (!m_experimentalDesignTree.isExpanded(pathToExpand)) {
                m_experimentalDesignTree.expandPath(pathToExpand);
            }
        }
        // scroll to node if needed
        TreePath path = new TreePath(node.getPath());
        m_experimentalDesignTree.scrollPathToVisible(path);

        // display error
        setStatus(true, error);
        highlight(m_experimentalDesignTree, m_experimentalDesignTree.getPathBounds(path));
    }
}
