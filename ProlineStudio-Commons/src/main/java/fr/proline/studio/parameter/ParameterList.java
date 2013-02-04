package fr.proline.studio.parameter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

    public String toString() {
        return name;
    }

    public JPanel getPanel() {

        if (parametersPanel != null) {
            return parametersPanel;
        }

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

            c.gridx = 0;
            c.weightx = 0;
            parametersPanel.add(new JLabel(parameter.getName()), c);

            c.gridx = 1;
            c.weightx = 1;
            parametersPanel.add(parameter.getComponent(), c);

            c.gridy++;

        }

        return parametersPanel;
    }
}
