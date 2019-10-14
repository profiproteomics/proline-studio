/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbPreferences;

/**
 * Utils methods to read/write settings
 * @author JM235353
 */
public class SettingsUtils {

    private static final String SETTINGS_DIRECTORY_POSTFIX = "SettingsDirectory";
    private static final String FILE_PATH_POSTFIX = ".filePath_";
    
    public static String readDefaultDirectory(String settingsKey) {
        Preferences preferences = NbPreferences.root();
        return preferences.get(settingsKey + SETTINGS_DIRECTORY_POSTFIX, null);
    }
    public static void writeDefaultDirectory(String settingsKey, String directoryPath) {
        Preferences preferences = NbPreferences.root();
        preferences.put(settingsKey + SETTINGS_DIRECTORY_POSTFIX, directoryPath);
    }
    
    public static void addSettingsPath(String settingsKey, String filePath) {
        ArrayList<String> filePathList = readSettingsPaths(settingsKey);
        boolean pathFound = false;
        for (int i = 0; i < filePathList.size(); i++) {
            String pathCur = filePathList.get(i);
            if (pathCur.compareTo(filePath) == 0) {
                pathFound = true;
                break;
            }
        }
        if (!pathFound) {
            filePathList.add(0, filePath);
        }
        writeSettingsPath(settingsKey, filePathList);
    }

    public static ArrayList<String> readSettingsPaths(String settingsKey) {

        Preferences preferences = NbPreferences.root();

        String settingsDirectoryKey = settingsKey + FILE_PATH_POSTFIX;
        ArrayList<String> filePathList = new ArrayList();
        int i = 1;
        while (i < 10) {
            String filePath = preferences.get(settingsDirectoryKey + i, null);
            if (filePath == null) {
                break;
            }
            filePathList.add(filePath);
            i++;
        }


        return filePathList;
    }

    public static void writeSettingsPath(String settingsKey, ArrayList<String> filePathList) {

        Preferences preferences = NbPreferences.root();

        String settingsDirectoryKey = settingsKey + FILE_PATH_POSTFIX;

        // remove previous file path
        int i = 1;
        while (i < 10) {
            String key = settingsDirectoryKey + i;

            String regex = preferences.get(key, null);
            if (regex == null) {
                break;
            }
            preferences.remove(key);
            i++;
        }

        // put new file path
        for (i = 0; i < filePathList.size(); i++) {
            String key = settingsDirectoryKey + (i + 1);
            preferences.put(key, filePathList.get(i));
        }
    }
    
    public static JFileChooser getFileChooser(String settingsKey) {
        
        return new SettingsFileChooser(settingsKey);
    }
    
    
    private static class SettingsFileChooser extends JFileChooser {
        
        public SettingsFileChooser(String settingsKey) {
            super(readDefaultDirectory(settingsKey));
            setMultiSelectionEnabled(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Proline Settings File", "settings");
            addChoosableFileFilter(filter);
            setFileFilter(filter);
        }
        
        @Override
        public File getSelectedFile() {
            File f = super.getSelectedFile();
            if (f!=null) {
                if (!f.getAbsolutePath().endsWith(".settings")) {
                    return new File(f.getAbsolutePath()+".settings");
                }
            }
            return f;
        }
        
    }
}
