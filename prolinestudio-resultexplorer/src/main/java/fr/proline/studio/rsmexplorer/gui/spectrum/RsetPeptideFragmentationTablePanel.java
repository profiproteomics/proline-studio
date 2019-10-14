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
package fr.proline.studio.rsmexplorer.gui.spectrum;



import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.export.ExportButton;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author AW
 */
public class RsetPeptideFragmentationTablePanel extends HourglassPanel implements DataBoxPanelInterface, SplittedPanelContainer.ReactiveTabbedComponent {

    private boolean m_isDisplayed = true;

    private AbstractDataBox m_dataBox;

    private RsetPeptideFragmentationTable m_fragmentationTable = null;
    private HideFragmentsTableIntensityButton m_hideFragIntensityButton = null;
    private final JTextArea m_descriptionTA;
    
    public RsetPeptideFragmentationTablePanel() {
        setLayout(new BorderLayout());
        
        m_fragmentationTable = new RsetPeptideFragmentationTable();
        m_descriptionTA = new JTextArea();
        m_descriptionTA.setEditable(false);
        m_descriptionTA.setOpaque(false);
        
        JToolBar toolbar = createToolbar();
        JPanel internalPanel = createInternalPanel();

        add(m_descriptionTA, BorderLayout.NORTH);
        add(internalPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
    }

        
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton m_exportButton = new ExportButton(null, "Fragmentation Table", m_fragmentationTable);
        toolbar.add(m_exportButton); //JPM.TODO

        m_hideFragIntensityButton = new HideFragmentsTableIntensityButton(m_fragmentationTable, false);
        m_hideFragIntensityButton.setEnabled(false);
        toolbar.add(m_hideFragIntensityButton);
        return toolbar;
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_fragmentationTable);
        m_fragmentationTable.setFillsViewportHeight(true);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(scrollPane, c);
        
        return internalPanel;
    }

    
    public void setData(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData) {
        m_fragmentationTable.setData(peptideMatch, peptideFragmentationData);
        
        // update hideFragIntensityButton button
        boolean isEnable =  m_hideFragIntensityButton.isEnabled();
        boolean enable = (peptideFragmentationData!=null);
        if (isEnable ^ enable) {
            m_hideFragIntensityButton.setEnabled(enable);
        }
        if ((peptideFragmentationData != null) && peptideFragmentationData.getNeutralLossStatus() != null)
          m_descriptionTA.setText("Sum of Neutral Losses : "+peptideFragmentationData.getNeutralLossStatus());
        else 
          m_descriptionTA.setText("");
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

    @Override
    public void setShowed(boolean showed) {
        if (showed == m_isDisplayed) {
            return;
        }
        m_isDisplayed = showed;
    }

    @Override
    public boolean isShowed() {
        return m_isDisplayed;
    }

}
