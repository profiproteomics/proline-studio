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

import fr.proline.studio.JavaVersion;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;


public class AboutDialog extends DefaultDialog {

    public AboutDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setButtonVisible(BUTTON_CANCEL, false);
        setButtonVisible(BUTTON_HELP, false);
        setStatusVisible(false);

        Component internalComponent = createInternalComponent();

        setInternalComponent(internalComponent);
    }

    private Component  createInternalComponent() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5,5,5, 5);


        internalPanel.add(new AboutImagePanel(), c);

        c.weightx = 1;
        c.weighty = 1;
        c.gridy++;

        String copyrightText = "<html>Proline Studio is developped in the context of ProFI ( French Proteomics National Infrastructure).<br><br>3 laboratories contribute to this software:<br>- iRTSV (Grenoble): EDyP Team, BGE Laboratory (U1038 CEA/INSERM/UJF)<br>- IPBS (Toulouse):UMR 5089 (CNRS/UPS)<br>- LSMBO (Strasbourg) :UMR 7178 (CNRS/UdS)<br><br>Version: "+ JavaVersion.getProductVersion()+" / "+JavaVersion.getProductDate()+"</html>";
        JLabel textCopyrightLabel = new JLabel(copyrightText);
        internalPanel.add(textCopyrightLabel, c);

        return internalPanel;

    }



    private class AboutImagePanel extends JPanel {

        private Dimension m_dimension;

        private static final int PAD = 5;

        @Override
        public Dimension getPreferredSize() {
            if (m_dimension == null) {
                Image img = IconManager.getImage(IconManager.IconType.SPLASH);
                m_dimension = new Dimension(img.getWidth(null)+PAD*2, img.getHeight(null)+PAD*2);
            }

            return m_dimension;
        }

        public void paint(Graphics g) {

            g.setColor(Color.white);
            g.fillRect(0,0, getWidth(), getHeight());

            Image img = IconManager.getImage(IconManager.IconType.SPLASH);
            g.drawImage(img, PAD, PAD, null);


        }


    }

}
