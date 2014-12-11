package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.LockedDataModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotAbstract;
import fr.proline.studio.graphics.PlotHistogram;
import fr.proline.studio.graphics.PlotScatter;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanel.GridListener;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public class GraphicsPanel extends HourglassPanel implements DataBoxPanelInterface, GridListener {

    
    private AbstractDataBox m_dataBox;

    private PlotPanel m_plotPanel;
    
    
    private JComboBox<PlotType> m_allPlotsComboBox;
    private JComboBox<String> m_valueXComboBox;
    private JComboBox<String> m_valueYComboBox;
    private JLabel m_valueXLabel;
    private JLabel m_valueYLabel;
    
    private PlotAbstract m_plotGraphics = null;
    
    private CompareDataInterface m_values = null;
    private CrossSelectionInterface m_crossSelectionInterface = null;
    
    private boolean m_isUpdatingCbx = false;
    
    private boolean m_dataLocked = false;
    
    private JToggleButton m_gridButton = null;
    
    public GraphicsPanel() {
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

        m_plotPanel = new PlotPanel();
        m_plotPanel.setGridListener(this);
        JPanel selectPanel = createSelectPanel();
        
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(selectPanel, c);

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_plotPanel, c);

        return internalPanel;
    }
 
    public final JToolBar initToolbar() {
            
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_gridButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.GRID));
        m_gridButton.setSelected(true);
        m_gridButton.setFocusPainted(false);
        m_gridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.displayGrid(m_gridButton.isSelected());
            }
        });
        
        final JButton importSelectionButton  = new JButton(IconManager.getIcon(IconManager.IconType.IMPORT_TABLE_SELECTION));
        importSelectionButton.setToolTipText( "Import Selection from Previous View");
        importSelectionButton.setFocusPainted(false);
        importSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_crossSelectionInterface != null) {
                    ArrayList<Integer> selection = m_crossSelectionInterface.getSelection();
                    m_plotPanel.setSelection(selection);
                }
            }
        });
        final JButton exportSelectionButton  = new JButton(IconManager.getIcon(IconManager.IconType.EXPORT_TABLE_SELECTION));
        exportSelectionButton.setToolTipText("Export Selection to Previous View");
        exportSelectionButton.setFocusPainted(false);
        exportSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_crossSelectionInterface != null) {
                    m_crossSelectionInterface.select(m_plotPanel.getSelection());
                }
            }
        });
        
        
        final JButton lockButton = new JButton(IconManager.getIcon(IconManager.IconType.UNLOCK));
        lockButton.setToolTipText( "Lock/Unlock Input Data");
        lockButton.setFocusPainted(false);
        lockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                m_dataLocked = ! m_dataLocked;
                if (m_dataLocked) {
                    m_values = new LockedDataModel(m_values);
                } else {
                    m_values = ((LockedDataModel) m_values).getSrcDataInterface();
                }
                
                m_plotPanel.lockData(m_dataLocked);
                setDataImpl(m_values, m_crossSelectionInterface);
                
                if (m_dataLocked) {
                    lockButton.setIcon(IconManager.getIcon(IconManager.IconType.LOCK));
                } else {
                    lockButton.setIcon(IconManager.getIcon(IconManager.IconType.UNLOCK));
                }
                importSelectionButton.setEnabled(!m_dataLocked);
                exportSelectionButton.setEnabled(!m_dataLocked);
            }
        });
        

        
        ExportButton exportImageButton = new ExportButton("Graphic", m_plotPanel);
        
        
        // add buttons to toolbar
        toolbar.add(m_gridButton);
        toolbar.addSeparator(); // ----
        toolbar.add(lockButton);
        toolbar.add(importSelectionButton);
        toolbar.add(exportSelectionButton);
        toolbar.addSeparator(); // ----
        toolbar.add(exportImageButton);

        return toolbar;

    }
    

    
    private JPanel createSelectPanel() {
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel selectLabel = new JLabel("Graphic :");
        m_allPlotsComboBox = new JComboBox(PlotType.ALL_PLOTS);
        m_valueXComboBox = new JComboBox();
        m_valueYComboBox = new JComboBox();
        m_valueXLabel = new JLabel();
        m_valueYLabel = new JLabel();
        updateXYCbxVisibility();
        
        m_allPlotsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fillXYCombobox();
                updateXYCbxVisibility();
                
            }
        });
        
        
        c.gridx = 0;
        c.gridy = 0;
        selectPanel.add(selectLabel, c);

        c.gridx++;
        selectPanel.add(m_allPlotsComboBox, c);

        c.gridx++;
        selectPanel.add(m_valueXLabel, c);
        
        c.gridx++;
        selectPanel.add(m_valueXComboBox, c);
        
        c.gridx++;
        selectPanel.add(m_valueYLabel, c);
        
        c.gridx++;
        selectPanel.add(m_valueYComboBox, c);
        
        c.gridx++;
        c.weightx = 1;
        selectPanel.add(Box.createHorizontalGlue(), c);
        
        return selectPanel;
        
    }
    
    private void updateXYCbxVisibility() {
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
        
        m_valueXLabel.setVisible(plotType.needsX());
        m_valueXComboBox.setVisible(plotType.needsX());
        
        m_valueYLabel.setVisible(plotType.needsY());
        m_valueYComboBox.setVisible(plotType.needsY());
        
        if (plotType.needsX()) {
            m_valueXLabel.setText(plotType.getXLabel());
        }
        if (plotType.needsY()) {
            m_valueYLabel.setText(plotType.getYLabel());
        }
        
    }
    
    private void fillXYCombobox() {

        m_isUpdatingCbx = true;
        try {

            // clear combobox
            ((DefaultComboBoxModel) m_valueXComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel) m_valueYComboBox.getModel()).removeAllElements();

            PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
            HashSet<Class> acceptedValues = plotType.getAcceptedXValues();

            int nbValuesType = m_values.getColumnCount();
            
            // find the best column for the current plot
            int bestColX = -1;
            int bestColY = -1;
            if (m_values instanceof BestGraphicsInterface) {
                BestGraphicsInterface bestGraphics = (BestGraphicsInterface) m_values;
                int col = bestGraphics.getBestXAxisColIndex(plotType);
                if (col != -1) {
                    bestColX = col;
                }
                col = bestGraphics.getBestYAxisColIndex(plotType);
                if (col != -1) {
                    bestColY = col;
                }
            }

            // fill the comboboxes and find the index to be selected
            int bestColIndexXCbx = 0;
            int bestColIndexYCbx = (nbValuesType >= 2) ? 1 : 0;
            int nbValuesInCbx = 0;
            for (int i = 0; i < nbValuesType; i++) {
                Class c = m_values.getDataColumnClass(i);
                if (acceptedValues.contains(c)) {
                    ReferenceToColumn ref = new ReferenceToColumn(m_values.getDataColumnIdentifier(i), i);
                    ((DefaultComboBoxModel) m_valueXComboBox.getModel()).addElement(ref);
                    ((DefaultComboBoxModel) m_valueYComboBox.getModel()).addElement(ref);
                    if (bestColX == i) {
                        bestColIndexXCbx = nbValuesInCbx;
                    }
                    if (bestColY == i) {
                        bestColIndexYCbx = nbValuesInCbx;
                    }
                    nbValuesInCbx++;
                    
                }
            }
            
            m_valueXComboBox.setSelectedIndex(bestColIndexXCbx);
            m_valueYComboBox.setSelectedIndex(bestColIndexYCbx);
            
            
            setDataImpl(m_values, m_crossSelectionInterface);


        } finally {
            m_isUpdatingCbx = false;
        }
    }
    
    public void setData(CompareDataInterface values, CrossSelectionInterface crossSelectionInterface) {
        if (m_plotPanel.isLocked()) {
            return;
        }
        setDataImpl(values, crossSelectionInterface);
    }
    private void setDataImpl(CompareDataInterface values, CrossSelectionInterface crossSelectionInterface) {

        m_values = values;
        m_crossSelectionInterface = crossSelectionInterface;
        
        if (values == null) {
            return;
        }
        
        
        if (m_valueXComboBox.getItemCount() == 0) {
            
            fillXYCombobox();
            
            ActionListener actionForXYCbx = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (m_isUpdatingCbx) {
                        return;
                    }
                    ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
                    ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
                    m_plotGraphics.update(refX.getColumnIndex(), refY.getColumnIndex());
                }
                
            };
            
            
            m_valueXComboBox.addActionListener(actionForXYCbx);
            m_valueYComboBox.addActionListener(actionForXYCbx);
            
        }
        
        ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
        ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
        switch (plotType) {
            case HISTOGRAM_PLOT:
                m_plotGraphics = new PlotHistogram(m_plotPanel, m_values, m_crossSelectionInterface, refX.getColumnIndex());
                m_plotPanel.setPlot(m_plotGraphics);
                break;
            case SCATTER_PLOT:
                m_plotGraphics = new PlotScatter(m_plotPanel, m_values, m_crossSelectionInterface, refX.getColumnIndex(), refY.getColumnIndex());
                m_plotPanel.setPlot(m_plotGraphics);
                break;
        }
        
        
        
        
        
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
    public void gridChanged() {
        if (!m_plotPanel.displayGrid()) {
            m_gridButton.setSelected(false);
        }
            
            
    }

    private static class ReferenceToColumn {
        private final String m_name;
        private final int m_columnIndex;
        public ReferenceToColumn(String name, int columnIndex) {
            m_name = name;
            m_columnIndex = columnIndex;
        }
        
        public int getColumnIndex() {
            return m_columnIndex;
        }
        
        @Override
        public String toString() {
            return m_name;
        }
    }
    
}
