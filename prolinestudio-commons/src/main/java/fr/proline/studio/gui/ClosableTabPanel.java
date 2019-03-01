package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * Tab for a JTabbedPane that the user can close through a button
 * 
 * @author JM235353
 */
public class ClosableTabPanel extends JPanel {

    private JTabbedPane m_tabbedPane;

    public ClosableTabPanel(JTabbedPane tabbedPane, String title, String sheetId) {
        setLayout(new FlowLayout());

        setOpaque(false);

        m_tabbedPane = tabbedPane;

        JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeButton.setOpaque(false);
        JLabel label = new JLabel(title);

        add(removeButton);
        add(label);

        final ClosableTabPanel _this = this;
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // JPM.WART : big wart due to the fact that dnd changes the index of tabs
                int indexCur = -1;
                int nbTabs = m_tabbedPane.getTabCount();
                for (int i = 0; i < nbTabs; i++) {
                    Component tabComponent = m_tabbedPane.getTabComponentAt(i);
                    if (tabComponent == _this) {
                        indexCur = i;
                        break;
                    }
                }

                if (indexCur != -1) {
                    m_tabbedPane.removeTabAt(indexCur);
                }


            }
        });

    }



}
