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
package fr.proline.studio.pattern;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;


/**
 * Action called to save a window
 * @author JM235353
 */
public class SaveDataBoxActionListener  implements ActionListener {

    private SplittedPanelContainer m_splittedPanel;
    
    public SaveDataBoxActionListener(SplittedPanelContainer splittedPanel) {
        m_splittedPanel = splittedPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String windowName = showSelectNameDialog();
        if (windowName == null) {
            return;
        }
        
        String windowSaved = saveSplittedPanelContainer(windowName, m_splittedPanel);
        
        WindowSavedManager.addSavedWindow(windowSaved);
  
    }
    
    public static String saveParentContainer(String windowName, Container c) {
        while ((c != null) && !(c instanceof SplittedPanelContainer)) {
            c = c.getParent();
        }
        if (c == null) {
            return null;
        }
        return saveSplittedPanelContainer(windowName, (SplittedPanelContainer) c);
    }
    public static String saveSplittedPanelContainer(String windowName, SplittedPanelContainer splittedPanel) {
        
        // window name must have no # characters and spaces at start and end.
        windowName = windowName.trim();
        if (windowName.indexOf('#') != -1) {
            windowName.replaceAll("#", "_");
        }
        
        ArrayList<JPanel> panelList = new ArrayList<>();
        ArrayList<SplittedPanelContainer.PanelLayout> layoutList = new ArrayList<>();
        splittedPanel.generateListOfPanels(panelList, layoutList);

        ArrayList<AbstractDataBox> boxList = new ArrayList<>();

        for (int i = 0; i < panelList.size(); i++) {
            JPanel p = panelList.get(i);
            if (p instanceof DataBoxPanelInterface) {
                DataBoxPanelInterface databoxPanelInterface = (DataBoxPanelInterface) p;
                AbstractDataBox b = databoxPanelInterface.getDataBox();
                boxList.add(b);
            }

        }
        
        String windowSaved = WindowSavedManager.writeBoxes(windowName, boxList, layoutList);
        
        return windowSaved;
    }
    
    private String showSelectNameDialog() {
        
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Select Window Name", null, "Window Name", OptionDialog.OptionDialogType.TEXTFIELD);
        dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        String newName = null;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            newName = dialog.getText();
        }
        
        if ((newName != null) && (newName.length() > 0)) {
            return newName;
        }
        
        return null;
    }
}
