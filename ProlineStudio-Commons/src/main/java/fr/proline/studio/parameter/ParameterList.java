package fr.proline.studio.parameter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.util.NbPreferences;

/**
 * List of Parameters; possibility to generate a panel from this list
 *
 * @author jm235353
 */
public class ParameterList extends ArrayList<AbstractParameter> {

    private String m_name;
    private JPanel m_parametersPanel;
    private boolean m_enable = true;

    private final HashMap<JComponent, JLabel> m_associatedLabels = new HashMap<>();

    public ParameterList(String name) {
        m_name = name;
    }

    @Override
    public String toString() {
        return m_name;
    }

    public String getPrefixName(){
       return m_name.replaceAll(" ", "_") + ".";
    }

    public JPanel getPanel() {

        if (m_parametersPanel != null) {
            return m_parametersPanel;
        }

        Preferences preferences = NbPreferences.root();
        String prefixKey = m_name.replaceAll(" ", "_") + ".";

        m_parametersPanel = new JPanel();
        m_parametersPanel.setLayout(new GridBagLayout());

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
            String suffixKey = parameterName.replaceAll(" ", "_");

            c.gridx = 0;
            c.weightx = 0;
            JLabel l = null;
            if (parameter.showLabel() == AbstractParameter.LabelVisibility.VISIBLE) {
                l = new JLabel(parameter.getName() + " :");

                l.setHorizontalAlignment(JLabel.RIGHT);
                m_parametersPanel.add(l, c);
            }

            String parameterValue = preferences.get(prefixKey + suffixKey, null);


            if (parameter.hasComponent()) {

                JComponent comp = null;
                if (parameter.showLabel() == AbstractParameter.LabelVisibility.AS_BORDER_TITLE) {
                    c.gridx = 0;
                    c.gridwidth = 2;
                    c.weightx = 1;
                    JPanel framedPanel = new JPanel(new GridBagLayout());
                    framedPanel.setBorder(BorderFactory.createTitledBorder(" " + parameter.getName() + " "));
                    GridBagConstraints cFrame = new GridBagConstraints();
                    cFrame.anchor = GridBagConstraints.NORTHWEST;
                    cFrame.fill = GridBagConstraints.BOTH;
                    cFrame.insets = new java.awt.Insets(5, 5, 5, 5);
                    cFrame.weightx = 1;
                    comp = parameter.getComponent(parameterValue);
                    JScrollPane scroll = null;
                    if (parameter.componentNeedsScrollPane()) {
                        scroll = new JScrollPane(comp) {

                            private Dimension maximumSize = new Dimension(360, 400);

                            @Override
                            public Dimension getMaximumSize() {
                                return maximumSize;
                            }
                        };
                    }
                    framedPanel.add((scroll != null) ? scroll : comp, cFrame);
                    m_parametersPanel.add(framedPanel, c);
                    c.gridwidth = 1;
                } else {
                    c.gridx = 1;
                    c.weightx = 1;
                    comp = parameter.getComponent(parameterValue);
                    JScrollPane scroll = null;
                    if (parameter.componentNeedsScrollPane()) {
                        scroll = new JScrollPane(comp) {

                            private Dimension maximumSize = new Dimension(360, 400);

                            @Override
                            public Dimension getMaximumSize() {
                                return maximumSize;
                            }
                        };
                    }
                    m_parametersPanel.add((scroll != null) ? scroll : comp, c);
                }
                if ((l != null) && (comp != null)) {
                    m_associatedLabels.put(comp, l);
                }
            }

            c.gridy++;

        }

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        m_parametersPanel.add(Box.createVerticalGlue(), c);

