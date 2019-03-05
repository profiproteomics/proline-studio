package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotBaseAbstract;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_X_ID;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_Y_ID;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.DefaultParameterDialog;
import fr.proline.studio.parameter.ParameterList;
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
import fr.proline.studio.graphics.BasePlotPanel.PlotToolbarListener;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class MultiGraphicsPanel extends HourglassPanel implements DataBoxPanelInterface, PlotToolbarListener {

    private static final Logger m_logger = LoggerFactory.getLogger(MultiGraphicsPanel.class);
    protected AbstractDataBox m_dataBox;

    protected BasePlotPanel m_plotPanel;

    protected boolean m_canChooseColor = false;
    //plot type combo box
    protected JComboBox<PlotType> m_allPlotsComboBox;
    //Axis combo box
    protected JComboBox<String> m_valueXComboBox;
    protected JComboBox<String> m_valueYComboBox;
    protected JComboBox<String> m_valueZComboBox;
    //Axis label
    protected JLabel m_valueXLabel;
    protected JLabel m_valueYLabel;
    protected JLabel m_valueZLabel;

    protected List<PlotBaseAbstract> m_plotGraphicsList = null;

    protected List<ExtendedTableModelInterface> m_valuesList = null;
    protected List<CrossSelectionInterface> m_crossSelectionInterfaceList = null;

    protected boolean m_isUpdatingCbx = false;

    protected boolean m_dataLocked = false;

    protected JToggleButton m_gridButton = null;
    protected JButton m_importSelectionButton = null;
    protected JButton m_exportSelectionButton = null;

    public MultiGraphicsPanel(boolean dataLocked, boolean canChooseColor) {
        m_dataLocked = dataLocked;
        m_canChooseColor = canChooseColor;
        m_plotGraphicsList = new ArrayList();

        initComponent();
    }

    protected void initComponent() {
        JPanel internalPanel = createInternalPanel();
        JToolBar toolbar = initToolbar();

        this.setLayout(new BorderLayout());
        add(internalPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
    }

    protected JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);

        m_plotPanel = new BasePlotPanel();
        m_plotPanel.setPlotToolbarListener(this);
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

    protected final JToolBar initToolbar() {

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

        m_importSelectionButton = new JButton(IconManager.getIcon(IconManager.IconType.IMPORT_TABLE_SELECTION));
        m_importSelectionButton.setToolTipText("Import Selection from Previous View");
        m_importSelectionButton.setFocusPainted(false);
        m_importSelectionButton.setEnabled(!m_dataLocked);
        m_importSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /*if (m_crossSelectionInterface != null) {
                    ArrayList<Integer> selection = m_crossSelectionInterface.getSelectedIds();
                    m_plotPanel.setSelectedIds(selection);
                }*/
            }
        });
        m_exportSelectionButton = new JButton(IconManager.getIcon(IconManager.IconType.EXPORT_TABLE_SELECTION));
        m_exportSelectionButton.setToolTipText("Export Selection to Previous View");
        m_exportSelectionButton.setFocusPainted(false);
        m_exportSelectionButton.setEnabled(!m_dataLocked);
        m_exportSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /*if (m_crossSelectionInterface != null) {
                    m_crossSelectionInterface.select(m_plotPanel.getSelectedIds());
                }*/
            }
        });

        final JButton lockButton = new JButton(m_dataLocked ? IconManager.getIcon(IconManager.IconType.LOCK) : IconManager.getIcon(IconManager.IconType.UNLOCK));
        lockButton.setToolTipText("Lock/Unlock Input Data");
        lockButton.setFocusPainted(false);
        lockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
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
                 */
            }
        });

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
        toolbar.add(m_gridButton);
        if (m_canChooseColor) {
            toolbar.add(colorPicker);
        }
        toolbar.addSeparator(); // ----
        /*toolbar.add(lockButton);
        toolbar.add(importSelectionButton);
        toolbar.add(exportSelectionButton);
        toolbar.addSeparator(); // ----
         */
        toolbar.add(exportImageButton);

        return toolbar;

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
                setDataImpl(m_valuesList, m_crossSelectionInterfaceList);
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

    public void setData(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList) {
        if (m_plotPanel.isLocked()) {
            return;
        }
        for (int i = 0; i < valuesList.size(); i++) {
            ExtendedTableModelInterface values = valuesList.get(i);
            if ((m_dataLocked) && !(values instanceof LockedDataModel)) {
                // wart for first call when directly locked
                values = new LockedDataModel(values);
                valuesList.set(i, values);
            }
        }

        setDataImpl(valuesList, crossSelectionInterfaceList);
        if (m_dataLocked) {
            // check that plotPanel corresponds, it can not correspond at the first call
            m_plotPanel.lockData(m_dataLocked);
        }
    }

    /**
     *
     * @param valuesList
     * @param crossSelectionInterfaceList
     * @param isSingle to display one plot
     */
    protected void setDataImpl(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList) {

        m_valuesList = valuesList;
        m_crossSelectionInterfaceList = crossSelectionInterfaceList;
        m_plotGraphicsList.clear();

        if (valuesList == null) {
            return;
        }

        if (m_valueXComboBox.getItemCount() == 0) {
            fillXYCombobox();//update select panel combo box

            ActionListener actionForXYCbx = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (m_isUpdatingCbx) {
                        return;
                    }
                    ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
                    ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
                    String zParameter = (String) m_valueZComboBox.getSelectedItem();
                    int[] cols = new int[2]; //JPM.TODO enhance
                    cols[COL_X_ID] = refX.getColumnIndex();
                    cols[COL_Y_ID] = refY.getColumnIndex();
                    m_logger.debug(String.format("--**--value X%s-(%d),  Y%s-(%d)", refX.toString(), cols[0], refY.toString(), cols[1]));
                    for (PlotBaseAbstract plotGraphic : m_plotGraphicsList) {
                        plotGraphic.update(cols, zParameter);
                        m_plotPanel.updateAxis(plotGraphic);
                    }
                    m_plotPanel.repaint();
                }
            };

            m_valueXComboBox.addActionListener(actionForXYCbx);
            m_valueYComboBox.addActionListener(actionForXYCbx);
            m_valueZComboBox.addActionListener(actionForXYCbx);

        }

        ReferenceToColumn refX = (ReferenceToColumn) m_valueXComboBox.getSelectedItem();
        ReferenceToColumn refY = (ReferenceToColumn) m_valueYComboBox.getSelectedItem();
        //String zParameter = (String) m_valueZComboBox.getSelectedItem();
        PlotType plotType = (PlotType) m_allPlotsComboBox.getSelectedItem();
        switch (plotType) {
            case LINEAR_PLOT: {
                m_plotPanel.clearPlots();
                for (int i = 0; i < m_valuesList.size(); i++) {
                    CrossSelectionInterface crossSelectionInterface = (m_crossSelectionInterfaceList == null) || (m_crossSelectionInterfaceList.size() <= i) ? null : m_crossSelectionInterfaceList.get(i);
                    //create plotGraphics for each table
                    PlotLinear plotGraphics = new PlotLinear(m_plotPanel, m_valuesList.get(i), crossSelectionInterface, refX.getColumnIndex(), refY.getColumnIndex());
                    plotGraphics.setPlotInformation(m_valuesList.get(i).getPlotInformation());
                    plotGraphics.setIsPaintMarker(false);
                    m_plotPanel.addPlot(plotGraphics);
                    m_plotGraphicsList.add(plotGraphics);
                }
                m_plotPanel.repaint();
                break;
            }
        }

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
