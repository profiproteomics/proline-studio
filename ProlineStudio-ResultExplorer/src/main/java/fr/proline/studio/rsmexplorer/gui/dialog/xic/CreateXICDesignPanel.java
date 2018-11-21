package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.IdentificationSelectionTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.DatasetReferenceNode;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;
import javax.swing.*;

/**
 * Panel to create the XIC XICDesignTree by drag and drop
 *
 * @author JM235353
 */
public class CreateXICDesignPanel extends JPanel {

    private static CreateXICDesignPanel m_singleton = null;

    private final AbstractNode m_rootNode;
    private final IdentificationTree m_selectionTree;
    
    private XICDesignTree m_designTree;
    
    public static CreateXICDesignPanel getPanel(AbstractNode rootNode, IdentificationTree selectionTree) {
        if ((m_singleton == null)
                || (!m_singleton.m_rootNode.equals(rootNode))
                || ((m_singleton.m_selectionTree != null) && (selectionTree == null))
                || ((m_singleton.m_selectionTree == null) && (selectionTree != null))
                || ((m_singleton.m_selectionTree != null) && (!m_singleton.m_selectionTree.equals(selectionTree)))) {
            m_singleton = new CreateXICDesignPanel(rootNode, selectionTree);
        }

        return m_singleton;
    }

    public static CreateXICDesignPanel getPanel() {
        if (m_singleton != null) {
            return m_singleton;
        }
        throw new IllegalAccessError(" Panel not initialized yet ! ");
    }



    private CreateXICDesignPanel(AbstractNode rootNode, IdentificationTree selectionTree) {
        m_rootNode = rootNode;
        m_selectionTree = selectionTree;
        m_designTree = new XICDesignTree(m_rootNode, true);
        
        JPanel wizardPanel = new WizardPanel("<html><b>Step 1:</b> Drag and Drop Identification Summaries to create your XIC Design.</html>");
        JPanel mainPanel = createMainPanel();

        setLayout(new BorderLayout());
        add(wizardPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

    }

    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JPanel designTreePanel = createDesignTreePanel();
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

    private JPanel createDesignTreePanel() {
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
        
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(m_designTree);

        designTreePanel.add(treeScrollPane, c);

        return designTreePanel;
    }

    private JPanel createSelectionTreePanel() {
        JPanel selectionTreePanel = new JPanel();
        boolean isCorrect = true;
        Enumeration childEnum = m_rootNode.children();
        while(childEnum.hasMoreElements()){
            AbstractNode childNode = (AbstractNode) childEnum.nextElement();
            if(DatasetReferenceNode.class.isInstance(childNode)){
                isCorrect = !((DatasetReferenceNode)childNode).isInvalidReference();
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
        if(isCorrect){
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
    
    public XICDesignTree getDesignTree(){
        return m_designTree;
    }

    public void updatePanel(){
        //Component c = this.getComponent(1);
        this.remove(1); //Remove component at 1 => Main Panel        
        add(createMainPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

}
