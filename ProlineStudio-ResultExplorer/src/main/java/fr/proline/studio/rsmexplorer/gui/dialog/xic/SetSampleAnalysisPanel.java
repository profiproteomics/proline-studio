/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class SetSampleAnalysisPanel extends JPanel {

    public SetSampleAnalysisPanel(RSMNode rootNode) {
        setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        JPanel designTreePanel = createDesignTreePanel(rootNode);
        add(designTreePanel, c);

        c.gridx++;
        JPanel selectionTreePanel = createSelectionTreePanel();
        add(selectionTreePanel, c);
    }

    private JPanel createDesignTreePanel(RSMNode rootNode) {
        JPanel designTreePanel = new JPanel();

        designTreePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        DesignTree tree = DesignTree.getDesignTree(rootNode);
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(tree);


        designTreePanel.add(treeScrollPane, c);

        return designTreePanel;
    }

    private JPanel createSelectionTreePanel() {
        JPanel selectionTreePanel = new JPanel();

        selectionTreePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;


        SelectionTree tree = new SelectionTree(IdentificationTree.getCurrentTree().copyRootNodeForSelection());
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(tree);

        c.gridy++;
        selectionTreePanel.add(treeScrollPane, c);

        return selectionTreePanel;
    }

    
    
}