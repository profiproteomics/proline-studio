/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author CB205360
 */
public class WizardPanel extends JPanel {

  public WizardPanel(String title) {
      this(title, null);
  }
  
  public WizardPanel(String title, String helpText) {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 15, 5, 15);

        JLabel wizardLabel = new JLabel(title);
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(wizardLabel, c);
 
        if (helpText != null && ! helpText.isEmpty()) {
            JTextArea helpArea = new JTextArea(helpText);
            helpArea.setForeground(Color.LIGHT_GRAY);
            helpArea.setBackground(Color.WHITE);
            helpArea.setLineWrap(true);
            helpArea.setWrapStyleWord(true);
            c.gridy++;
            add(helpArea, c);
        }
    }
  
}
