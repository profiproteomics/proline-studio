/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ParameterComboboxRenderer;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author VD225637
 */
public class FilterProteinSetPanel extends JPanel {

    private JPanel m_proteinPrefiltersSelectedPanel = null;
    private JComboBox m_proteinPrefilterJComboBox = null;
    private JButton m_addProteinPrefilterButton = null;
    private AbstractParameter[] m_proteinFilterParameters;
    
    public FilterProteinSetPanel(String title,AbstractParameter[] proteinFilterParameters) {
        super(new GridBagLayout());
        m_proteinFilterParameters = proteinFilterParameters;
        if (title != null && !title.isEmpty()) {
            setBorder(BorderFactory.createTitledBorder(title));
        }
        createPanel();
        initProteinFilterPanel();
    }

    
    public AbstractParameter[] getProteinSetFilterParameters() {        
        return m_proteinFilterParameters;        
    }
    
    private void createPanel() {

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_proteinPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        add(m_proteinPrefiltersSelectedPanel, c);

        m_proteinPrefilterJComboBox = new JComboBox(m_proteinFilterParameters);
        m_proteinPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        m_addProteinPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addProteinPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        add(m_proteinPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        add(m_addProteinPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        add(Box.createHorizontalBox(), c);

        m_addProteinPrefilterButton.addActionListener((ActionEvent e) -> {
            AbstractParameter p = (AbstractParameter) m_proteinPrefilterJComboBox.getSelectedItem();
            if (p == null) {
                return;
            }
            p.setUsed(true);
            initProteinFilterPanel();
        });
    }
    
    protected void initProteinFilterPanel() {

        m_proteinPrefiltersSelectedPanel.removeAll();
        m_proteinPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        for (final AbstractParameter p : m_proteinFilterParameters) {
            if ((p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }

                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                m_proteinPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                c.gridx++;
                JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                m_proteinPrefiltersSelectedPanel.add(cmpLabel, c);

                c.weightx = 1;
                c.gridx++;
                m_proteinPrefiltersSelectedPanel.add(p.getComponent(), c);

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener((ActionEvent e) -> {
                    p.setUsed(false);
                    initProteinFilterPanel();                    
                });
                m_proteinPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                m_proteinPrefilterJComboBox.addItem(p);
            }
        }

        boolean hasUnusedParameters = (nbUsed != m_proteinFilterParameters.length);
        m_proteinPrefilterJComboBox.setVisible(hasUnusedParameters);
        m_addProteinPrefilterButton.setVisible(hasUnusedParameters);
        
        revalidate();
    }

}
