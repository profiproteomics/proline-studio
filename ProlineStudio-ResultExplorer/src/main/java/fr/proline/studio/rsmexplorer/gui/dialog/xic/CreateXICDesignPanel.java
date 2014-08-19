package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.SelectionTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;



/**
 * Panel to create the XIC XICDesignTree by drag and drop
 * @author JM235353
 */
public class CreateXICDesignPanel extends JPanel {
    
    private static CreateXICDesignPanel m_singleton = null;
    
    private AbstractNode m_rootNode;
    
    public static CreateXICDesignPanel getPanel(AbstractNode rootNode) {
        if((m_singleton == null) || (!m_singleton.m_rootNode.equals(rootNode))){
            m_singleton = new CreateXICDesignPanel(rootNode);
        }
        return m_singleton;
    }
    
    public static CreateXICDesignPanel getPanel() {
        if (m_singleton != null) {
            return m_singleton;
        }
        throw new IllegalAccessError(" Panel not initialized yet ! ");
    }
    
    private CreateXICDesignPanel(AbstractNode rootNode) {
        m_rootNode = rootNode;
        
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
    
    public final JPanel createMainPanel(AbstractNode rootNode) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JPanel designTreePanel = createDesignTreePanel(rootNode);
        JPanel selectionTreePanel = createSelectionTreePanel();
        
        JPanel framePanel = new JPanel(new GridBagLayout());
        framePanel.setBorder(BorderFactory.createTitledBorder(" XIC Design "));
        
        
        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(designTreePanel);
        sp.setRightComponent(selectionTreePanel);
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

        JLabel wizardLabel = new JLabel("<html><b>Step 1:</b> Drag and Drop Identification Summaries to create your XIC Design.</html>");
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);

        return wizardPanel;
    }
    
    
    private JPanel createDesignTreePanel(AbstractNode rootNode) {
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

        XICDesignTree tree = XICDesignTree.getDesignTree(rootNode);
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


        SelectionTree tree = new SelectionTree(IdentificationTree.getCurrentTree().copyRootNodeForSelection(), true);
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(tree);

        c.gridy++;
        selectionTreePanel.add(treeScrollPane, c);

        return selectionTreePanel;
    }

    
    
}
