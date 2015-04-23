package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public abstract class AbstractFunction {
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process();

    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.FUNCTION);
    }
    
}
