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
