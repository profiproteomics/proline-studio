/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A border including a Checkbox in the title
 *
 * @author VD225637
 */
public class CheckBoxTitledBorder extends AbstractBorder implements MouseListener, ChangeListener {

  private final TitledBorder m_border;
  private final JCheckBox m_checkBox;
  private Container m_container;
  private Rectangle m_rectangle;

  public CheckBoxTitledBorder(String title, boolean selected) {
    m_border = BorderFactory.createTitledBorder(title);
    m_checkBox = new JCheckBox(title, selected);
    m_checkBox.setHorizontalTextPosition(SwingConstants.RIGHT);
    m_checkBox.addChangeListener(this);
  }

  public JCheckBox getInternalCheckBox() {
    return m_checkBox;
  }
  
  public boolean isSelected() {
    return m_checkBox.isSelected();
  }
  
  public void setSelected(boolean isSelected) {
    m_checkBox.setSelected(isSelected);
  }
  
  public void setEnabled(boolean isEnabled) {
    m_checkBox.setEnabled(isEnabled);
  }
  
  public void addActionListener(ActionListener listener) {
    m_checkBox.addActionListener(listener);
  }

  public void addChangeListener(ChangeListener listener) {
    m_checkBox.addChangeListener(listener);
  }

  @Override
  public boolean isBorderOpaque() {
    return true;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Insets borderInsets = m_border.getBorderInsets(c);
    Insets insets = getBorderInsets(c);
    int temp = (insets.top - borderInsets.top) / 2;
    m_border.paintBorder(c, g, x, y + temp, width, height - temp);
    Dimension size = m_checkBox.getPreferredSize();
    if (c != m_container) {
      m_container = (Container)c;
      m_container.addMouseListener(this);
    }
    m_rectangle = new Rectangle(5, 0, size.width, size.height);
    SwingUtilities.paintComponent(g, m_checkBox, m_container, m_rectangle);
  }

  @Override
  public Insets getBorderInsets(Component c) {
    Insets insets = m_border.getBorderInsets(c);
    insets.top = Math.max(insets.top, m_checkBox.getPreferredSize().height);
    return insets;
  }

  private void dispatchEvent(MouseEvent me) {
    if (m_rectangle != null && m_rectangle.contains(me.getX(), me.getY())) {
      Point pt = me.getPoint();
      pt.translate(-5, 0);
      m_checkBox.setBounds(m_rectangle);
      m_checkBox.dispatchEvent(new MouseEvent(m_checkBox, me.getID(),
              me.getWhen(), me.getModifiers(), pt.x, pt.y, me.getClickCount(), me.isPopupTrigger(), me.getButton()));
      if (!m_checkBox.isValid() && (m_container != null))  {
        m_container.repaint();
      }
    }
  }

  public void mousePressed(MouseEvent me) {
    dispatchEvent(me);
  }

  public void mouseReleased(MouseEvent me) {
    dispatchEvent(me);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    if (m_container != null) {
      m_container.repaint();
    }
  }
}
