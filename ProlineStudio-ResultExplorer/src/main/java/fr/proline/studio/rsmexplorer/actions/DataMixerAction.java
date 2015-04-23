package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Window",
id = "fr.proline.studio.rsmexplorer.actions.DataMixerAction")
@ActionRegistration(displayName = "#CTL_DataMixerAction")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 100)
})
@NbBundle.Messages("CTL_DataMixerAction=Data Mixer")
public final class DataMixerAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DataMixerWindowBoxManager.openDataMixer();

    }
}
