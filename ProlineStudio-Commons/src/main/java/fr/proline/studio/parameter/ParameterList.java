package fr.proline.studio.parameter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;

/**
 *
 * @author jm235353
 */
public class ParameterList extends ArrayList<AbstractParameter> {

    private String name;
    private JPanel parametersPanel;

    public ParameterList(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public JPanel getPanel() {

        if (parametersPanel != null) {
            return parametersPanel;
        }

        Preferences preferences = NbPreferences.root();
        String prefixKey = name.replaceAll(" ", "_")+".";
        
        
        parametersPanel = new JPanel();
        parametersPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;


        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            String parameterName = parameter.getName();
            String suffixKey = parameterName.replaceAll(" ", "_")+".";
            
            c.gridx = 0;
            c.weightx = 0;
            JLabel l = new JLabel(parameter.getName()+" :");
            l.setHorizontalAlignment(JLabel.RIGHT);
            parametersPanel.add(l, c);

            String parameterValue = preferences.get(prefixKey+suffixKey, null);
            
            
            c.gridx = 1;
            c.weightx = 1;
            parametersPanel.add(parameter.getComponent(parameterValue), c);

            c.gridy++;

        }

        return parametersPanel;
    }
    
    public void completePanel(JPanel p, GridBagConstraints c) {
        Preferences preferences = NbPreferences.root();
        String prefixKey = name.replaceAll(" ", "_")+".";
        
         int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);
            
            String parameterName = parameter.getName();
            String suffixKey = parameterName.replaceAll(" ", "_")+".";
            
            c.gridy++;
            
            c.gridx = 0;
            c.weightx = 0;
            JLabel l = new JLabel(parameter.getName()+" :");
            l.setHorizontalAlignment(JLabel.RIGHT);
            p.add(l, c);

            String parameterValue = preferences.get(prefixKey+suffixKey, null);
            
            
            c.gridx = 1;
            c.weightx = 1;
            p.add(parameter.getComponent(parameterValue), c);

            

        }
    }
    
    
    public void initDefaults() {

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            parameter.initDefault();
        }
    }
    
    public void saveParameters() {
        Preferences preferences = NbPreferences.root();
        String prefixKey = name.replaceAll(" ", "_")+".";
        
        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            String parameterName = parameter.getName();
            String suffixKey = parameterName.replaceAll(" ", "_")+".";
        
            String key = prefixKey+suffixKey;
            String value = parameter.getStringValue();
            preferences.put(key, value);
        }
    }
    
    public HashMap<String, String> getValues() {
        HashMap<String, String> valuesMap = new HashMap<String, String>();
        
        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);
            String key = parameter.getKey();
            String value = parameter.getStringValue();
            valuesMap.put(key, value);
        }
        
        return valuesMap;
        
    }
    
    public AbstractParameter getParameter(String key) {
        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);
            if (parameter.getKey().compareTo(key) ==0) {
                return parameter;
            }
        }
        return null;
    }
    
    
    public ParameterError checkParameters() {
        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);
            ParameterError error = parameter.checkParameter();
            if (error != null) {
                return error;
            }
        }
        return null;
    }
    
    
}
