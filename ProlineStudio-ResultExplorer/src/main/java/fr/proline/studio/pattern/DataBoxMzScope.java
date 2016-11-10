package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.rsmexplorer.gui.MzDBFilesPanel;
import fr.proline.studio.rsmexplorer.gui.StudioMzScopePanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

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
                    propagateDataChanged(CompareDataInterface.class); 
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
                File file = new File(m_mzdbDir+File.separator+f);
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
    
}
