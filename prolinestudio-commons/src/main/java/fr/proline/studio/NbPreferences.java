package fr.proline.studio;

import java.util.prefs.Preferences;

public class NbPreferences {

    public static Preferences root() {
        return Preferences.userRoot(); //JPM.DOCK
    }
}
