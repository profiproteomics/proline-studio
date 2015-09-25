package fr.proline.studio.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author JM235353
 */
public class CheckboxTabPanel extends JPanel {

    private String m_sheetId = null;

    private JCheckBox m_checkBox;
    private JLabel m_label;
    private JTabbedPane m_tabbedPane;

    public CheckboxTabPanel(JTabbedPane tabbedPane, String title, String sheetId) {
        setLayout(new FlowLayout());

        setOpaque(false);

        m_tabbedPane = tabbedPane;

        m_sheetId = sheetId;

        m_checkBox = new JCheckBox();
        m_checkBox.setOpaque(false);
        m_label = new JLabel(title);

        add(m_checkBox);
        add(m_label);

        final CheckboxTabPanel _this = this;
        m_checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = m_checkBox.isSelected();

                //JPM.WART : big wart due to the fact that dnd changes the index of tabs
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
                    m_tabbedPane.setEnabledAt(indexCur, isSelected);
                }

                if (isSelected) {
                    m_label.setForeground(Color.black);
                } else {
                    m_label.setForeground(Color.lightGray);
                }

            }
        });

    }

    public void setSelected(boolean isSelected) {
        m_checkBox.setSelected(isSelected);
        if (isSelected) {
            m_label.setForeground(Color.black);
        } else {
            m_label.setForeground(Color.lightGray);
        }
    }

    public boolean isSelected() {
        return m_checkBox.isSelected();
    }

    public String getText() {
        return m_label.getText();
    }

    public void setText(String text) {
        m_label.setText(text);
    }

    public String getSheetId() {
        return m_sheetId;
    }
    
    public void setSheetId(String sheetId) {
        m_sheetId = sheetId;
    }

}
