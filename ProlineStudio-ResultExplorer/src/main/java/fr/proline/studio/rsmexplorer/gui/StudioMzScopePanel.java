package fr.proline.studio.rsmexplorer.gui;

import fr.profi.mzscope.MzScope;
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
import fr.profi.mzscope.ui.event.ExtractFeatureListener;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.mzscope.MzdbInfo;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.openide.windows.WindowManager;

/**
 * panel which contains mzScope 
 * @author MB243701
 */
public class StudioMzScopePanel extends HourglassPanel implements DataBoxPanelInterface, ExtractFeatureListener {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_mzscopeScrollPane;
    
    private MzScope m_mzScope;
    private JPanel m_mzScopePanel;
    
    
    private List<File> m_mzdbFiles = null;
    
    public StudioMzScopePanel() {
        m_mzdbFiles = new ArrayList();
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
    
    
    public void setData(Long taskId, List<File> mzdbFile, List<MzdbInfo> mzdbList, boolean finished) {
        int i=0;
        for (File file : mzdbFile) {
            MzdbInfo info = mzdbList.get(i);
            if (!this.m_mzdbFiles.contains(file)){
                this.m_mzdbFiles.add(file);
                m_mzScope.openRawAndExtract(file, info.getMoz(), info.getElutionTime(), info.getFirstScan(), info.getLastScan());
            }else{
                m_mzScope.extractRawFile(file, info.getMoz(), info.getElutionTime(), info.getFirstScan(), info.getLastScan());  
            }
            i++;
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
    public void extractFeatureListener(boolean extractFeatures, boolean detectPeakels) {
        //
    }
    
}
