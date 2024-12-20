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
package fr.proline.studio.gui;

import fr.proline.studio.WindowManager;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * Panel with a generic behaviour to work as a floating panel. -> possibility
 * to drag it and to close it.
 *
 * @author JM235353
 */
public class DefaultFloatingPanel extends HourglassPanel {

    protected JButton[] m_actionButtonArray = null;

    public DefaultFloatingPanel() {

    }

    public DefaultFloatingPanel(String infoText, String[] actionName, ActionListener[] a, Icon[] icon) {
        this(infoText, actionName, a, icon, true);
    }
    public DefaultFloatingPanel(String infoText, String[] actionName, ActionListener[] a, Icon[] icon, boolean closable) {
        setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
        setOpaque(true);
        setLayout(new FlowLayout());

        if (closable) {
            JButton closeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
            closeButton.setMargin(new Insets(0, 0, 0, 0));
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);

            closeButton.addActionListener(e -> setVisible(false));

            add(closeButton);
        }

        JLabel infoLabel = null;
        if (infoText != null) {
            infoLabel = new JLabel(infoText);
        }


        if (infoLabel != null) {
            add(infoLabel);
        }

        int preferredButtonWidth = 0;
        int preferredButtonHeight = 0;
        int nbActions = actionName.length;
        m_actionButtonArray = new JButton[nbActions];
        for (int i = 0; i < nbActions; i++) {
            m_actionButtonArray[i] = new JButton(actionName[i], icon[i]);
            m_actionButtonArray[i].setMargin(new Insets(1, 1, 1, 1));
            m_actionButtonArray[i].setFocusPainted(false);

            m_actionButtonArray[i].addActionListener(a[i]);

            add(m_actionButtonArray[i]);

            preferredButtonWidth = Math.max(preferredButtonWidth, m_actionButtonArray[i].getPreferredSize().width);
            preferredButtonHeight = Math.max(preferredButtonHeight, m_actionButtonArray[i].getPreferredSize().height);
        }

        // equalize dimension of buttons
        Dimension buttonDimension = new Dimension(preferredButtonWidth, preferredButtonHeight);
        for (int i = 0; i < nbActions; i++) {
            m_actionButtonArray[i].setPreferredSize(buttonDimension);
        }

        //

        Dimension d = getPreferredSize();
        setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());

        MouseAdapter dragGestureAdapter = new DragGestureAdapter();

        addMouseMotionListener(dragGestureAdapter);
        addMouseListener(dragGestureAdapter);

        setVisible(false);

    }

    public void enableButton(int index, boolean value) {
        m_actionButtonArray[index].setEnabled(value);
    }

    public void enableAllButtons(boolean value) {
        for (int i=0;i<m_actionButtonArray.length;i++) {
            m_actionButtonArray[i].setEnabled(value);
        }
    }

    public static class DragGestureAdapter extends MouseAdapter {

        public DragGestureAdapter() {
        }

        int dX, dY;

        @Override
        public void mouseDragged(MouseEvent e) {
            Component panel = e.getComponent();

            int newX = e.getLocationOnScreen().x - dX;
            int newY = e.getLocationOnScreen().y - dY;

            Component parentComponent = panel.getParent();
            int parentX = parentComponent.getX();
            if (newX < parentX) {
                newX = parentX;
            }
            int parentY = parentComponent.getY();
            if (newY < parentY) {
                newY = parentY;
            }
            int parentWidth = parentComponent.getWidth();
            if (newX + panel.getWidth() > parentWidth - parentX) {
                newX = parentWidth - parentX - panel.getWidth();
            }
            int parentHeight = parentComponent.getHeight();
            if (newY + panel.getHeight() > parentHeight - parentY) {
                newY = parentHeight - parentY - panel.getHeight();
            }

            panel.setLocation(newX, newY);

            dX = e.getLocationOnScreen().x - panel.getX();
            dY = e.getLocationOnScreen().y - panel.getY();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            JPanel panel = (JPanel) e.getComponent();
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            dX = e.getLocationOnScreen().x - panel.getX();
            dY = e.getLocationOnScreen().y - panel.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            JPanel panel = (JPanel) e.getComponent();
            panel.setCursor(null);
        }
    }

    public void actionStarted() {
        int nbActions = m_actionButtonArray.length;
        for (int i = 0; i < nbActions; i++) {
            m_actionButtonArray[i].setEnabled(false);
        }

        setLoading(1, true);
    }

    public void actionFinished(boolean success, String errorMessage) {
        int nbActions = m_actionButtonArray.length;
        for (int i = 0; i < nbActions; i++) {
            m_actionButtonArray[i].setEnabled(true);
        }
        setLoaded(1);
        setVisible(false);
        if (!success) {
            InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Error", errorMessage, true);
            errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
            errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            errorDialog.setVisible(true);
        }
    }

}
