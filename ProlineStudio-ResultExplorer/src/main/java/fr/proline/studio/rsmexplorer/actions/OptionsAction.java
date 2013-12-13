package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.gui.OptionDialog;
import fr.proline.util.system.OSInfo;
import fr.proline.util.system.OSType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

@ActionID(category = "Window",
id = "fr.proline.studio.rsmexplorer.actions.OptionsAction")
@ActionRegistration(displayName = "#CTL_OptionsAction")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 0)
})
@Messages("CTL_OptionsAction=Server Option")
public final class OptionsAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // check that the server file path is defined
        Preferences preferences = NbPreferences.root();
        String serverFilePath = preferences.get("ServerIdentificationFilePath", "");


        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Server Search Result File Path", "Please define the file path where the search results (Mascot, Omssa files...) are saved.\nAsk to your IT Administrator if you don't know where it is.\nIf the data server is running on your computer, the file path can be empty.", "Search Result File Path");
        dialog.setText(serverFilePath);


        dialog.setAllowEmptyText(false);
        dialog.centerToFrame(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);

        String path = dialog.getText();
        if ((dialog.getButtonClicked() == OptionDialog.BUTTON_OK) && (!path.isEmpty())) {
            preferences.put("ServerIdentificationFilePath", dialog.getText());
        }

    }
}
