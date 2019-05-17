package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
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
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.*;
import org.hibernate.Hibernate;
import org.slf4j.LoggerFactory;

/**
 * Panel to create the Quantitation Experimental Design by drag and drop
 *
 * @author JM235353
 */
public class QuantExperimentalDesignPanel extends JPanel {

    private final AbstractNode m_rootNode;
    private final IdentificationTree m_selectionTree;
    private final QuantExperimentalDesignTree m_experimentalDesignTree;
    private final QuantitationMethod.Type m_quantitationType;
    private JComboBox<QuantitationMethod> m_methodsCbx;
    private QuantitationMethod m_selectedMethod;

    public QuantExperimentalDesignPanel(AbstractNode rootNode, IdentificationTree selectionTree, QuantitationMethod.Type quantitationType) {
        m_rootNode = rootNode;
        m_selectionTree = selectionTree;
        m_experimentalDesignTree = new QuantExperimentalDesignTree(m_rootNode, true);
        m_quantitationType = quantitationType;

        JPanel mainPanel = createMainPanel();
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel designTreePanel = createDesignTreePanel();
        JPanel selectionTreePanel = createSelectionTreePanel();

        JPanel framePanel = new JPanel(new GridBagLayout());
        framePanel.setBorder(BorderFactory.createTitledBorder(" Experimental Design "));

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(designTreePanel);
        sp.setRightComponent(selectionTreePanel);
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
        c.anchor = GridBagConstraints.NORTHWEST;

        /*
        Create method JCombo box but display only for some types. 
        Only one choice for label free, no panel needed.
         */
        m_methodsCbx = new JComboBox<>(retrieveQuantMethods(m_quantitationType));
        m_selectedMethod = ((QuantitationMethod) m_methodsCbx.getSelectedItem());
        m_methodsCbx.addItemListener((e) -> {
            m_selectedMethod = ((QuantitationMethod) m_methodsCbx.getSelectedItem());
        });

        switch (m_quantitationType) {
            case ISOBARIC_TAGGING:
            case RESIDUE_LABELING: {
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weighty = 0;
                mainPanel.add(createMethodPanel(m_quantitationType), c);
                c.gridx = 0;
                c.gridy++;
            }
        }
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }

    private JPanel createMethodPanel(QuantitationMethod.Type quantitationType) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" Quantitation Method "));

        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;

        JLabel label = new JLabel("Name:");
        panel.add(label, c);

        c.gridx++;
        c.weightx = 0.2;
        panel.add(m_methodsCbx, c);

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

        return null;
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
        treeScrollPane.setViewportView(m_experimentalDesignTree);

        designTreePanel.add(treeScrollPane, c);

        return designTreePanel;
    }

    private JPanel createSelectionTreePanel() {
        JPanel selectionTreePanel = new JPanel();
        boolean isCorrect = true;
        Enumeration childEnum = m_rootNode.children();
        while (childEnum.hasMoreElements()) {
            AbstractNode childNode = (AbstractNode) childEnum.nextElement();
            if (DatasetReferenceNode.class.isInstance(childNode)) {
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
        return m_experimentalDesignTree.getQuantifiedRsmIds(m_rootNode);
    }

    public QuantitationMethod getQuantitationMethod() {
        return m_selectedMethod;
    }

}
