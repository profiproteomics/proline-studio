package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotBaseAbstract;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_X_ID;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_Y_ID;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.pattern.AbstractDataBox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.DoubleYAxisPlotPanel;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.awt.Color;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class XicAbundanceGraphicPanel extends MultiGraphicsPanel {

    //private static final Logger m_logger = LoggerFactory.getLogger(XicAbundanceGraphicPanel.class);
    private AbstractDataBox m_dataBox;

    private DoubleYAxisPlotPanel m_plotPanel;
    private PTMSite m_ptmSite;

    public BasePlotPanel getM_plotPanel() {
        return m_plotPanel;
    }

    public XicAbundanceGraphicPanel(boolean dataLocked, boolean canChooseColor) {
        super(dataLocked, canChooseColor);
    }

    @Override
    public JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);

        m_plotPanel = new DoubleYAxisPlotPanel();
        m_plotPanel.setPlotToolbarListener(this);
        //m_plotPanel.setPlotTitle("Abundance-PTM site");
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

    public void setData(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList, XicAbundanceProteinTableModel proteinAbundance) {
        if (m_plotPanel.isLocked()) {
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
        setDataImpl(valuesList, crossSelectionInterfaceList, proteinAbundance);
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
    private void setDataImpl(List<ExtendedTableModelInterface> valuesList, List<CrossSelectionInterface> crossSelectionInterfaceList,
            XicAbundanceProteinTableModel proteinAbundance) {
        m_valuesList = valuesList;
        m_crossSelectionInterfaceList = crossSelectionInterfaceList;
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
                    int[] cols = new int[2];
                    cols[COL_X_ID] = refX.getColumnIndex();
                    cols[COL_Y_ID] = refY.getColumnIndex();
                    m_plotPanel.updatePlots(cols, zParameter);
                    if (proteinAbundance.getRowCount() > 0 && m_valuesList.size() > 0) {
                        Color color = proteinAbundance.getPlotInformation().getPlotColor();
                        m_plotPanel.setSecondAxisPlotInfo("Protein " + proteinAbundance.getColumnName(refY.getColumnIndex()), color);
                        m_plotPanel.preparePaint();
                    }
                }
            };

            //@Karine XUE: if Axis change, Combo Box Change
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
                //plot on main Axis Y
                for (int i = 0; i < m_valuesList.size(); i++) {
                    CrossSelectionInterface crossSelectionInterface = (m_crossSelectionInterfaceList == null) || (m_crossSelectionInterfaceList.size() <= i) ? null : m_crossSelectionInterfaceList.get(i);
                    //create plotGraphics for each table
                    PlotLinear plotGraphics = new PlotLinear(m_plotPanel, m_valuesList.get(i), crossSelectionInterface, refX.getColumnIndex(), refY.getColumnIndex());
                    plotGraphics.setPlotInformation(m_valuesList.get(i).getPlotInformation());
                    plotGraphics.setIsPaintMarker(false);
                    m_plotPanel.addPlot(plotGraphics, DoubleYAxisPlotPanel.Layout.MAIN);
                }
                //plot on second Axis Y
                if (proteinAbundance.getRowCount() != 0 && m_valuesList.size() != 0) {//creat a plot which show protein abundance  
                    CrossSelectionInterface crossSelectionInterface2 = null;
                    PlotLinear plotGraphics = new PlotLinear(m_plotPanel, proteinAbundance, crossSelectionInterface2, refX.getColumnIndex(), refY.getColumnIndex());
                    plotGraphics.setPlotInformation(proteinAbundance.getPlotInformation());
                    plotGraphics.setIsPaintMarker(false);
                    m_plotPanel.addPlot(plotGraphics, DoubleYAxisPlotPanel.Layout.SECOND);
                    Color color = proteinAbundance.getPlotInformation().getPlotColor();
                    m_plotPanel.setSecondAxisPlotInfo("Protein " + proteinAbundance.getColumnName(refY.getColumnIndex()), color);
                }
                m_plotPanel.preparePaint();
                break;
            }
        }
    }

}
