package fr.proline.studio.rsmexplorer.gui.calc.parameters;

import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;

/**
 *
 * @author JM235353
 */
public interface CheckParameterInterface {
    public ParameterError checkParameters(AbstractGraphObject[] graphObjects);
}
