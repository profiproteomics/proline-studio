package fr.proline.studio.parameter;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface SettingsInterface {
    public ArrayList<ParameterList> getParameters();
    public void parametersChanged();
}