        return m_parametersPanel;
    }

    public JLabel getAssociatedLabel(JComponent c) {
        return m_associatedLabels.get(c);
    }

    public void completePanel(JPanel p, GridBagConstraints c) {
        Preferences preferences = NbPreferences.root();
        String prefixKey = m_name.replaceAll(" ", "_") + ".";

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            String parameterName = parameter.getName();
            String suffixKey = parameterName.replaceAll(" ", "_");

            c.gridy++;

            c.gridx = 0;
            c.weightx = 0;
            JLabel l = new JLabel(parameter.getName() + " :");
            l.setHorizontalAlignment(JLabel.RIGHT);
            p.add(l, c);

            String parameterValue = preferences.get(prefixKey + suffixKey, null);

            if (parameter.hasComponent()) {
                c.gridx = 1;
                c.weightx = 1;
                p.add(parameter.getComponent(parameterValue), c);
            }

        }
    }

    public void updateIsUsed(Preferences preferences) {
        String prefixKey = m_name.replaceAll(" ", "_") + ".";

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            String parameterKey = parameter.getKey();
            String suffixKey = parameterKey.replaceAll(" ", "_");

            String key = prefixKey + suffixKey;


            String parameterValue = preferences.get(key, null);

            //JPM.WART ------------
            if (parameterValue == null) {
                // no value found, for backward compatibility, load the parameter from the name as a key (there was a bug, name was used as the key)
                parameterKey = parameter.getName();
                suffixKey = parameterKey.replaceAll(" ", "_");

                key = prefixKey + suffixKey;

                parameterValue = preferences.get(key, null);
            }
            if (parameterValue == null) {
                // no value found, for backward compatibility, load the parameter from the backward compatible key 
                parameterKey = parameter.getBackwardCompatibleKey();
                if (parameterKey != null) {
                    suffixKey = parameterKey.replaceAll(" ", "_");

                    key = prefixKey + suffixKey;

                    parameterValue = preferences.get(key, null);
                }
            }
            // -------------
            
            
            if (!parameter.isUsed()) {
                if ((parameterValue != null) && (!parameterValue.isEmpty())) {
                    parameter.setUsed(true);
                }
            } else {
                if ((parameterValue == null) || (parameterValue.isEmpty())) {
                    parameter.setUsed(false);
                }
            }

            //Used to initialize values            
            parameter.getComponent(parameterValue);
        }
    }

    public void initDefaults() {

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            parameter.initDefault();
        }
    }

    public void saveParameters(Preferences preferences) {

        String prefixKey = m_name.replaceAll(" ", "_") + ".";

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            
            // JPM.WART remove any old parameter in the file (constructed with the name of the parameter instead of the key ----------
            String parameterKey = parameter.getName();
            String suffixKey = parameterKey.replaceAll(" ", "_");
            String key = prefixKey + suffixKey;
            preferences.remove(key);

            parameterKey = parameter.getBackwardCompatibleKey();
            if (parameterKey != null) {
                suffixKey = parameterKey.replaceAll(" ", "_");

                key = prefixKey + suffixKey;

                preferences.remove(key);
            }
            // --------------
            
            
            parameterKey = parameter.getKey();
            suffixKey = parameterKey.replaceAll(" ", "_");

            key = prefixKey + suffixKey;

            if (parameter.isUsed()|| parameter.isCompulsory()) {
                String value = parameter.getStringValue();
                preferences.put(key, value);
            } else {
                preferences.remove(key);
            }
            
            

        }
    }


    public void loadParameters(Preferences preferences) {

        String prefixKey = m_name.replaceAll(" ", "_") + ".";

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            
            // load the parameter from the correct key
            String parameterKey = parameter.getKey();
            String suffixKey = parameterKey.replaceAll(" ", "_");

            String key = prefixKey + suffixKey;

            String value = preferences.get(key, null);
            
            //JPM.WART ------------
            if (value == null) {
                // no value found, for backward compatibility, load the parameter from the name as a key (there was a bug, name was used as the key)
                parameterKey = parameter.getName();
                suffixKey = parameterKey.replaceAll(" ", "_");

                key = prefixKey + suffixKey;

                value = preferences.get(key, null);
            }
            if (value == null) {
                // no value found, for backward compatibility, load the parameter from the backward compatible key 
               parameterKey = parameter.getBackwardCompatibleKey();
                if (parameterKey != null) {
                    suffixKey = parameterKey.replaceAll(" ", "_");

                    key = prefixKey + suffixKey;

                    value = preferences.get(key, null);
                }
            }
            // -------------

            if (value == null) {
                 continue;
            }

            parameter.setValue(value);

        }

        updateIsUsed(preferences);
    }

    public HashMap<String, String> getValues() {
        HashMap<String, String> valuesMap = new HashMap<>();

        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);

            if (!parameter.isUsed()) {
                continue;
            }

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
            if (parameter.getKey().compareTo(key) == 0) {
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

    public void clean() {
        int nbParameters = size();
        for (int i = 0; i < nbParameters; i++) {
            AbstractParameter parameter = get(i);
            parameter.clean();
        }
    }

    public void enableList(boolean v) {
        m_enable = v;
    }

    public boolean isEnable() {
        return m_enable;
    }

}
