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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.DefaultParameterDialog;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.openide.windows.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_X_ID;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_Y_ID;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import fr.proline.studio.graphics.core.GraphicsToolbarPanel;
import fr.proline.studio.pattern.DataboxMultiGraphics;
import java.awt.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To rename : This Panel displays multiple Linear Plots : One per
 * ExtendedTableModelInterface passed through setData method. A second (set:
 * actually one, should be list) of object could also be plotted on a second
 * axis . ==> Rename to MultipleLinearPlotsPanel ... ?
 *
 *
 * @author JM235353
 */
public class MultiGraphicsPanel extends GraphicsToolbarPanel implements DataBoxPanelInterface {

    private static final Logger m_logger = LoggerFactory.getLogger(MultiGraphicsPanel.class);
    protected AbstractDataBox m_dataBox;


    protected boolean m_canChooseColor = false;
    //plot type combo box
    protected JComboBox<PlotType> m_allPlotsComboBox;
    //Axis combo box
    protected JComboBox<String> m_valueXComboBox;
    protected JComboBox<String> m_valueYComboBox;
    protected JComboBox<String> m_valueZComboBox;
    protected int[] columnXYIndex;
    //Axis label
    protected JLabel m_valueXLabel;
    protected JLabel m_valueYLabel;
    protected JLabel m_valueZLabel;

    protected List<ExtendedTableModelInterface> m_valuesList = null;
    protected List<CrossSelectionInterface> m_crossSelectionInterfaceList = null;
    protected SecondAxisTableModelInterface m_valueOn2Yxis = null;
    protected boolean m_isUpdatingCbx = false;

    private boolean m_setHideButton;

    public MultiGraphicsPanel(boolean dataLocked, boolean canChooseColor, boolean isDoubleYAxis, boolean setHideButton) {
        super(dataLocked, isDoubleYAxis);
        
        columnXYIndex = new int[2];
        m_canChooseColor = canChooseColor;
        m_setHideButton = setHideButton;
    }

