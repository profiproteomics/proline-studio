package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;

/**
 * 
 * Action displayed as a menu to open the Data Analyzer window
 * 
 * @author JM235353
 */

@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.actions.DataAnalyzerAction")
@ActionRegistration(displayName = "#CTL_DataAnalyzerAction")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 100)
})
@NbBundle.Messages("CTL_DataAnalyzerAction=Data Analyzer")
public final class DataAnalyzerAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DataAnalyzerWindowBoxManager.openDataAnalyzer();
    }
}
