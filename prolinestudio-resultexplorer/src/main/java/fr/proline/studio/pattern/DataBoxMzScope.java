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

import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.rsmexplorer.gui.StudioMzScopePanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.msfiles.WorkingSetView;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 * databox which contains mzscope
 * @author MB243701
 */
public class DataBoxMzScope extends AbstractDataBox{
    
    public final static String MZDB_DIRECTORY_KEY = "mzdbDirectory";

    private MzScopeInterface mzScope = null;
    
    private String m_mzdbDir;
    
     public DataBoxMzScope() {
        super(DataboxType.DataBoxMzScope, DataboxStyle.STYLE_UNKNOWN);

        // Name of this databox
        m_typeName = "MzScope";
        m_description = "MzScope";

        // Register Possible in parameters
        // One Map 
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(File.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        //outParameter.addParameter(?.class, false);
        registerOutParameter(outParameter);
        
         // mzdb directory       
        
        /*
        Preferences preferences = NbPreferences.root();
        m_mzdbDir = preferences.get(MZDB_DIRECTORY_KEY, null);
        if (m_mzdbDir == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "No mzdb directory is specified. Please, select one!");
            JFileChooser chooser = new JFileChooser(); 
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Mzdb local directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            // disable the "All files" option.
            chooser.setAcceptAllFileFilterUsed(false);
            //    
            if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) { 
                m_mzdbDir = chooser.getSelectedFile().getAbsolutePath();
                preferences.put(MZDB_DIRECTORY_KEY, m_mzdbDir);
                MzDBFilesPanel.getMzdbFilesPanel().updateMzdbDir(m_mzdbDir);
            } else {
               m_mzdbDir = ".";
            }
        }
        */
        

    }
     
    @Override
    public void createPanel() {
        StudioMzScopePanel p = new StudioMzScopePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        mzScope = (MzScopeInterface) data;

        dataChanged();
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
//            if (parameterType.equals(?.class)) {
//                return ?;
//            }
        }
        return super.getData(getArray, parameterType);
    }
    
    @Override
    public String getFullName() {
        return m_typeName;
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                   //((StudioMzScopePanel) getDataBoxPanelInterface()).setData(taskId, file, finished);
                } else {
                    ((StudioMzScopePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class); 
                }
            }
        };

        // ask asynchronous loading of data
        
        //registerTask(task)
       List<MzdbInfo> mzdbInfos =  mzScope.getMzdbInfo();
       List<MzdbInfo> infos = new ArrayList();
        for (MzdbInfo mzdbInfo : mzdbInfos) {
            String f = mzdbInfo.getFileName();
            if ( f!= null){
                File file = findFile(f);
                if (file.exists()){
                    mzdbInfo.setFile(file);
                    infos.add(mzdbInfo);
                }
            }else{
                infos.add(mzdbInfo);
            }
        }
        ((StudioMzScopePanel) getDataBoxPanelInterface()).setData((long)-1,  infos, true);
        setLoaded(loadingId);
    }
    
    private File findFile(String f) {
        
        Map<String,JSONObject> map = WorkingSetView.getWorkingSetView().getModel().getEntiesObjects();
        for (Map.Entry<String,JSONObject> entry: map.entrySet()) {
            String filename = (String)entry.getValue().get("filename");
            String location = (String) entry.getValue().get("location");

            if (filename.equalsIgnoreCase(f) && location.equalsIgnoreCase("LOCAL")) {
                String path = (String)entry.getValue().get("path");
                return new File(path);
            }
        }
        
        return new File(m_mzdbDir+File.separator+f);
        
    }
    
}
