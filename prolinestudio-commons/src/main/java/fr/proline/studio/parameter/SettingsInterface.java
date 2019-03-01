package fr.proline.studio.parameter;

import java.util.ArrayList;

/**
 * Interface to be implemented when we want to use the SettingButton to add settings
 * to a view.
 * @author JM235353
 */
public interface SettingsInterface {
    public ArrayList<ParameterList> getParameters();
    public void parametersChanged();
    public boolean parametersCanceled();
}
