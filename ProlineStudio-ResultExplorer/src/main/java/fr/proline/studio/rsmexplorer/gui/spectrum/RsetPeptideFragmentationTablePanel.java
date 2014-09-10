package fr.proline.studio.rsmexplorer.gui.spectrum;



import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.export.ExportButton;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author AW
 */
public class RsetPeptideFragmentationTablePanel extends HourglassPanel implements DataBoxPanelInterface {


    private AbstractDataBox m_dataBox;

    private RsetPeptideFragmentationTable m_fragmentationTable = null;
    private HideFragmentsTableIntensityButton m_hideFragIntensityButton = null;
    
    public RsetPeptideFragmentationTablePanel() {
        setLayout(new BorderLayout());
        
        m_fragmentationTable = new RsetPeptideFragmentationTable();
        
        JToolBar toolbar = createToolbar();
        JPanel internalPanel = createInternalPanel();
        
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

    
    public void setData(DPeptideMatch peptideMatch, PeptideFragmentationData petpideFragmentationData) {
        if(petpideFragmentationData.isEmpty) {
        	return;
        }
    	m_fragmentationTable.setData(peptideMatch, petpideFragmentationData);
        
        // update hideFragIntensityButton button
        boolean isEnable =  m_hideFragIntensityButton.isEnabled();
        boolean enable = (petpideFragmentationData!=null);
        if (isEnable ^ enable) {
            m_hideFragIntensityButton.setEnabled(enable);
        }
        
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
