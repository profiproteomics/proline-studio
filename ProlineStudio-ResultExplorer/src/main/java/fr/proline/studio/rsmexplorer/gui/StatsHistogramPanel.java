package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotHistogram;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.stats.ValuesForStatsAbstract;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;



/**
 *
 * @author JM235353
 */
public class StatsHistogramPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private PlotPanel m_plotPanel;
    private JPanel m_statPanel;
    
    
    private JComboBox<String> m_valueComboBox;
    private PlotHistogram m_histogram;
    private ValuesForStatsAbstract m_values = null;
    
    public StatsHistogramPanel() {
        setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);

    }
    
    public final JPanel createInternalPanel() {
        
        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);

        m_statPanel = createStatPanel();
        JPanel selectPanel = createSelectPanel();
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_statPanel, c);
        
        c.gridy++;
        c.weighty = 0;
        internalPanel.add(selectPanel, c);

        return internalPanel;
    }
 
    public final JToolBar initToolbar() {
            
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        ExportButton exportImageButton = new ExportButton("Histogram", m_statPanel);

        
        toolbar.add(exportImageButton);

        return toolbar;

    }
    
    
    private JPanel createStatPanel() {
        
        m_plotPanel = new PlotPanel();
        return m_plotPanel;
    }
    
    private JPanel createSelectPanel() {
        JPanel selectPanel = new JPanel();
         selectPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel selectLabel = new JLabel("Histogram on :");
        
        c.gridx = 0;
        c.gridy = 0;
        selectPanel.add(selectLabel, c);
        
        m_valueComboBox = new JComboBox();
        c.gridx++;
        selectPanel.add(m_valueComboBox, c);
        
        c.gridx++;
        c.weightx = 1;
        selectPanel.add(Box.createHorizontalGlue(), c);
        
        return selectPanel;
        
    }
    
    
    public void setData(ValuesForStatsAbstract values) {

        m_values = values;
        
        if (values == null) {
            return;
        }
        
        
        if (m_valueComboBox.getItemCount() == 0) {
            String[] valuesType = values.getAvailableValueTypes();
            for (int i=0;i<valuesType.length;i++) {
                m_valueComboBox.addItem(valuesType[i]);
            }
            m_valueComboBox.setSelectedIndex(0);
            
            m_valueComboBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_values.setValueType((String) m_valueComboBox.getSelectedItem());
                    m_histogram.update(m_values);
                }
                
            });
            
        }
        
        m_histogram = new PlotHistogram(m_plotPanel, values);
        
        m_plotPanel.addPlot(m_histogram);
        
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

    
}
