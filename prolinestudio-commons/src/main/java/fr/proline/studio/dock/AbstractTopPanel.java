package fr.proline.studio.dock;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractTopPanel extends JPanel {


    public abstract String getTopPanelIdentifierKey();

    public abstract Image getIcon();

    public abstract String getTitle();

}
