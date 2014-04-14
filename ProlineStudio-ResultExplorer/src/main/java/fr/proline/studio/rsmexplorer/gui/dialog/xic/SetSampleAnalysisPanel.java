/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import javax.swing.*;
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
        
        JPanel wizardPanel = createWizardPanel();
        JPanel mainPanel = createMainPanel(rootNode);
        
        
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
    
    public final JPanel createMainPanel(RSMNode rootNode) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 4;
        JPanel designTreePanel = createDesignTreePanel(rootNode);
        mainPanel.add(designTreePanel, c);

        c.gridx++;
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 1;
        mainPanel.add(Box.createGlue(), c);
        
        c.gridy++;
        c.weighty = 0;
        mainPanel.add(new JLabel(IconManager.getIcon(IconManager.IconType.ARROW_CURVED)), c);
        
        c.gridy++;
        mainPanel.add(new JLabel("Drag & Drop"), c);
        
        c.gridy++;
        c.weighty = 1;
        mainPanel.add(Box.createGlue(), c);
        
        c.gridy = 0;
        c.gridx++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 4;
        JPanel selectionTreePanel = createSelectionTreePanel();
        mainPanel.add(selectionTreePanel, c);
        
        return mainPanel;
    }

    private JPanel createWizardPanel() {
        JPanel wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel wizardLabel = new JLabel("<html><b>Step 3:</b> Drag and Drop Identification Summaries as Sample Analysis in Biological Samples.</html>");
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);

        return wizardPanel;
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