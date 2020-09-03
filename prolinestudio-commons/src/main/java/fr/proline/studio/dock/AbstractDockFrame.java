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

package fr.proline.studio.dock;


import fr.proline.studio.dock.gui.InfoLabel;

import javax.swing.*;
import java.util.HashSet;

public abstract class AbstractDockFrame extends JFrame {

    public AbstractDockFrame(String title) {
        super(title);
    }

    public abstract void closeWindow(AbstractTopPanel topPanel);

    public abstract void displayWindow(AbstractTopPanel topPanel);

    public abstract void resetWindows();

    public abstract void closeDataWindows();

    public abstract boolean isDisplayed(String windowKey);

    public abstract void toFront(String windowKey);

    public abstract HashSet<AbstractTopPanel> getTopPanels();

    public abstract void alert(InfoLabel.INFO_LEVEL level, String message, Throwable t);
    public abstract void alert(InfoLabel.INFO_LEVEL level, Throwable t);

    public abstract void addLog();
}
