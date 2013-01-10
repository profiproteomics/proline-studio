/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import java.util.HashMap;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public class IconManager {
    
    public enum IconType {
        OK,
        CANCEL,
        DEFAULT,
        SAME_SET,
        SUB_SET,
        ADD_REMOVE,
        HOUR_GLASS,
        GEL,
        RSET,
        RSM,
        VIAL,
        PROJECT,
        USER,
        MAXIMIZE,
        MINIMIZE,
        DELETE
    }
    
    private static HashMap<IconType, ImageIcon> iconMap = new HashMap<>();
    

    public static ImageIcon getIcon(IconType iconType) {
        
        ImageIcon icon = iconMap.get(iconType);
        if (icon == null) {
            String path = getIconFilePath(iconType);
            icon = ImageUtilities.loadImageIcon(path, false);
            iconMap.put(iconType, icon);
        }
        
        return icon;
    }
    
    private static String getIconFilePath(IconType iconType) {
        switch (iconType) {
            case OK:
                return "fr/proline/studio/images/tick.png";
            case CANCEL:
                return "fr/proline/studio/images/tick-red.png";
            case DEFAULT:
                return "fr/proline/studio/images/arrow-circle.png";
            case SAME_SET:
                return "fr/proline/studio/images/sameset.png";
            case SUB_SET:
                return "fr/proline/studio/images/subset.png";
            case ADD_REMOVE:
                return "fr/proline/studio/images/addremove.png";
            case HOUR_GLASS:
                return "fr/proline/studio/images/hourGlass.png";
            case GEL:
                return "fr/proline/studio/images/gel.png";
            case RSET:
                return "fr/proline/studio/images/resultSet.png";
            case RSM:
                return "fr/proline/studio/images/resultSummary.png";
            case VIAL:
                return "fr/proline/studio/images/vial.png";
            case PROJECT :
                return "fr/proline/studio/images/project.png";
            case USER:
                return "fr/proline/studio/images/user.png";
            case MAXIMIZE:
                return "fr/proline/studio/images/maximize.png";
            case MINIMIZE:
                return "fr/proline/studio/images/minimize.png";
            case DELETE:
                return "fr/proline/studio/images/deleteBlue.png";
        }
        

            

    
        
        return null; // can not happen
    }

    
}
