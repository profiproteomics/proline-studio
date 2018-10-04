package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractGenericQuantParamsPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DefineQuantParamsCompletePanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DefineQuantParamsCompletePanelV2;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.QuantProfileXICPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
    private JTabbedPane m_tabbedPane;
    private QuantProfileXICPanel m_profilizerParamPanel;
    private JPanel m_confPanel;
    
    private JPanel m_refinedPanel;
    
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
        
        m_tabbedPane = new JTabbedPane();
        
        // create objects
        m_scrollPaneExpDesign = new JScrollPane();
        m_expDesignPanel = new JPanel();
        m_expDesignPanel.setLayout(new BorderLayout());
        m_expDesignTree =  new XICDesignTree(QuantitationTree.getCurrentTree().copyCurrentNodeForSelection(), false);
        m_expDesignPanel.add(m_expDesignTree, BorderLayout.CENTER);
        m_scrollPaneExpDesign.setViewportView(m_expDesignPanel);
        
        m_confPanel = new JPanel();
        m_confPanel.setLayout(new BorderLayout());
        
        m_refinedPanel = new JPanel();
        m_refinedPanel.setLayout(new BorderLayout());
        
        m_tabbedPane.add("Exp.Design", m_scrollPaneExpDesign);
        m_tabbedPane.add("Exp. Parameters", m_confPanel );
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_tabbedPane, c);
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
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
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
        XICDesignTree.displayExperimentalDesign(m_dataset, (AbstractNode) m_expDesignTree.getModel().getRoot(), m_expDesignTree, true, true, false);
        
        // expand all
        for (int i = 0; i < m_expDesignTree.getRowCount(); i++) {
            m_expDesignTree.expandRow(i);
        }
        
        try {
            if (m_dataset.getQuantProcessingConfig() != null){
                Map<String,Object> quantConfig = m_dataset.getQuantProcessingConfigAsMap();
                AbstractGenericQuantParamsPanel xicParamPanel = null;
                if(quantConfig.containsKey("config_version") && quantConfig.get("config_version").equals("2.0")) {
                    xicParamPanel =  new DefineQuantParamsCompletePanelV2(true, false);
                } else {
                    xicParamPanel = new DefineQuantParamsCompletePanel(true, false);
                }
                    
                m_confPanel.removeAll();
                xicParamPanel.resetScrollbar();
                m_confPanel.add(xicParamPanel, BorderLayout.CENTER);
                xicParamPanel.setQuantParams(m_dataset.getQuantProcessingConfigAsMap());
            }else{
                m_confPanel.removeAll();
                m_confPanel.add(new JLabel("no configuration available"), BorderLayout.CENTER);
            }
            
            if(m_dataset.getPostQuantProcessingConfig() != null){
                m_refinedPanel.removeAll();
                if (m_tabbedPane.getTabCount() == 2){
                    m_tabbedPane.add("Refined Parameters", m_refinedPanel );
                }
                m_profilizerParamPanel = new QuantProfileXICPanel(true);
                m_refinedPanel.add(m_profilizerParamPanel, BorderLayout.CENTER);
                m_profilizerParamPanel.setRefinedParams(m_dataset.getPostQuantProcessingConfigAsMap());
                
            }else{
                if (m_tabbedPane.getTabCount() == 3){
                    m_tabbedPane.removeTabAt(2);
                }
                m_refinedPanel.removeAll();
                m_refinedPanel.add(new JLabel("no configuration available"), BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            m_logger.error("error while settings quanti params "+ex);
        }
        m_tabbedPane.revalidate();
    }


}
