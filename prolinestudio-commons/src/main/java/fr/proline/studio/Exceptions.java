package fr.proline.studio;

import fr.proline.studio.dock.gui.InfoLabel;
import org.slf4j.LoggerFactory;

public class Exceptions {

    public static void printStackTrace(Throwable t) {
        LoggerFactory.getLogger("ProlineStudio.Commons").error(t.getMessage(), t);
        WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, t);
    }
}
