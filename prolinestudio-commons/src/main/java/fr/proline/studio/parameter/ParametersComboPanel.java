package fr.proline.studio.parameter;

import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ParametersComboPanel extends JPanel {

  private AbstractParameter[] m_parameters;
  private JPanel m_selectedParametersPanel = null;
  private JComboBox m_comboBox = null;

  public ParametersComboPanel(String title, AbstractParameter[] parameters) {
    m_parameters = parameters;
    initComponent(title);
    updatePanel();
  }

  private void initComponent(String title) {
    this.setLayout(new GridBagLayout());
    this.setBorder(BorderFactory.createTitledBorder(title));

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    m_selectedParametersPanel = new JPanel(new GridBagLayout());
    c.gridx = 0;
    c.gridy = 0;
    this.add(m_selectedParametersPanel, c);

    m_comboBox = new JComboBox(m_parameters);
    m_comboBox.setRenderer(new ParameterComboboxRenderer(null));

    c.gridx = 0;
    c.gridy++;
    c.weightx = 1;
    this.add(m_comboBox, c);

    c.gridx++;
    c.weightx = 1.0;
    this.add(Box.createHorizontalBox(), c);

    m_comboBox.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent ie) {
        AbstractParameter p = (AbstractParameter) m_comboBox.getSelectedItem();
        if (p == null) {
          return;
        }
        p.setUsed(true);
        updatePanel();
      }
    });

    }


  public void updatePanel() {

    m_selectedParametersPanel.removeAll();
    m_comboBox.removeAllItems();

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new java.awt.Insets(5, 5, 5, 5);
    c.gridy = 0;

    int nbUsed = 0;
    boolean putAndInFront = false;
    int nbParameters = m_parameters.length;
    for (int i = 0; i < nbParameters; i++) {
      final AbstractParameter p = m_parameters[i];

      if ((p != null) && (p.isUsed())) {

        c.gridx = 0;
        if (putAndInFront) {
          m_selectedParametersPanel.add(new JLabel("AND"), c);
        } else {
          putAndInFront = true;
          m_selectedParametersPanel.add(new JLabel("   "), c);
        }

        c.gridx++;
        JLabel prefilterNameLabel = new JLabel(p.getName());
        prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
        m_selectedParametersPanel.add(prefilterNameLabel, c);

        if (p.hasComponent()) {
          c.gridx++;
          JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
          cmpLabel.setHorizontalAlignment(JLabel.CENTER);
          m_selectedParametersPanel.add(cmpLabel, c);

          c.weightx = 1;
          c.gridx++;
          m_selectedParametersPanel.add(p.getComponent(), c);
        } else {
          c.gridx++;
          c.gridx++;
        }

        c.weightx = 0;
        c.gridx++;
        JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeButton.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            p.setUsed(false);
            updatePanel();
          }
        });
        m_selectedParametersPanel.add(removeButton, c);

        nbUsed++;

        c.gridy++;
      } else {
        m_comboBox.addItem(p);
      }
    }

    boolean hasUnusedParameters = (nbUsed != nbParameters);
    m_comboBox.setVisible(hasUnusedParameters);

    revalidate();
  }
}
