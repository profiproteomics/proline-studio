package fr.proline.studio.dock;

import fr.proline.studio.dock.container.DockContainer;

import javax.swing.*;
import java.util.HashSet;

public abstract class AbstractDockFrame extends JFrame {

    public AbstractDockFrame(String title) {
        super(title);
    }

    public abstract void closeWindow(AbstractTopPanel topPanel);

    public abstract void displayWindow(AbstractTopPanel topPanel);

    public abstract boolean isDisplayed(String windowKey);

    public abstract void toFront(String windowKey);

    public abstract HashSet<AbstractTopPanel> getTopPanels();
}
