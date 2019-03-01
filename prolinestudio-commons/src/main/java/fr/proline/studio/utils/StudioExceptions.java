/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class StudioExceptions {
    
    private static ImageIcon exceptionIcon = ImageUtilities.loadImageIcon("org/netbeans/core/resources/exception.gif", false);
        
    public static void notify(String message, Throwable throwable) {
        NotificationDisplayer.getDefault().notify("Proline Studio Error", exceptionIcon, message, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Exceptions.printStackTrace(Exceptions.attachSeverity(throwable, Level.SEVERE));
            }
        });
    }

    public static void logAndNotify(String loggerName, String message, Throwable throwable) {
        LoggerFactory.getLogger(loggerName).error(message, throwable);
        notify(message, throwable);
    }
}
