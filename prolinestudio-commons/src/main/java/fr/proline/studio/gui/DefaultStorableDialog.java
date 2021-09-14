package fr.proline.studio.gui;

import fr.proline.studio.Exceptions;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

public abstract class DefaultStorableDialog extends  DefaultDialog {

  public DefaultStorableDialog() {
    super();
    init();
  }

  public DefaultStorableDialog(Window parent) {
    super(parent);
    init();
  }

  public DefaultStorableDialog(Window parent, ModalityType modalityType) {
    super(parent, modalityType);
    init();
  }

  public DefaultStorableDialog(Dialog owner) {
    super(owner);
    init();
  }

  private void init(){
    setButtonVisible(BUTTON_LOAD, true);
    setButtonVisible(BUTTON_SAVE, true);
  }


  abstract protected String getSettingsKey();
  abstract protected void saveParameters(Preferences preferences) throws Exception;
  abstract protected boolean checkParameters();
  abstract protected void resetParameters() throws Exception;
  abstract protected void loadParameters(Preferences preferences) throws Exception;

  @Override
  protected boolean saveCalled() {
    // check parameters
    if (!checkParameters()) {
      return false;
    }

    JFileChooser fileChooser = SettingsUtils.getFileChooser(getSettingsKey());
    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      try {
        File f = fileChooser.getSelectedFile();
        if(f.exists()){
          FileOutputStream fos = new FileOutputStream(f);
          fos.close();
        }
        FilePreferences filePreferences =  new FilePreferences(f, null, "");

        saveParameters(filePreferences);

        SettingsUtils.addSettingsPath(getSettingsKey(), f.getAbsolutePath());
        SettingsUtils.writeDefaultDirectory(getSettingsKey(), f.getParent());
      } catch (Exception e) {
        Exceptions.printStackTrace(e);
        JOptionPane.showMessageDialog(this, "Error saving settings "+e.getMessage(), "Save Settings Error",JOptionPane.ERROR_MESSAGE);
      }

    }

    return false;
  }



  @Override
  protected boolean loadCalled() {

    SettingsDialog settingsDialog = new SettingsDialog(this, getSettingsKey());
    settingsDialog.setLocationRelativeTo(this);
    settingsDialog.setVisible(true);

    if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
      if (settingsDialog.isDefaultSettingsSelected()) {
        try {
          resetParameters();
        } catch (Exception e) {
          LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Reset Settings Failed", e);
          setStatus(true, "Reset Settings Failed");
        }
      } else {
        try {
          File settingsFile = settingsDialog.getSelectedFile();
          FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");
          loadParameters(filePreferences);
        } catch (Exception e) {
          LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
          setStatus(true, "Parsing of your Settings File failed");
        }
      }
    }
    return false;
  }

}
