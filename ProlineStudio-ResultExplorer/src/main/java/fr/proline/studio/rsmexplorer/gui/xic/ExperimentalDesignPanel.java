package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DefineQuantParamsPanel;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * experimental design panel and quanti params
 * @author MB243701
 */
public class ExperimentalDesignPanel extends HourglassPanel implements DataBoxPanelInterface {
    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_scrollPaneExpDesign;
    private JPanel m_expDesignPanel;
    private XICDesignTree m_expDesignTree;
    private ExportButton m_exportButton;
    private DefineQuantParamsPanel m_xicParamPanel;
    private JSplitPane m_splitPane;
    private JPanel m_confPanel;
    
    private DDataset m_dataset;

    public ExperimentalDesignPanel() {
        super();
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        JPanel expDesignPanel = createExperimentalDesignPanel();
        this.add(expDesignPanel, BorderLayout.CENTER);
    }
    
    private JPanel createExperimentalDesignPanel(){
        JPanel expDesignPanel = new JPanel();
        expDesignPanel.setBounds(0, 0, 500, 400);
        expDesignPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        expDesignPanel.add(toolbar, BorderLayout.WEST);
        expDesignPanel.add(internalPanel, BorderLayout.CENTER);
        return expDesignPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        m_exportButton = new ExportButton("Exp. Design", m_expDesignPanel);
        toolbar.add(m_exportButton);
        return toolbar;
    }
    
    private JPanel createInternalPanel(){
        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        m_splitPane = new JSplitPane();
        m_splitPane.setDividerLocation(350);
        m_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        m_splitPane.setOneTouchExpandable(true);
        
        // create objects
        m_scrollPaneExpDesign = new JScrollPane();
        m_expDesignPanel = new JPanel();
        m_expDesignPanel.setLayout(new BorderLayout());
        m_expDesignTree =  XICDesignTree.getDesignTree(QuantitationTree.getCurrentTree().copyCurrentNodeForSelection(), false);
        m_expDesignPanel.add(m_expDesignTree, BorderLayout.CENTER);
        m_scrollPaneExpDesign.setViewportView(m_expDesignPanel);
        
        m_confPanel = new JPanel();
        m_confPanel.setLayout(new BorderLayout());
        
        
        m_splitPane.setRightComponent(m_confPanel);
        m_splitPane.setLeftComponent(m_scrollPaneExpDesign);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_splitPane, c);
        //internalPanel.add(m_scrollPaneExpDesign, c);
        return internalPanel;
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
    
    public void setData(Long taskId, DDataset dataset, boolean finished) {
        m_dataset = dataset;
        updateData();
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        updateData();
    }
    
    private void updateData(){
        m_expDesignTree.setExpDesign(m_dataset);
        try {
            if (m_dataset.getQuantProcessingConfig() != null){
                m_confPanel.removeAll();
                m_xicParamPanel = new DefineQuantParamsPanel(true);
                m_xicParamPanel.resetScrollbar();
                m_confPanel.add(m_xicParamPanel, BorderLayout.CENTER);
                m_xicParamPanel.setQuantParams(m_dataset.getQuantProcessingConfigAsMap());
            }else{
                m_confPanel.removeAll();
                m_confPanel.add(new JLabel("no configuration available"), BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            m_logger.error("error while settings quanti params "+ex);
        }
        m_splitPane.revalidate();
    }


}
