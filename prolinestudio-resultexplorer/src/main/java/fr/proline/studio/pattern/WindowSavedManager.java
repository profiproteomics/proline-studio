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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class WindowSavedManager {
    
    //private static boolean m_newWindowAdded = true;
    
    public final static char SAVE_WINDOW_FOR_RSET = '1';
    public final static char SAVE_WINDOW_FOR_RSM = '2';
    public final static char SAVE_WINDOW_FOR_QUANTI = '3';
    
    public static String writeBoxes(String windowName, ArrayList<AbstractDataBox> boxList, ArrayList<SplittedPanelContainer.PanelLayout> layoutList) {
        
        boolean rset = false;
        boolean rsm = false;
        boolean quanti = false;
        
        AbstractDataBox entryBox = boxList.get(0);
        ParameterList inParameter = entryBox.getInParameters();

        ArrayList<DataParameter> parametersList = inParameter.getParameterList();
        for (int i = 0; i < parametersList.size(); i++) {
            DataParameter parameter = parametersList.get(i);
            if (parameter.equalsData(ResultSet.class, ParameterSubtypeEnum.SINGLE_DATA)) {
                rset = true;
            } else if (parameter.equalsData(ResultSummary.class, ParameterSubtypeEnum.SINGLE_DATA)) {
                rsm = true;
            } else if (parameter.equalsData(DDataset.class, ParameterSubtypeEnum.SINGLE_DATA)) {
                quanti = true;
            }
        }

        
        StringBuilder sb = new StringBuilder();
        
        // save rset/rsm entry parameter
        if (rset) {
            sb.append(SAVE_WINDOW_FOR_RSET);
            sb.append("#");
        } else if (rsm) {
            sb.append(SAVE_WINDOW_FOR_RSM);
            sb.append("#");
        }  else if (quanti) {
            sb.append(SAVE_WINDOW_FOR_QUANTI);
            sb.append("#");
        } else {
            sb.append("0#");
        }
        
        // save window name
        sb.append(windowName);
        sb.append('#');
        
        
        for (int i=0;i<boxList.size();i++) {
            sb.append(boxList.get(i).getType().intValue());
            sb.append('#');
            sb.append(layoutList.get(i).intValue());
            if (i<boxList.size()-1) {
                sb.append('#');
            }
        }
        return sb.toString();
    }
    
    public static boolean hasResultSetParameter(String dump) {
        return dump.charAt(0)  == SAVE_WINDOW_FOR_RSET;
    }
    
    public static boolean hasResultSummaryParameter(String dump) {
        return dump.charAt(0)  == SAVE_WINDOW_FOR_RSM;
    }
    
    public static boolean hasQuantiParameter(String dump) {
        return dump.charAt(0)  == SAVE_WINDOW_FOR_QUANTI;
    }
    
    public static char getWindowType(String dump){
        return dump.charAt(0) ;
    }
    
    public static AbstractDataBox[] readBoxes(String dump) {
        String[] values = dump.split("\\#");
        
        int nbBoxes = (values.length-2)/2;
        AbstractDataBox[] boxes = new AbstractDataBox[nbBoxes];
        int boxId = 0;

        for(int i=2;i<values.length;i+=2) {
            AbstractDataBox.DataboxType databoxType = AbstractDataBox.DataboxType.getDataboxType(Integer.parseInt(values[i]));
            SplittedPanelContainer.PanelLayout layout = SplittedPanelContainer.PanelLayout.getLayoutType(Integer.parseInt(values[i+1]));
            
            AbstractDataBox databox = databoxType.getDatabox();
            databox.setLayout(layout);
            
            boxes[boxId++] = databox;
        }
        
        return boxes;
    }
    
    public static String getWindowName(String dump) {
        int endOfName = dump.indexOf('#', 2);
        return dump.substring(2, endOfName);
    }
    
    
    private static final String WINDOW_SAVED_KEY = "WindowSaved";
    private static ArrayList<String> m_windowSavedList = null;
    public static ArrayList<String> readSavedWindows() {

        if (m_windowSavedList!= null) {
            return m_windowSavedList;
        }
        
        Preferences preferences = NbPreferences.root();

        m_windowSavedList = new ArrayList();
        int i = 1;
        while (true) {
            String windowSaved = preferences.get(WINDOW_SAVED_KEY + i, null);
            if (windowSaved == null) {
                break;
            }
            m_windowSavedList.add(windowSaved);
            i++;
        }


        return m_windowSavedList;
    }
    
    public static  void setSavedWindows(ArrayList<String> windowSavedList) {
        m_windowSavedList = windowSavedList;
        //m_newWindowAdded = true;
        writeSavedWindows();
        
        IdentificationTree.reinitMainPopup();
    }
    
    public static void addSavedWindow(String savedWindow) {
        
        //m_newWindowAdded = true;
        
        readSavedWindows();
        
        m_windowSavedList.add(savedWindow);
        
        writeSavedWindows();
        
        IdentificationTree.reinitMainPopup();
        QuantitationTree.reinitMainPopup();
    }
    
    /*public static void setNoWindowAdded() {
        m_newWindowAdded = false;
    }
    
    public static boolean isWindowAdded() {
        return m_newWindowAdded;
    }*/
    
    public static void writeSavedWindows() {

        Preferences preferences = NbPreferences.root();


        // remove previously saved windows
        int i = 1;
        while (true) {
            String key = WINDOW_SAVED_KEY + i;

            String windowSaved = preferences.get(key, null);
            if (windowSaved == null) {
                break;
            }
            preferences.remove(key);
            i++;
        }

        // put new file path
        for (i = 0; i < m_windowSavedList.size(); i++) {
            String key = WINDOW_SAVED_KEY + (i + 1);
            preferences.put(key, m_windowSavedList.get(i));
        }
    }

}
