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
package fr.proline.studio.rsmexplorer.gui.calc.macros;


import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Management of all user macros for DataAnalyzer
 * @author JM235353
 */
public class MacroSavedManager {

    
    private static final String MACRO_SAVED_KEY = "MacroSaved";
    private static ArrayList<String> m_macroSavedList = null;
    public static ArrayList<String> readSavedMacros() {

        if (m_macroSavedList!= null) {
            return m_macroSavedList;
        }
        
        Preferences preferences = NbPreferences.root();

        m_macroSavedList = new ArrayList();
        int i = 1;
        while (true) {
            String macroSaved = preferences.get(MACRO_SAVED_KEY + i, null);
            if (macroSaved == null) {
                break;
            }
            m_macroSavedList.add(macroSaved);
            i++;
        }


        return m_macroSavedList;
    }

    
    public static void addSavedMacro(String savedMacro) {

        
        readSavedMacros();
        
        m_macroSavedList.add(savedMacro);
        
        writeSavedMacros();

    }
    
    public static void removeSavedMacro(String name) {
        for (String macro : m_macroSavedList) {
            if (macro.indexOf("\""+name+"\"") != -1) {
                m_macroSavedList.remove(macro);
                break;
            }
        }
        writeSavedMacros();
    }

    
    public static void writeSavedMacros() {

        Preferences preferences = NbPreferences.root();


        // remove previously saved windows
        int i = 1;
        while (true) {
            String key = MACRO_SAVED_KEY + i;

            String macroSaved = preferences.get(key, null);
            if (macroSaved == null) {
                break;
            }
            preferences.remove(key);
            i++;
        }

        // put new file path
        for (i = 0; i < m_macroSavedList.size(); i++) {
            String key = MACRO_SAVED_KEY + (i + 1);
            preferences.put(key, m_macroSavedList.get(i));
        }
    }

}
