package fr.proline.studio.rsmexplorer.gui.dialog.pride;

import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author VD225637
 */
public abstract class PrideWizardPanel extends JPanel {
    
       

    protected void initWizardPanels(String wizardLabel){
        setLayout(new BorderLayout());
        add(createWizardPanel(wizardLabel), BorderLayout.PAGE_START);
//        JPanel mainp = createMainPanel();
//        mainp.setBorder(BorderFactory.createLineBorder(Color.yellow));
        add(createMainPanel(), BorderLayout.CENTER);
    }
    
    protected abstract JPanel createMainPanel();
    protected abstract HashMap<String, Object> getExportPrideParams();

    //Return component which is invalid
    protected abstract Component checkExportPrideParams();
    //Return message explaining invalid component
    protected abstract String getErrorMessage();
    
    private JPanel createWizardPanel(String label) {
        JPanel wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel wizardLabel = new JLabel(label);
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);

        return wizardPanel;
    }
}
