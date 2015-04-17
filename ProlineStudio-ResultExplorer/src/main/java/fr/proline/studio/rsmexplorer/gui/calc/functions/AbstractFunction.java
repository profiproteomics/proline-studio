package fr.proline.studio.rsmexplorer.gui.calc.functions;

/**
 *
 * @author JM235353
 */
public abstract class AbstractFunction {
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process();
    
}
