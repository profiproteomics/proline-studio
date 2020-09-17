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

package fr.proline.studio.main;



import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.rsmexplorer.MainFrame;
import fr.proline.studio.rsmexplorer.SplashScreenWindow;
import fr.proline.studio.WindowManager;


import javax.swing.*;
import java.awt.*;

public class Main {




    public static void main(String[] args) {




        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    SplashScreenWindow splashScreen = new SplashScreenWindow();
                    splashScreen.setVisible(true);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {


                            JFrame frame = MainFrame.getInstance();

                            initTheme();



                            frame.setVisible(true);
                            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

                            splashScreen.setVisible(false);
                        }
                    });




                }
            });
        } catch (Exception e) {

        }
    }


    public static void initTheme() {
        // for Mac : we need to use Metal UI, otherwise the browse file on server does not work
        setUI();


        // initialize the connection to the server as soon as possible
        ServerConnectionManager.getServerConnectionManager();
    }


    private static void setUI() {
        String OS = System.getProperty("os.name").toLowerCase();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    try {
                        if (OS.contains("mac")) {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        } else if (OS.contains("win")) {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        }

                        Frame f = WindowManager.getDefault().getMainWindow();
                        if (f == null) {
                            // should never be null
                            setUI();
                        } else {
                            SwingUtilities.updateComponentTreeUI(f);
                        }
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                    }

                }

            });
        }
    }
