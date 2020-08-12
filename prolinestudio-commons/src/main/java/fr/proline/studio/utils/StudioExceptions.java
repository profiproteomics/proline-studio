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
package fr.proline.studio.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.ImageIcon;

import fr.proline.studio.Exceptions;
import fr.proline.studio.ImageUtilities;
import fr.proline.studio.WindowManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class StudioExceptions {
    
    private static ImageIcon exceptionIcon = ImageUtilities.loadImageIcon("org/netbeans/core/resources/exception.gif", false);
        
    public static void notify(String message, Throwable throwable) {
        WindowManager.getDefault().notify("Proline Studio Error", exceptionIcon, message, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Exceptions.printStackTrace( throwable /*Exceptions.attachSeverity(throwable, Level.SEVERE)*/); //JPM.DOCK
            }
        });
    }

    public static void logAndNotify(String loggerName, String message, Throwable throwable) {
        LoggerFactory.getLogger(loggerName).error(message, throwable);
        notify(message, throwable);
    }
}
