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
package fr.proline.studio.info;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;
import org.openide.windows.WindowManager;

/**
 * Button to access to an info floating panelo
 * 
 * @author JM235353
 */
public class InfoToggleButton extends JToggleButton {

    private ProgressInterface m_progressInterface = null;

    private InfoInterface m_infoInterface = null;

    private InfoFloatingPanel m_infoPanel = null;
    
    /**
     * Constructor for search on JXTable
     * @param progressInterface
     * @param infoInterface 
     */
    public InfoToggleButton(ProgressInterface progressInterface, InfoInterface infoInterface) {
        init(progressInterface, infoInterface);
        initGraphic();
    }

    
    public final void init(ProgressInterface progressInterface, InfoInterface infoInterface) {
        m_progressInterface = progressInterface;
        m_infoInterface = infoInterface;

    }
    
    private void initGraphic() {
        
        m_infoPanel = new InfoFloatingPanel();
        m_infoPanel.setToggleButton(this);
        
        setIcon(IconManager.getIcon(IconManager.IconType.INFORMATION));

        setFocusPainted(false);

        addActionListener(new ActionListener() {

            boolean firstTime = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Info is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        setSelected(false);
                        return;
                    }
                }
                
                updateInfo();
                
                m_infoPanel.setVisible(isSelected());

                if (firstTime) {
                    firstTime = false;
                    m_infoPanel.setLocation(getX() + getWidth() + 5, getY() + 5);
                }
            }
        });
    }
    
    public InfoFloatingPanel getInfoPanel() {
        return m_infoPanel;
    }

    public void updateInfo() {
        if (isSelected()) {
            m_infoPanel.setInfo(m_infoInterface.getInfo());
        }
    }
    
}

