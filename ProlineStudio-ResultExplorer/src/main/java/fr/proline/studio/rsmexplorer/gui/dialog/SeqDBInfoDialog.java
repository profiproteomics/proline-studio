package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.InfoDialog;
import java.awt.Window;


/**
 * Dialog used to display a warning when the SeqDB is not available.
 * @author JM235353
 */
public class SeqDBInfoDialog extends InfoDialog {
    
    private static String SHOW_AT_START_KEY = "Show_SeqDb_Warning_At_Start";
    
    public SeqDBInfoDialog(Window parent) {
        super(parent, InfoDialog.InfoType.WARNING, "No Protein Sequence Database Found", "There is no Protein Sequence Database available.\nPlease contact your IT Administrator to install it.", SHOW_AT_START_KEY);
        setButtonName(InfoDialog.BUTTON_OK, "OK");
        setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
    }
    
     public static boolean showAtStart() {
        return showAtStart(SHOW_AT_START_KEY);
    }
    
}
