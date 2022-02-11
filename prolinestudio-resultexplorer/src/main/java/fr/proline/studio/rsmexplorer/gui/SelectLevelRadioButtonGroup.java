package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;

public class SelectLevelRadioButtonGroup {

    private final JRadioButton m_radioButton;
    private final JLabel m_validationLabel;
    private final JLabel m_automaticLabel;

  /**
   *
   * @param containerPanel : Panel where radio button and associated component should be added
   * @param c : GridBagConstraints to be used to add radio button. This will be modified while component are added
   * @param text : Label to be used for radio button
   * @param icon : icon to be used for radio button with radio button
   */
    public SelectLevelRadioButtonGroup(JPanel containerPanel, GridBagConstraints c, String text, ImageIcon icon) {

      m_radioButton = new JRadioButton(text);

      m_validationLabel = new JLabel(icon){
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(16,16);
        }
      };
      m_validationLabel.setHorizontalTextPosition(JLabel.LEFT);
      m_validationLabel.setVerticalTextPosition(JLabel.BOTTOM);
      m_validationLabel.setToolTipText(text);

      m_automaticLabel = new JLabel("") {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(16,16);
        }
      };

      containerPanel.add(m_radioButton, c);


      c.gridx++;
      containerPanel.add(m_validationLabel, c);

      c.gridx++;
      containerPanel.add(m_automaticLabel, c);

      c.gridx++;
      c.weightx = 1;
      containerPanel.add(Box.createHorizontalGlue(), c);
      c.weightx = 0;

    }


    public JRadioButton getRadioButton() {
      return m_radioButton;
    }

  /**
   *  Add an icon indicating if selection level was obtained automatically or manually)
   *
    * @param selectLevel ; Selection Level to represent.
   */
  public void addIcon(int selectLevel) {
      boolean isManual = false;
      switch (selectLevel) {
        case 0:
        case 3:
          isManual = true;
          break;
        case 1:
        case 2:
          isManual = false;
      }

      ImageIcon icon = null;
      String tooltips;
      if (isManual) {
        icon = new ImageIcon(IconManager.getIcon(IconManager.IconType.HAND_OPEN).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        tooltips = "Manual";
      } else {
        icon = new ImageIcon(IconManager.getIcon(IconManager.IconType.GEAR).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        tooltips = "Automatic";
      }
      m_automaticLabel.setIcon(icon);
      m_automaticLabel.setToolTipText(tooltips);
    }

  /**
   * Remove automatic/manual indication
   */
  public void removeOptionIcon() {
      m_automaticLabel.setIcon(null);
    }

  }
