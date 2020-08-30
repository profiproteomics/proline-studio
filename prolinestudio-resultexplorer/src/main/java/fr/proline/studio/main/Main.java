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

        /*
        String productVersion = moduleVersion;
        int firstIndex = moduleVersion.indexOf('.');
        int secIndex =  moduleVersion.length();
        if(firstIndex >0)
            secIndex = moduleVersion.indexOf('.', firstIndex+1);
        if(secIndex>0)
            productVersion = moduleVersion.substring(0, secIndex);

        String buildnumber = productVersion+" Milestone ("+moduleBuildDate+")"; //specify if Milestone, Release Candidate or release (or nothing = release)

        // set the proline version for the application title
        System.setProperty("netbeans.buildnumber", buildnumber);  //"1.0.1 (alpha : build date @build.date@)"
*/
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


        /*
    private static String[] parseJMSServerURL(String serverURL) {
        String[] hostAndPort = new String[2];

        String parsedURL = serverURL;
        int portSep = parsedURL.indexOf(":");
        if (portSep > 0) {
            hostAndPort[0] = parsedURL.substring(0, portSep);
            hostAndPort[1] = parsedURL.substring(portSep + 1);
        } else {
            hostAndPort[0] = parsedURL;
        }

        return hostAndPort;
    }*/

//JPM.DOCK.TEST
//TEST JMS
/*

        try {
            String[] jmsHostAndPort = parseJMSServerURL("localhost");
            JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(jmsHostAndPort[0]);
            if (jmsHostAndPort[1] != null) {
                JMSConnectionManager.getJMSConnectionManager().setJMSServerPort(Integer.parseInt(jmsHostAndPort[1]));
            }
            //Try to connect to server
            JMSConnectionManager.getJMSConnectionManager().getJMSConnection();
        } catch (Exception e) {
        }


        final HashMap<Object, Object> databaseProperties = new HashMap<>();
        final String[] databasePassword = new String[1];
        databasePassword[0] = "";

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {

                        String password = databasePassword[0];

                    GetDBConnectionTemplateTask task = new GetDBConnectionTemplateTask(null, password, databaseProperties);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);


                } else {
                    System.err.println("ERREUR");
                }
            }
        };


        int count =30;
        while(count>0) {



            AuthenticateUserTask task = new AuthenticateUserTask(callback, "menetrey", "proline", databasePassword);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
count--;
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

