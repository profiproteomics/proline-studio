/*
 * Copyright (C) 2019 VD225637
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

package fr.proline.studio.dock.gui;

import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.io.StringWriter;

public class InfoLabel extends JLabel implements MouseListener {


    public enum INFO_LEVEL {
        INFO,
        WARNING,
        ERROR
    }

    private INFO_LEVEL m_infoLevel = INFO_LEVEL.INFO;
    private Throwable m_throwable = null;

    public InfoLabel() {
        setInfo(INFO_LEVEL.INFO, "", null);

        addMouseListener(this);
    }


    public void setInfo(INFO_LEVEL infoLevel, Throwable t) {
        setInfo(infoLevel, t.getMessage(), t);

    }

    public void setInfo(INFO_LEVEL infoLevel, String message, Throwable t) {
        switch (infoLevel) {
            case INFO:
                setIcon(IconManager.getIcon(IconManager.IconType.INFORMATION));
                break;
            case WARNING:
                setIcon(IconManager.getIcon(IconManager.IconType.WARNING));
                break;
            case ERROR:
                setIcon(IconManager.getIcon(IconManager.IconType.EXCLAMATION));
                break;
        }
        setText(message);

        m_throwable = t;

        if (m_throwable != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }



    @Override
    public void mouseClicked(MouseEvent e) {
        if (m_throwable != null) {
            ExceptionDialog.getSingleton(m_throwable.getMessage(), m_throwable).setVisible(true);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }



    private static class ExceptionDialog extends DefaultDialog {

        private JTextArea m_textArea;

        private String _requestContent = "";
        private boolean m_firstDisplay = true;

        private static ExceptionDialog m_singleton = null;

        private ExceptionDialog() {
            super(WindowManager.getDefault().getMainWindow());
            setTitle("Exception");

            setButtonVisible(BUTTON_CANCEL, false);
            setButtonVisible(BUTTON_HELP, false);
            setStatusVisible(false);


            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setPreferredSize(new Dimension(600, 400));
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);


            m_textArea = new JTextArea();
            m_textArea.setText(_requestContent);
            m_textArea.setCaretPosition(0);
            m_textArea.setEditable(false);
            m_textArea.setLineWrap(true);
            // --- add objects
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            JScrollPane top = new JScrollPane(m_textArea);
            mainPanel.add(top, c);

            setInternalComponent(mainPanel);

            setResizable(true);
        }

        public static ExceptionDialog getSingleton(String message, Throwable t) {
            if (m_singleton == null) {
                m_singleton = new ExceptionDialog();

            }

            m_singleton.setContent(message, t);

            return m_singleton;
        }

        @Override
        public void setVisible(boolean v) {
            super.setVisible(v);

            if (m_firstDisplay) {
                m_singleton.centerToWindow(WindowManager.getDefault().getMainWindow());
                m_firstDisplay = false;
            }
        }

        private void setContent(String message, Throwable t) {
            setTitle(message);

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            m_textArea.setText(sw.toString());


        }


    }



}
