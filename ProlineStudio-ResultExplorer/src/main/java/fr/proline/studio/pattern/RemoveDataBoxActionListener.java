package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action to remove the last DataBox
 * @author JM235353
 */
public class RemoveDataBoxActionListener implements ActionListener {

    private SplittedPanelContainer m_splittedPanel;
    private AbstractDataBox m_previousDatabox;

    public RemoveDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
        m_splittedPanel = splittedPanel;
        m_previousDatabox = previousDatabox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        m_previousDatabox.setNextDataBox(null);
        m_splittedPanel.removeLastPanel();


    }
}