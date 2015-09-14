package fr.proline.studio.rsmexplorer.gui;

import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.ui.RawFileManager;
import fr.proline.mzscope.ui.RawFilesPanel;
import fr.proline.mzscope.utils.IPopupMenuDelegate;
import fr.proline.studio.mzscope.MzdbInfo;
import static fr.proline.studio.pattern.DataBoxMzScope.MZDB_DIRECTORY_KEY;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author MB243701
 */
public class MzDBFilesPanel extends JPanel implements IPopupMenuDelegate {
    
    private static MzDBFilesPanel m_singleton = null;
    
    private JPanel m_mzdbPathPanel;
    private JTextField m_mzdbPathField;
    private JButton m_mzdbPathBtn;
    private JFileChooser m_fchooser;
    private Preferences m_preferences;
    private String m_mzdbDir;
    
    private final static String LAST_DIR = "Last directory";
    
    private RawFilesPanel m_rawFilesPanel;
    private Map<IRawFile, File> mapFiles; 
    
    
    public static MzDBFilesPanel getMzdbFilesPanel() {
        if (m_singleton == null) {
            m_singleton = new MzDBFilesPanel();
        }
        return m_singleton;
    }
   private JMenuItem detectPeakelsMI;
   private JMenuItem viewRawFileMI;
   private ActionListener viewRawFileAction;
    
    public MzDBFilesPanel(){
        super();
        setLayout(new BorderLayout());
        init();
    }
    
    private void init(){
        mapFiles = new HashMap<IRawFile, File>();
        m_preferences = NbPreferences.root();
        m_mzdbDir = m_preferences.get(MZDB_DIRECTORY_KEY, null);
        File dir = new File(".");
        if (m_mzdbDir != null){
            dir = new File(m_mzdbDir);
        }
        m_fchooser = new JFileChooser(); 
        m_fchooser.setCurrentDirectory(dir);
        m_fchooser.setDialogTitle("Mzdb local directory");
        m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // disable the "All files" option.
        m_fchooser.setAcceptAllFileFilterUsed(false);
        
        this.add(getMzdbPathPanel(), BorderLayout.NORTH);
        this.add(getRawFilesPanel(), BorderLayout.CENTER);
        
        setRawFiles();
    }
    
    private JPanel getMzdbPathPanel(){
        if (m_mzdbPathPanel == null){
            m_mzdbPathPanel = new JPanel();
            m_mzdbPathPanel.setName("m_mzdbPathPanel");
            m_mzdbPathPanel.setLayout(new GridBagLayout());
            m_mzdbPathPanel.setBorder(BorderFactory.createTitledBorder("MzDB Files Location"));
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(5, 5, 5, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.weightx = 2;
            m_mzdbPathPanel.add(getFieldMzdbPath(), c);
            c.gridx+=2;
            c.gridwidth = 1;
            c.weightx = 1;
            m_mzdbPathPanel.add(getBtnMzdbPath(), c);
        }
        return m_mzdbPathPanel;
    }
    
    
    private JTextField getFieldMzdbPath(){
        if (m_mzdbPathField == null){
            m_mzdbPathField = new JTextField(30);
            m_mzdbPathField.setText(m_mzdbDir);
            m_mzdbPathField.setEditable(false);
        }
        return m_mzdbPathField;
    }
    
    private JButton getBtnMzdbPath(){
        if (m_mzdbPathBtn == null){
            m_mzdbPathBtn = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
            m_mzdbPathBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int result = m_fchooser.showOpenDialog(m_mzdbPathBtn);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        updateMzdbDir(m_fchooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
        }
        return m_mzdbPathBtn;
    }
    
    
    private RawFilesPanel getRawFilesPanel(){
        if (m_rawFilesPanel == null){
            m_rawFilesPanel = new RawFilesPanel(this);
            m_rawFilesPanel.setName("m_rawFilesPanel");
        }
        return m_rawFilesPanel;
    }
    
    
    public void updateMzdbDir(String directory){
        m_mzdbDir = directory;
        m_preferences.put(MZDB_DIRECTORY_KEY, m_mzdbDir);
        m_mzdbPathField.setText(directory);
        setRawFiles();
    }
    
    private void setRawFiles(){
        m_rawFilesPanel.removeAllFiles();
        RawFileManager.getInstance().removeAllFiles();
        mapFiles = new HashMap<IRawFile, File>();
        if (m_mzdbDir != null){
            File mzdbDir = new File(m_mzdbDir);
            File[] list = mzdbDir.listFiles();
            if (list != null){
                for ( File f : list ) {
                    if (isMzdbFile(f)){
                        IRawFile rawfile = RawFileManager.getInstance().addRawFile(f);
                        m_rawFilesPanel.addFile(rawfile);
                        mapFiles.put(rawfile, f);
                    }
                }
            }
        }
    }
    
    private boolean isMzdbFile(File f){
        return f.getName().toLowerCase().endsWith(".mzdb");
    }
    
    public void displayRaw(IRawFile rawfile) {
        File file = mapFiles.get(rawfile);
        if (file != null){
            MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_VIEW, file);
            MzScopeWindowBoxManager.addMzdbScope(mzScope);
        }
    }

    public void displayRaw(List<IRawFile> rawfiles) {
        List<File> files = new ArrayList();
        for(IRawFile rawFile: rawfiles){
            File file = mapFiles.get(rawFile);
            files.add(file);
        }
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_VIEW,files);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
    }

    public void detectPeakels(List<IRawFile> rawfiles) {
        List<File> files = new ArrayList();
        for(IRawFile rawFile: rawfiles){
            File file = mapFiles.get(rawFile);
            files.add(file);
        }
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_DETECT_PEAKEL, files);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
        
    }

       @Override
   public void initPopupMenu(JPopupMenu popupMenu) {
      // view data
      viewRawFileAction = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            displayRaw(getRawFilesPanel().getSelectedValues());
         }
      };
      viewRawFileMI = new JMenuItem();
      viewRawFileMI.setText("View");
      viewRawFileMI.addActionListener(viewRawFileAction);
      popupMenu.add(viewRawFileMI);

      // detect peakels
      detectPeakelsMI = new JMenuItem();
      detectPeakelsMI.setText("Detect Peakels...");
      detectPeakelsMI.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            detectPeakels(getRawFilesPanel().getSelectedValues());
         }
      });
      popupMenu.add(detectPeakelsMI);

   }

   @Override
   public void updatePopupMenu() {
      int nbS = getRawFilesPanel().getSelectedValues().size();
      viewRawFileMI.setEnabled(nbS > 0);
      detectPeakelsMI.setEnabled((nbS > 0));
   }

   @Override
   public ActionListener getDefaultAction() {
      return viewRawFileAction;
   }
   
   

}