    @Override
    protected final JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);
        
        PlotPanel panel = new PlotPanel(m_isDoubleYAxis);
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

    @Override
    public final void fillToolbar(JToolBar toolbar) {

        ExportButton exportImageButton = new ExportButton("Graphic", m_plotPanel);

        JButton colorPicker = new JButton(IconManager.getIcon(IconManager.IconType.SETTINGS));
        colorPicker.setFocusPainted(false);
        colorPicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<ParameterList> parameterListArray = m_plotPanel.getParameters();
                if (parameterListArray == null) {
                    return;
                }
                DefaultParameterDialog parameterDialog = new DefaultParameterDialog(WindowManager.getDefault().getMainWindow(), "Plot Parameters", parameterListArray);
                parameterDialog.setLocationRelativeTo(m_plotPanel);
                parameterDialog.setVisible(true);

                if (parameterDialog.getButtonClicked() == DefaultParameterDialog.BUTTON_OK) {
                    m_plotPanel.parametersChanged();
                }

            }
        });

        // add buttons to toolbar
        if (m_canChooseColor) {
            toolbar.add(colorPicker);
        }
        if (m_setHideButton) {
            JToggleButton showSelectionButton;
            showSelectionButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.SELECT_ALL));
            showSelectionButton.setSize(m_gridButton.getSize());
            showSelectionButton.setToolTipText("Hide Selection");
            showSelectionButton.setEnabled(true);
            showSelectionButton.setFocusPainted(false);
            showSelectionButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((DataboxMultiGraphics) m_dataBox).hideSelection(showSelectionButton.isSelected());
                }
            });
            toolbar.add(showSelectionButton);
        }
        toolbar.addSeparator(); // ----
        toolbar.add(exportImageButton);


    }

    protected JPanel createSelectPanel() {
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel selectLabel = new JLabel("Graphic :");
        m_allPlotsComboBox = new JComboBox(PlotType.LINEAR_PLOTS);
        m_valueXComboBox = new JComboBox();
        m_valueYComboBox = new JComboBox();
        m_valueZComboBox = new JComboBox();
        m_valueXLabel = new JLabel();
        m_valueYLabel = new JLabel();
        m_valueZLabel = new JLabel();
        updateXYCbxVisibility();

        m_allPlotsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fillXYCombobox();
                setDataImpl(false);
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
        selectPanel.add(m_valueZLabel, c);

        c.gridx++;
        selectPanel.add(m_valueZComboBox, c);

        c.gridx++;
        c.weightx = 1;
        selectPanel.add(Box.createHorizontalGlue(), c);

        return selectPanel;

    }

    /**
     * for the current Plot, needsX(), needsY() will affect m_valueX-Y-ZLabel
     * and m_valueX-Y-Z ComboBox
     */
    protected void updateXYCbxVisibility() {
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();

        m_valueXLabel.setVisible(plotType.needsX());
        m_valueXComboBox.setVisible(plotType.needsX());

        m_valueYLabel.setVisible(plotType.needsY());
        m_valueYComboBox.setVisible(plotType.needsY());

        m_valueZLabel.setVisible(plotType.needsZ());
        m_valueZComboBox.setVisible(plotType.needsZ());

        if (plotType.needsX()) {
            m_valueXLabel.setText(plotType.getXLabel());
        }
        if (plotType.needsY()) {
            m_valueYLabel.setText(plotType.getYLabel());
        }

    }

    /**
     * set data in Combo Box X, Y
     */
    protected void fillXYCombobox() {
        m_isUpdatingCbx = true;
        try {
            // clear combobox
            ((DefaultComboBoxModel) m_valueXComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel) m_valueYComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel) m_valueZComboBox.getModel()).removeAllElements();

            PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
            HashSet<Class> acceptedValues = plotType.getAcceptedXValues(); //Double,Float,Integer,String...

            int nbValuesType = 0;
            boolean hasValues = m_valuesList != null && !m_valuesList.isEmpty();

            if (hasValues) {
                nbValuesType = m_valuesList.get(0).getColumnCount();
            }

            // find the best column for the current plot
            int bestColX = -1;
            int bestColY = -1;
            if (hasValues && m_valuesList.get(0) instanceof BestGraphicsInterface) {
                BestGraphicsInterface bestGraphics = (BestGraphicsInterface) m_valuesList.get(0);
                int[] cols = bestGraphics.getBestColIndex(plotType);
                if (cols != null) {
                    bestColX = cols[0];
                    bestColY = cols[1];
                }
            }

            // fill the comboboxes and find the index to be selected
            int bestColIndexXCbx = 0;
            int bestColIndexYCbx = (nbValuesType >= 2) ? 1 : 0;
            int nbValuesInCbx = 0;
            for (int i = 0; i < nbValuesType; i++) {
                Class c = m_valuesList.get(0).getDataColumnClass(i);
                if (acceptedValues.contains(c)) {
                    ReferenceToColumn ref = new ReferenceToColumn(m_valuesList.get(0).getDataColumnIdentifier(i), i);
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
            if (nbValuesType > 0) {
                m_valueXComboBox.setSelectedIndex(bestColIndexXCbx);
            }
            if (nbValuesType > 0) {
                m_valueYComboBox.setSelectedIndex(bestColIndexYCbx);
            }
            if (plotType.needsZ()) {
                ArrayList<String> zValues = plotType.getZValues();
                for (int i = 0; i < zValues.size(); i++) {
                    ((DefaultComboBoxModel) m_valueZComboBox.getModel()).addElement(zValues.get(i));
                }
            }

        } finally {
            m_isUpdatingCbx = false;
        }
    }

    protected void setXYZComboBox() {
        if (m_valueXComboBox.getItemCount() == 0) {
            fillXYCombobox();//update select panel combo box

            ActionListener actionForXYCbx = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //m_logger.debug("--**--actionPerformed actionForXYCbx " + e.getActionCommand());
                    if (m_isUpdatingCbx) {//=setData in combo box
                        return;
                    }
                    ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
                    ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
                    String zParameter = (String) m_valueZComboBox.getSelectedItem();
                    int[] cols = new int[2];
                    cols[COL_X_ID] = refX.getColumnIndex();
                    cols[COL_Y_ID] = refY.getColumnIndex();
                    if ((cols[0] == columnXYIndex[COL_X_ID]) && (cols[1] == columnXYIndex[COL_Y_ID])) {
                        return;
                    }
                    columnXYIndex[COL_X_ID] = cols[0];
                    columnXYIndex[COL_Y_ID] = cols[1];

                    m_plotPanel.updatePlots(cols, zParameter);
                    m_plotPanel.repaint();
                }
            };

            m_valueXComboBox.addActionListener(actionForXYCbx);
            m_valueYComboBox.addActionListener(actionForXYCbx);
            m_valueZComboBox.addActionListener(actionForXYCbx);

        }
    }

    public void setData(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList) {
        this.setData(valuesList, crossSelectionInterfaceList, m_valueOn2Yxis, false);
    }

    public void setData(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList, SecondAxisTableModelInterface value2, boolean keepZoom) {
        if (m_plotPanel.isLocked()) {
            return;
        }
        m_valuesList = valuesList;
        m_crossSelectionInterfaceList = crossSelectionInterfaceList;
        m_valueOn2Yxis = value2;
        if (valuesList == null) {
            return;
        }
        for (int i = 0; i < valuesList.size(); i++) {
            ExtendedTableModelInterface values = valuesList.get(i);
            if ((m_dataLocked) && !(values instanceof LockedDataModel)) {
                // wart for first call when directly locked
                values = new LockedDataModel(values);
                valuesList.set(i, values);//replace values with the same but locked values
            }
        }
        this.setXYZComboBox();
        this.setDataImpl(keepZoom);
        if (m_dataLocked) {
            // check that plotPanel corresponds, it can not correspond at the first call
            m_plotPanel.lockData(m_dataLocked);
        }
    }

    /**
     *
     */
    private void setDataImpl(boolean keepZoom) {
        ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
        ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
        if (refX != null && refY != null) {
            columnXYIndex[COL_X_ID] = refX.getColumnIndex();
            columnXYIndex[COL_Y_ID] = refY.getColumnIndex();
        }
        //String zParameter = (String) m_valueZComboBox.getSelectedItem();
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
        switch (plotType) {
            case LINEAR_PLOT: {
                
                // Values if keepZoom is true
                XAxis xAxis = m_plotPanel.getXAxis();
                double minXValue = xAxis.getMinValue();
                double maxXValue = xAxis.getMaxValue();
                YAxis yAxis = m_plotPanel.getYAxis();
                double minYValue = yAxis.getMinValue();
                double maxYValue = yAxis.getMaxValue();
                
                m_plotPanel.clearPlots();
                if (m_isDoubleYAxis) {
                    setPlotsWithDoubleYAxis();
                } else {
                    setPlots();
                }
                
                if (keepZoom) {
                    xAxis = m_plotPanel.getXAxis(); // axis has been changed
                    xAxis.setRange(minXValue, maxXValue);
                    yAxis = m_plotPanel.getYAxis();
                    yAxis.setRange(minYValue, maxYValue);
                }
                
                m_plotPanel.repaint();

                break;
            }
        }
    }

    private void setPlots() {
        for (int i = 0; i < m_valuesList.size(); i++) {
            CrossSelectionInterface crossSelectionInterface = (m_crossSelectionInterfaceList == null) || (m_crossSelectionInterfaceList.size() <= i) ? null : m_crossSelectionInterfaceList.get(i);
            //create plotGraphics for each table
            PlotLinear plotGraphics = new PlotLinear(m_plotPanel, m_valuesList.get(i), crossSelectionInterface, columnXYIndex[COL_X_ID], columnXYIndex[COL_Y_ID]);
            plotGraphics.setPlotInformation(m_valuesList.get(i).getPlotInformation());
            plotGraphics.setIsPaintMarker(false);
            m_plotPanel.addPlot(plotGraphics, true);
        }
    }

    private void setPlotsWithDoubleYAxis() {
  
        double mainPlotMaxY = Double.NEGATIVE_INFINITY;
        double secondPlotMaxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < m_valuesList.size(); i++) {
            CrossSelectionInterface crossSelectionInterface = (m_crossSelectionInterfaceList == null) || (m_crossSelectionInterfaceList.size() <= i) ? null : m_crossSelectionInterfaceList.get(i);
            //create plotGraphics for each table
            PlotLinear plotGraphics = new PlotLinear(m_plotPanel, m_valuesList.get(i), crossSelectionInterface, columnXYIndex[COL_X_ID], columnXYIndex[COL_Y_ID]);
            mainPlotMaxY = Math.max(mainPlotMaxY, plotGraphics.getYMax());
            plotGraphics.setPlotInformation(m_valuesList.get(i).getPlotInformation());
            plotGraphics.setIsPaintMarker(false);
            m_plotPanel.addPlot(plotGraphics, true);
        }
        //plot on second Axis Y
        if (m_valueOn2Yxis != null && m_valueOn2Yxis.getRowCount() != 0) {//creat a plot which show PlotLinear on 2nd Axis  
            CrossSelectionInterface crossSelectionInterface2 = null;
            PlotLinear plotGraphics = new PlotLinear(m_plotPanel, m_valueOn2Yxis, crossSelectionInterface2, columnXYIndex[COL_X_ID], columnXYIndex[COL_Y_ID]);
            secondPlotMaxY = plotGraphics.getYMax();
            plotGraphics.setPlotInformation(m_valueOn2Yxis.getPlotInformation());
            plotGraphics.setIsPaintMarker(false);
            m_plotPanel.addPlot(plotGraphics, false);
            Color color = m_valueOn2Yxis.getPlotInformation().getPlotColor();
            String axisTitle = m_valueOn2Yxis.getName();
            if (axisTitle == null) {
                axisTitle = "";
            }
            m_plotPanel.setSecondAxisPlotInfo(axisTitle + " " + m_valueOn2Yxis.getDataColumnIdentifier(columnXYIndex[COL_Y_ID]), color);
        }

        m_plotPanel.updateAxis();


    }

    @Override
    public void addSingleValue(Object v) {
        // should not be called
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


    public void setHideButton(boolean h) {
        m_setHideButton = h;
    }

    protected static class ReferenceToColumn {

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
