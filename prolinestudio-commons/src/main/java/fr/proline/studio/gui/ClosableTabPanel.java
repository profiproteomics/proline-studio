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
package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * Tab for a JTabbedPane that the user can close through a button
 * 
 * @author JM235353
 */
public class ClosableTabPanel extends JPanel {

    private JTabbedPane m_tabbedPane;

    public ClosableTabPanel(JTabbedPane tabbedPane, String title, String sheetId) {
        setLayout(new FlowLayout());

        setOpaque(false);

        m_tabbedPane = tabbedPane;

        JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeButton.setOpaque(false);
        JLabel label = new JLabel(title);

        add(removeButton);
        add(label);

        final ClosableTabPanel _this = this;
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // JPM.WART : big wart due to the fact that dnd changes the index of tabs
                int indexCur = -1;
                int nbTabs = m_tabbedPane.getTabCount();
                for (int i = 0; i < nbTabs; i++) {
                    Component tabComponent = m_tabbedPane.getTabComponentAt(i);
                    if (tabComponent == _this) {
                        indexCur = i;
                        break;
                    }
                }

                if (indexCur != -1) {
                    m_tabbedPane.removeTabAt(indexCur);
                }


            }
        });

    }



}
