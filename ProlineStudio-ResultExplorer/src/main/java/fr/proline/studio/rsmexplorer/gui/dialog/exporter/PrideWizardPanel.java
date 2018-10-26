package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.WizardPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author VD225637
 */
public abstract class PrideWizardPanel extends JPanel {

    protected void initWizardPanels(String wizardLabel){
        setLayout(new BorderLayout());
        add(new WizardPanel(wizardLabel), BorderLayout.PAGE_START);
        add(createMainPanel(), BorderLayout.CENTER);
    }
    
    protected abstract JPanel createMainPanel();
    protected abstract HashMap<String, Object> getExportPrideParams();

    //Return component which is invalid
    protected abstract Component checkExportPrideParams();
    //Return message explaining invalid component
    protected abstract String getErrorMessage();
    
}
