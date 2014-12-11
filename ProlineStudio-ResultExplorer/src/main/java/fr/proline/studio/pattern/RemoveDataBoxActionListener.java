package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action to remove the last DataBox
 * @author JM235353
 */
public class RemoveDataBoxActionListener implements ActionListener {

    private final SplittedPanelContainer m_splittedPanel;
    private final AbstractDataBox m_databox;

    public RemoveDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox databox) {
        m_splittedPanel = splittedPanel;
        m_databox = databox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (m_databox.m_previousDataBox != null) {
            m_databox.m_previousDataBox.removeNextDataBox(m_databox);
        }
        m_splittedPanel.removeLastPanel();


    }
}