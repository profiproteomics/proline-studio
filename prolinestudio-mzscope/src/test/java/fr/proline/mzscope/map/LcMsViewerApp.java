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
package fr.proline.mzscope.map;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import fr.proline.mzscope.map.ui.LcMsViewerUI;

/**
 * @author JeT
 *
 */
public class LcMsViewerApp {

    private static String filename = "src/test/resources/fr/proline/mzscope/map/OECCF131220_08.raw.mzDB";

    /**
     * @param args
     */
    public static void main(String[] args) {
	// Set cross-platform Java L&F (also called "Metal")
	try {
	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		if ("Nimbus".equals(info.getName())) {
		    UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (Exception e) {
	    // If Nimbus is not available, you can set the GUI to another look and feel.
	}

	JFrame frame = new JFrame("LCMS Map Viewer");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	LcMsViewer viewer = new LcMsViewer(new LcMsMap(filename));
	LcMsViewerUI viewerUI = new LcMsViewerUI();
	frame.getContentPane().add(viewerUI.getGui(), BorderLayout.CENTER);
	frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	frame.setVisible(true);
	viewerUI.setViewer(viewer);
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	viewerUI.getController().changeViewport(new LcMsViewport(390, 440, 1000, 1150));
    }

}
