package fr.proline.studio;

import fr.proline.studio.settings.FilePreferences;

import java.io.File;
import java.util.prefs.Preferences;

public class NbPreferences {

    private static FilePreferences m_preferences;

    public static Preferences root() {

        if (m_preferences == null) {
            initPreferences(null);
        }

        return m_preferences;
    }

    public static void initPreferences(String path) {

        if (path == null) {
            path = "./Preferences.properties";  // by default Preferences.properties is saved in the local directory of the application
        } else {
            path = path+"/Preferences.properties";
        }

        m_preferences = new FilePreferences(new File(path), null, "");
    }

}
