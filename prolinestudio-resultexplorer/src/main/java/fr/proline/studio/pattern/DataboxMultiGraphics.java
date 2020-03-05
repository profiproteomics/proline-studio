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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.xic.XICComparePeptideTableModel;
import java.util.ArrayList;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DataboxMultiGraphics extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<ExtendedTableModelInterface> m_plotValues = null;
    private SecondAxisTableModelInterface m_plotSecondAxisValues = null;
    private List<CrossSelectionInterface> m_crossSelectionValues = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;
    private boolean m_displayDoubleYAxis = false;
    private boolean m_hideSelection = false;
    private ArrayList<Integer> m_selectedIndex;
    private boolean m_setHideButton;

    public DataboxMultiGraphics() {
        this(false, false, false);
    }

    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor) {
        this(defaultLocked, canChooseColor, false);
    }

    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor, boolean displayDoubleYAxis) {
        super((displayDoubleYAxis ? DataboxType.DataboxMultiGraphicsDoubleYAxis : DataboxType.DataboxMultiGraphics), DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = defaultLocked;
        m_canChooseColor = canChooseColor;
        m_displayDoubleYAxis = displayDoubleYAxis;
        // Name of this databox
        if (m_displayDoubleYAxis) {
            m_typeName = "Graphic Linear Plot (two axis)";
            m_description = "Display two sets of data as linear plot using 2 axis";
        } else {
            m_typeName = "Graphic Linear Plot";
            m_description = "Display data as linear plot";
        }

        // Register Possible in parameters
        if (!m_displayDoubleYAxis) {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            registerInParameter(inParameter);
        } else {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            inParameter.addParameter(SecondAxisTableModelInterface.class, true);
            inParameter.addParameter(PTMPeptideInstance.class, true);//for DataBoxPTMPeptide[XXXX].propagate
            inParameter.addParameter(DMasterQuantPeptide.class, true);//for DataBoxXicPeptideSet.propagate
            registerInParameter(inParameter);
        }
        m_setHideButton = false;
    }

    protected void setHideButton(boolean h) {
        this.m_setHideButton = h;
    }

    protected boolean isDoubleYAxis() {
        return m_displayDoubleYAxis;
    }

    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor, m_displayDoubleYAxis, m_setHideButton);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        List<ExtendedTableModelInterface> dataModelInterfaceSet1 = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        //obtain select index in next bloc
        if ((dataModelInterfaceSet1 != null && !dataModelInterfaceSet1.isEmpty() && (dataModelInterfaceSet1.get(0) instanceof XICComparePeptideTableModel))) {
            m_selectedIndex = (ArrayList<Integer>) m_previousDataBox.getData(false, Integer.class);
            //m_logger.debug("multiGraphic, get Selected Index : {}, data size {}", selectedIndex, dataModelInterfaceSet1.size());
            if (!m_hideSelection && m_selectedIndex != null && !m_selectedIndex.isEmpty()) {//***### here, set selection
                XICComparePeptideTableModel data;
                for (int i = 0; i < dataModelInterfaceSet1.size(); i++) {
                    data = (XICComparePeptideTableModel) dataModelInterfaceSet1.get(i);
                    if (m_selectedIndex.contains(i)) {
                        data.setSelected(true);
                    }
                }
            }
        }
        
        Boolean keepZoom = (Boolean) m_previousDataBox.getExtraData(ExtendedTableModelInterface.class); // True during xic extraction
        if (keepZoom == null) {
            keepZoom = Boolean.FALSE;
        }

        //final List<ExtendedTableModelInterface> dataModelInterfaceSet1 = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL = (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        SecondAxisTableModelInterface dataModelInterfaceSet2 = m_displayDoubleYAxis ? (SecondAxisTableModelInterface) m_previousDataBox.getData(false, SecondAxisTableModelInterface.class, true) : null;

        boolean valueUnchanged = Objects.equals(dataModelInterfaceSet1, m_plotValues) && Objects.equals(crossSelectionInterfaceL, m_crossSelectionValues) && Objects.equals(dataModelInterfaceSet2, m_plotSecondAxisValues);
        if (valueUnchanged) {
            return;
        }
        m_plotValues = dataModelInterfaceSet1;
        m_crossSelectionValues = crossSelectionInterfaceL;
        m_plotSecondAxisValues = dataModelInterfaceSet2;
        if (m_plotValues != null) {
            ((MultiGraphicsPanel) getDataBoxPanelInterface()).setData(m_plotValues, m_crossSelectionValues, m_plotSecondAxisValues, keepZoom);
        }
    }

    @Override
    public void setEntryData(Object data) {
        m_plotValues = (List<ExtendedTableModelInterface>) data;
        ((MultiGraphicsPanel) getDataBoxPanelInterface()).setData(m_plotValues, null);
    }

    public void hideSelection(boolean hide) {
        m_hideSelection = hide;
        if ((m_plotValues != null && !m_plotValues.isEmpty() && (m_plotValues.get(0) instanceof XICComparePeptideTableModel))) {
            if (m_selectedIndex != null && !m_selectedIndex.isEmpty()) {//***### here, set selection
                XICComparePeptideTableModel data;
                for (int i = 0; i < m_plotValues.size(); i++) {
                    data = (XICComparePeptideTableModel) m_plotValues.get(i);
                    if (m_selectedIndex.contains(i)) {
                        data.setSelected(!hide);
                    }
                }
            }
        }
        ((MultiGraphicsPanel) getDataBoxPanelInterface()).setData(m_plotValues, m_crossSelectionValues, m_plotSecondAxisValues, false);
    }

}
