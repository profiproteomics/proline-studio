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
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {

                    SplashScreenWindow splashScreen = new SplashScreenWindow();
                    splashScreen.setVisible(true);

                    JFrame frame = MainFrame.getInstance();

                    initTheme();



                    frame.setVisible(true);

                    splashScreen.setVisible(false);
                }
            });
        } catch (Exception e) {

        }
    }

    public static void initTheme() {
        // for Mac : we need to use Metal UI, otherwise the browse file on server does not work
        forceMetalUIForMac();

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


    private static void forceMetalUIForMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("mac")) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        Frame f = WindowManager.getDefault().getMainWindow();
                        if (f == null) {
                            // should never be null
                            forceMetalUIForMac();
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



}
