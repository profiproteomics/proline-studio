package fr.proline.studio.python.interpreter;

import java.util.ArrayList;

/**
 *
 * Callback used to return results after a calculation
 * 
 * @author JM235353
 */
public abstract class CalcCallback {
    
    public abstract void run(ArrayList<ResultVariable> variables, CalcError error);
}
