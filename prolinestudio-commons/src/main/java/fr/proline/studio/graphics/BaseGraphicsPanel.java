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
package fr.proline.studio.graphics;

import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.export.ExportButton;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_X_ID;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_Y_ID;
import fr.proline.studio.gui.AdvancedSelectionPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.parameter.DefaultParameterDialog;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.openide.windows.WindowManager;
import fr.proline.studio.graphics.BasePlotPanel.PlotToolbarListener;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class BaseGraphicsPanel extends HourglassPanel implements PlotToolbarListener {

    private BasePlotPanel m_plotPanel;
    
    private JComboBox<PlotType> m_allPlotsComboBox;
    private JComboBox<String> m_valueXComboBox;
    private JComboBox<String> m_valueYComboBox;
    private JComboBox<String> m_valueZComboBox;
    private JButton m_selectDataColumnsButton;
    private JLabel m_valueXLabel;
    private JLabel m_valueYLabel;
    private JLabel m_valueZLabel;
    
    private MultiObjectParameter m_columnsParameter = null;
    
    private PlotBaseAbstract m_plotGraphics = null;
    
    private ExtendedTableModelInterface m_values = null;
    private CrossSelectionInterface m_crossSelectionInterface = null;
    
    private boolean m_isUpdatingCbx = false;
    
    private boolean m_dataLocked = false;
    
    private JToggleButton m_gridButton = null;
    private JButton m_importSelectionButton = null;
    private JButton m_exportSelectionButton = null;
    
    
    public BaseGraphicsPanel(boolean dataLocked) {
        setLayout(new BorderLayout());
        
        m_dataLocked = dataLocked;
        
        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);

    }
    
    public PlotBaseAbstract getPlotGraphics() {
        return m_plotGraphics;
    }
    
    public JPanel getPlotPanel() {
        return m_plotPanel;
    }
    
    public final JPanel createInternalPanel() {
        
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);

        PlotPanel panel = new PlotPanel();
        m_plotPanel = panel.getBasePlotPanel();
        m_plotPanel.setPlotToolbarListener(this);
        JPanel selectPanel = createSelectPanel();
        
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(selectPanel, c);

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(panel, c);

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
        
        SettingsButton settingsButton = new SettingsButton(null, m_plotPanel);

        
        m_importSelectionButton  = new JButton(IconManager.getIcon(IconManager.IconType.IMPORT_TABLE_SELECTION));
        m_importSelectionButton.setToolTipText( "Import Selection from Previous View");
        m_importSelectionButton.setFocusPainted(false);
        m_importSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_crossSelectionInterface != null) {
                    ArrayList<Long> selection = m_crossSelectionInterface.getSelection();
                    m_plotPanel.setSelection(selection);
                }
            }
        });
        m_exportSelectionButton  = new JButton(IconManager.getIcon(IconManager.IconType.EXPORT_TABLE_SELECTION));
        m_exportSelectionButton.setToolTipText("Export Selection to Previous View");
        m_exportSelectionButton.setFocusPainted(false);
        m_exportSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_crossSelectionInterface != null) {
                    m_crossSelectionInterface.select(m_plotPanel.getSelection());
                }
            }
        });
        
        
        final JButton lockButton = new JButton(m_dataLocked ? IconManager.getIcon(IconManager.IconType.LOCK) : IconManager.getIcon(IconManager.IconType.UNLOCK));
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
            }
        });
        

        
        ExportButton exportImageButton = new ExportButton("Graphic", m_plotPanel);
        
        
        // add buttons to toolbar
        toolbar.add(m_gridButton);
        toolbar.add(settingsButton);
        toolbar.addSeparator(); // ----
        toolbar.add(lockButton);
        toolbar.add(m_importSelectionButton);
        toolbar.add(m_exportSelectionButton);
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
        m_valueZComboBox = new JComboBox();
        m_selectDataColumnsButton = new JButton("Select Data Columns");
        m_valueXLabel = new JLabel();
        m_valueYLabel = new JLabel();
        m_valueZLabel = new JLabel();
        updateXYCbxVisibility();
        
        m_allPlotsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_isUpdatingCbx) {
                    return;
                }
                

                fillXYCombobox(false);
                updateXYCbxVisibility();
                
            }
        });
        
        m_selectDataColumnsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                ParameterList parameterList = new ParameterList("MULTI_COLUMNS_GRAPHICS");
                
                if (m_columnsParameter == null) {

                    ArrayList<String> columnNamesArrayList = new ArrayList<>();
                    ArrayList<Integer> columnIdsArrayList = new ArrayList<>();

                    int nbColumns = m_values.getColumnCount();
                    for (int i = 0; i < nbColumns; i++) {
                        Class c = m_values.getDataColumnClass(i);
                        if ((c.equals(Double.class)) || (c.equals(Float.class)) || (c.equals(Long.class)) || (c.equals(Integer.class)) || (c.equals(String.class))) {
                            columnNamesArrayList.add(m_values.getDataColumnIdentifier(i));
                            columnIdsArrayList.add(i);
                        }
                    }

                    int nb = columnNamesArrayList.size();

                    boolean[] selection = new boolean[nb];
                    for (int i = 0; i < nb; i++) {
                        selection[i] = false;
                    }

                    Object[] columnNamesArray = columnNamesArrayList.toArray(new String[nb]);
                    Object[] columnIdsArray = columnIdsArrayList.toArray(new Integer[nb]);

                    m_columnsParameter = new MultiObjectParameter("MULTI_COLUMNS", "Columns Selection", "Selected Columns", "Unselected Columns", AdvancedSelectionPanel.class, columnNamesArray, columnIdsArray, selection, null);

                }
                parameterList.add(m_columnsParameter);
                
                ArrayList<ParameterList> parameterListArray = new ArrayList<>(1);
                parameterListArray.add(parameterList);
                
                DefaultParameterDialog parameterDialog = new DefaultParameterDialog(WindowManager.getDefault().getMainWindow(), "Columns Parameters", parameterListArray);
                parameterDialog.setLocationRelativeTo(m_plotPanel);
                parameterDialog.setVisible(true);
                
                if (parameterDialog.getButtonClicked() == DefaultParameterDialog.BUTTON_OK) {
                    ArrayList<Integer> selectedColumnsList = (ArrayList<Integer>) m_columnsParameter.getAssociatedValues(true);
                    int nbSelected = selectedColumnsList.size();

                    int[] cols = new int[nbSelected];
                    for (int i = 0; i < nbSelected; i++) {
                        cols[i] = selectedColumnsList.get(i);
                    }
                    m_plotGraphics.update(cols, null);
                }
                
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
        selectPanel.add(m_selectDataColumnsButton, c);
        
        c.gridx++;
        selectPanel.add(m_valueYLabel, c);
        
        c.gridx++;
        selectPanel.add(m_valueYComboBox, c);
        
        c.gridx++;
        selectPanel.add(m_valueZLabel, c);
        
        c.gridx++;
        selectPanel.add(m_valueZComboBox, c);
        
        c.gridx++;
        c.weightx = 1;
        selectPanel.add(Box.createHorizontalGlue(), c);
        
        return selectPanel;
        
    }
    
    private void updateXYCbxVisibility() {
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
        
        m_valueXLabel.setVisible(plotType.needsX() || plotType.needsMultiData());
        m_valueXComboBox.setVisible(plotType.needsX());
        
        m_selectDataColumnsButton.setVisible(plotType.needsMultiData());
        
        m_valueYLabel.setVisible(plotType.needsY());
        m_valueYComboBox.setVisible(plotType.needsY());
        
        m_valueZLabel.setVisible(plotType.needsZ());
        m_valueZComboBox.setVisible(plotType.needsZ());
        
        if (plotType.needsX() || plotType.needsMultiData()) {
            m_valueXLabel.setText(plotType.getXLabel());
        }
        if (plotType.needsY()) {
            m_valueYLabel.setText(plotType.getYLabel());
        }
        if (plotType.needsZ()) {
            m_valueZLabel.setText(plotType.getZLabel());
        }
        
        
    }
    
    private void fillXYCombobox(boolean changePlotType) {

        m_isUpdatingCbx = true;
        try {

            if (m_values == null) {
                return;
            }
            
            // clear combobox
            ((DefaultComboBoxModel) m_valueXComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel) m_valueYComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel) m_valueZComboBox.getModel()).removeAllElements();

            PlotType plotType = null;
            if ((changePlotType) && (m_values instanceof BestGraphicsInterface)) {
                plotType = ((BestGraphicsInterface)m_values).getBestPlotType();
                if (plotType != null) {
                    m_allPlotsComboBox.setSelectedItem(plotType);
                }
            }
            if (plotType == null) {
                plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
            }
            if (changePlotType) {
                updateXYCbxVisibility();
            }

            HashSet<Class> acceptedValues = plotType.getAcceptedXValues();

            int nbValuesType = m_values.getColumnCount();
            
            // find the best column for the current plot
            int bestColX = -1;
            int bestColY = -1;
            if (m_values instanceof BestGraphicsInterface) {
                BestGraphicsInterface bestGraphics = (BestGraphicsInterface) m_values;
                int[] cols = bestGraphics.getBestColIndex(plotType);
                if (cols!= null) {
                    bestColX = cols[0];
                    bestColY = cols[1];
                }

            }

            // fill the comboboxes and find the index to be selected
            int bestColIndexXCbx = 0;
            int bestColIndexYCbx = (nbValuesType >= 2) ? 1 : 0;
            int nbValuesInCbx = 0;
            for (int i = 0; i < nbValuesType; i++) {
                Class c = m_values.getDataColumnClass(i);
                if (acceptedValues.contains(c)) {
                    ReferenceIdName ref = new ReferenceIdName(m_values.getDataColumnIdentifier(i), i);
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
            
            if (plotType.needsZ()) {
                ArrayList<String> zValues = plotType.getZValues();
                for (int i=0;i<zValues.size();i++) {
                    ((DefaultComboBoxModel) m_valueZComboBox.getModel()).addElement(zValues.get(i));
                }
            }
            
            
            setDataImpl(m_values, m_crossSelectionInterface);


        } finally {
            m_isUpdatingCbx = false;
        }
    }
    
    public void setXColTypeIndex(int index) {
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) m_valueXComboBox.getModel();

        int count = model.getSize();
        for (int i=0;i<count;i++) {
            ReferenceIdName ref = (ReferenceIdName) model.getElementAt(i);
            if (ref.getColumnIndex() == index) {
                m_valueXComboBox.setSelectedIndex(index);
                break;
            }
        }
    }
    public void setYColTypeIndex(int index) {
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) m_valueYComboBox.getModel();
        
        
        
        int count = model.getSize();
        for (int i=0;i<count;i++) {
            ReferenceIdName ref = (ReferenceIdName) model.getElementAt(i);
            if (ref.getColumnIndex() == index) {
                m_valueYComboBox.setSelectedIndex(index);
                break;
            }
        }
        
         
    }
    
    public void setData(ExtendedTableModelInterface values, CrossSelectionInterface crossSelectionInterface) {
        if (m_plotPanel.isLocked()) {
            return;
        }
        if ((m_dataLocked) && !(values instanceof LockedDataModel)) {
            // wart for first call when directly locked
            values = new LockedDataModel(values);
        }
        setDataImpl(values, crossSelectionInterface);
        if (m_dataLocked) {
            // check that plotPanel corresponds, it can not correspond at the first call
            m_plotPanel.lockData(m_dataLocked);
        }
    }
    private void setDataImpl(ExtendedTableModelInterface values, CrossSelectionInterface crossSelectionInterface) {

        m_values = values;
        m_crossSelectionInterface = crossSelectionInterface;
        
        if (values == null) {
            return;
        }
        
        
        if (m_valueXComboBox.getItemCount() == 0) {
            
            fillXYCombobox(true);
            
            ActionListener actionForXYCbx = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (m_isUpdatingCbx) {
                        return;
                    }
                    ReferenceIdName refX = (ReferenceIdName) m_valueXComboBox.getSelectedItem();
                    ReferenceIdName refY = (ReferenceIdName) m_valueYComboBox.getSelectedItem();
                    String zParameter = (String) m_valueZComboBox.getSelectedItem();
                    
                    int[] cols = new int[2]; //JPM.TODO enhance
                    cols[COL_X_ID] = refX.getColumnIndex();
                    cols[COL_Y_ID] = refY.getColumnIndex();
                    m_plotGraphics.update(cols, zParameter);
                }
                
            };
            
            
            m_valueXComboBox.addActionListener(actionForXYCbx);
            m_valueYComboBox.addActionListener(actionForXYCbx);
            m_valueZComboBox.addActionListener(actionForXYCbx);
            
        }
        
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
   
        int[] cols = null;
        if (m_columnsParameter == null) {

            if (m_values instanceof BestGraphicsInterface) {
                BestGraphicsInterface bestGraphics = (BestGraphicsInterface) m_values;
                
                
                //JPM.TODO : this is a wart (JPM.WART): should be done in another way
                cols = bestGraphics.getBestColIndex(PlotType.VENN_DIAGRAM_PLOT);
                if (cols == null) {
                    cols = bestGraphics.getBestColIndex(PlotType.PARALLEL_COORDINATES_PLOT);
                }
                
                boolean[] done = null;
                if (cols != null) {
                    done = new boolean[cols.length];
                    Arrays.fill(done, Boolean.FALSE);
                }
                
                ArrayList<String> columnNamesArrayList = new ArrayList<>();
                ArrayList<Integer> columnIdsArrayList = new ArrayList<>();

                int nbColumns = m_values.getColumnCount();
                int indexCur = 0;
                for (int i = 0; i < nbColumns; i++) {
                    Class c = m_values.getDataColumnClass(i);
                    
                    if ((c!= null) && ((c.equals(Double.class)) || (c.equals(Float.class)) || (c.equals(Long.class)) || (c.equals(Integer.class)) || (c.equals(String.class)))) {
                        columnNamesArrayList.add(m_values.getDataColumnIdentifier(i));
                        columnIdsArrayList.add(i);
                        
                        if (cols != null) {
                            for (int j = 0; j < cols.length; j++) {
                                if ((!done[j]) && (cols[j] == i)) {
                                    cols[j] = indexCur;
                                    done[j] = true;
                                }
                            }
                        }
                        indexCur++;
                    }
                }

                int nb = columnNamesArrayList.size();

                boolean[] selection = new boolean[nb];
                for (int i = 0; i < nb; i++) {
                    selection[i] = false;
                }
                if (cols != null) {
                    for (int j = 0; j < cols.length; j++) {
                        selection[cols[j]] = true;
                    }
                }

                Object[] columnNamesArray = columnNamesArrayList.toArray(new String[nb]);
                Object[] columnIdsArray = columnIdsArrayList.toArray(new Integer[nb]);

                m_columnsParameter = new MultiObjectParameter("MULTI_COLUMNS", "Columns Selection", "Selected Columns", "Unselected Columns", AdvancedSelectionPanel.class, columnNamesArray, columnIdsArray, selection, null);
                m_columnsParameter.getComponent(null); // finalize initialization, to be able to retrieve selected cols.
            }
        } else {
            
            ArrayList<Integer> selectedColumnsList = (ArrayList<Integer>) m_columnsParameter.getAssociatedValues(true);
            int nbSelected = selectedColumnsList.size();

            cols = new int[nbSelected];
            for (int i = 0; i < nbSelected; i++) {
                cols[i] = selectedColumnsList.get(i);
            }
        }
        
        ReferenceIdName refX = (ReferenceIdName) m_valueXComboBox.getSelectedItem();
        ReferenceIdName refY = (ReferenceIdName) m_valueYComboBox.getSelectedItem();
        String zParameter = (String) m_valueZComboBox.getSelectedItem();
        
                
        // enable buttons before to reinit them
        m_gridButton.setEnabled(true);
        m_importSelectionButton.setEnabled(true);
        m_exportSelectionButton.setEnabled(true);
        
        switch (plotType) {
            case HISTOGRAM_PLOT:
                m_plotGraphics = new PlotHistogram(m_plotPanel, m_values, m_crossSelectionInterface, refX.getColumnIndex(), zParameter);
                m_plotPanel.setPlot(m_plotGraphics);
                break;
            case SCATTER_PLOT:
                m_plotGraphics = new PlotScatter(m_plotPanel, m_values, m_crossSelectionInterface, refX.getColumnIndex(), refY.getColumnIndex());
                m_plotPanel.setPlot(m_plotGraphics);
                break;
            case VENN_DIAGRAM_PLOT:
                m_plotGraphics = new PlotVennDiagram(m_plotPanel, m_values, m_crossSelectionInterface, cols);
                m_plotPanel.setPlot(m_plotGraphics);
                break;
            case PARALLEL_COORDINATES_PLOT:
                m_plotGraphics = new PlotParallelCoordinates(m_plotPanel, m_values, m_crossSelectionInterface, cols);
                m_plotPanel.setPlot(m_plotGraphics);
                break;
            case LINEAR_PLOT:
                m_plotGraphics = new PlotLinear(m_plotPanel, m_values, m_crossSelectionInterface, refX.getColumnIndex(), refY.getColumnIndex());
                m_plotPanel.setPlot(m_plotGraphics);
                break;
        }

        
        
        
    }


    @Override
    public void stateModified(BUTTONS b) {
        switch (b) {
            case GRID:
                if (!m_plotPanel.displayGrid()) {
                    m_gridButton.setSelected(false);
                }
                break;
        }
        
    }

    @Override
    public void enable(BUTTONS b, boolean v) {
        switch (b) {
            case GRID:
                m_gridButton.setEnabled(v);
                break;
            case IMPORT_SELECTION:
                m_importSelectionButton.setEnabled(v);
                break;
            case EXPORT_SELECTION:
                m_exportSelectionButton.setEnabled(v);
                break;
        }
    }


    
}
