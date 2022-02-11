/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * Add to a button, the possibility to expand and select another button functionnality
 * For instance, replace zoom+ by zoom- or view all
 * 
 * @author JM235353
 */
public class ExtendableButtonPanel extends JPanel {
    
    private AbstractButton m_currentButton = null;
    private final ArrayList<AbstractButton> m_buttonList = new ArrayList<>();
    private final JPanel m_buttonPanel;
    
    private boolean m_isExpanded = false;
    
    private ExtendableButtonPanelGroup m_group = null;
    private final JButton m_expandButton;

    public ExtendableButtonPanel(AbstractButton currentButton) {
        
        setBorder(null);
        setOpaque(true);

//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new FlowLayout( FlowLayout.TRAILING, 0, 0));
        
        m_currentButton = currentButton;

        m_expandButton = new JButton(IconManager.getIcon(IconManager.IconType.EXPAND));
        m_expandButton.setAlignmentY(JComponent.TOP_ALIGNMENT);
        m_expandButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        m_expandButton.setFocusPainted(false);
        m_expandButton.setBorder(null);
        m_expandButton.setContentAreaFilled(false);
        m_expandButton.setOpaque(false);
        
        add(m_expandButton);
        
        m_expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_isExpanded) {
                    collapse();
                } else {
                    expand();
                }
            }
        });

        m_buttonPanel = new JPanel(new FlowLayout( FlowLayout.LEFT, 1, 1));
        m_buttonPanel.setBorder(BorderFactory.createLineBorder(Color.gray, 1, true));
        m_buttonPanel.setBackground(Color.white);
        
        add(m_buttonPanel);
        
        //add(Box.createRigidArea(new Dimension(1,16))); // minimal height : 22
        
        Dimension d = getPreferredSize();
        setSize((int) d.getWidth(), (int) d.getHeight());

    }
    
    protected void addedToGroup(ExtendableButtonPanelGroup g) {
        m_group = g;
    }
    
    public void paint(Graphics g) {
        
        if (getYForExpandedPanel() != m_positionY) {
            calculatePosition();
            repaint();
            return;
        }
        
        super.paint(g);
    }
    private int m_positionY = -1; // -1 : not set
    
    private void calculatePosition() {
        m_positionY = getYForExpandedPanel();
        setLocation(getXForExpandedPanel(), getYForExpandedPanel());
        Dimension d = getPreferredSize();
        setSize((int) d.getWidth(), (int) d.getHeight());
    }
    
    private int getXForExpandedPanel() {
        return m_currentButton.getX() + m_currentButton.getWidth() - m_expandButton.getWidth();
    }

    private int getYForExpandedPanel() {
        int buttonMiddleY = m_currentButton.getY() + m_currentButton.getHeight() / 2;
        int expandedPanelY = buttonMiddleY - getHeight() / 2;
        return expandedPanelY;
    }
    
    public void expand() {
        m_isExpanded = true;
        m_buttonPanel.setVisible(true);

        if (m_group != null) {
            m_group.beingExpanded(this);
        }
        
        calculatePosition();
        
        repaint();
    }
    
    public void collapse() {
        m_isExpanded = false;
        m_buttonPanel.setVisible(false);

        calculatePosition();
        
        repaint();
    }

    public void registerButton(final AbstractButton button) {
        
        if (m_buttonList.isEmpty()) {
            initCurrentButton(button);
        }
        
        m_buttonList.add(button);


        button.setMargin(new java.awt.Insets(2, 2, 2, 2));
        button.setFocusPainted(false);
        button.setBorder(null);
        button.setContentAreaFilled(false);
        

        
        m_buttonPanel.add(button);
        

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initCurrentButton(button);
            }
        });
    }
        
    private void initCurrentButton(AbstractButton button) {
        m_currentButton.setIcon(button.getIcon());
        m_currentButton.setText(button.getText());
        m_currentButton.setToolTipText(button.getToolTipText());
        for (ActionListener a : m_currentButton.getActionListeners()) {
            m_currentButton.removeActionListener(a);
        }
        for (ActionListener a : button.getActionListeners()) {
            m_currentButton.addActionListener(a);
        }
        
        if (m_currentButton instanceof JToggleButton) {
            if (!((JToggleButton) m_currentButton).isSelected()) {
                ((JToggleButton) m_currentButton).doClick();
            }
        }
        
        collapse();
    }

    
}
