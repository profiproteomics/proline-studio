package fr.proline.studio.rsmexplorer.gui;

import fr.proline.mzscope.MzScope;
import fr.proline.mzscope.ui.event.ExtractionEvent;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import fr.proline.mzscope.ui.event.ExtractionStateListener;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.mzscope.MzdbInfo;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * panel which contains mzScope 
 * @author MB243701
 */
public class StudioMzScopePanel extends HourglassPanel implements DataBoxPanelInterface, ExtractionStateListener {

   protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
   
   private AbstractDataBox m_dataBox;
   private JScrollPane m_mzscopeScrollPane;
    
   private MzScope m_mzScope;
   private JPanel m_mzScopePanel;
    
    
   public StudioMzScopePanel() {
        initComponents();
   }
    
    private void initComponents() {
        setLayout(new BorderLayout());

       final JPanel mzscopePanel = createMzScopePanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                mzscopePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(mzscopePanel, JLayeredPane.DEFAULT_LAYER);
    }
    
    private JPanel createMzScopePanel() {

        JPanel mzScopePanel = new JPanel();
        mzScopePanel.setBounds(0, 0, 500, 400);
        mzScopePanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        mzScopePanel.add(toolbar, BorderLayout.WEST);
        mzScopePanel.add(internalPanel, BorderLayout.CENTER);

        return mzScopePanel;
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        m_mzScope = new MzScope();
        m_mzScopePanel = m_mzScope.createMzScopePanel(WindowManager.getDefault().getMainWindow());
        // create objects
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_mzScopePanel, c);
        

        
        return internalPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        return toolbar;
    }
    
    
    public void setData(Long taskId, List<MzdbInfo> mzdbList, boolean finished) {
        for (MzdbInfo info : mzdbList) {
            if (info.isMultiFile()){
                List<File> files = info.getMzdbFiles();
                int action = info.getAction();
                switch(action){
                    case MzdbInfo.MZSCOPE_VIEW :{
                        m_mzScope.openRaw(files);
                        break;
                    }
                    case MzdbInfo.MZSCOPE_DETECT_PEAKEL:{
                        m_mzScope.detectPeakels(files);
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }else{ // single file
                File file = info.getMzdbFile();
                int action = info.getAction();
                switch(action){
                    case MzdbInfo.MZSCOPE_VIEW :{
                        m_mzScope.openRaw(file);
                        break;
                    }
                    case MzdbInfo.MZSCOPE_DETECT_PEAKEL:{
                        m_mzScope.detectPeakels(file);
                        break;
                    }
                    case MzdbInfo.MZSCOPE_EXTRACT:{
                        m_mzScope.openRawAndExtract(file, info.getMoz(), info.getElutionTime(), info.getFirstScan(), info.getLastScan());
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        
    }
    
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    @Override
    public void extractionStateChanged(ExtractionEvent event) {
        //
    }
    
}
