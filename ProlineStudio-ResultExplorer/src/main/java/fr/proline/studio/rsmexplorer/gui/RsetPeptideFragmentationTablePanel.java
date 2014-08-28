package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.dto.DPeptideMatch;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author AW
 */
public class RsetPeptideFragmentationTablePanel extends HourglassPanel implements DataBoxPanelInterface {


    private AbstractDataBox m_dataBox;

    private DPeptideMatch m_previousPeptideMatch = null;
    private RsetPeptideFragmentationTable m_fragmentationTable = null;

    public RsetPeptideFragmentationTablePanel() {
        setLayout(new BorderLayout());
        m_fragmentationTable = new RsetPeptideFragmentationTable();
        add(m_fragmentationTable, BorderLayout.CENTER);
    }

    public void setData(DPeptideMatch peptideMatch) {

        if (peptideMatch == m_previousPeptideMatch) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;

        updateFragmentationTable(peptideMatch);
    }

    private void updateFragmentationTable(DPeptideMatch pm) {

 
        m_fragmentationTable.updateFragmentationTable(pm, m_dataBox);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

}
