package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JM235353
 */
public class JCheckBoxListPanel extends JPanel {
    
    private JCheckBoxList m_checkBoxList = null;
    private JCheckBox m_selectAllCheckBox = null;
    
    public JCheckBoxListPanel(JCheckBoxList checkBoxList, boolean showSelectAll) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        
        m_checkBoxList = checkBoxList;
        
        if (showSelectAll) {
            m_selectAllCheckBox = new JCheckBox("Select / Unselect All", IconManager.getIcon(IconManager.IconType.SELECTED_CHECKBOXES));
            m_selectAllCheckBox.setBackground(Color.white);
            m_selectAllCheckBox.setFocusPainted(false);

            m_selectAllCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (m_selectAllCheckBox.isSelected()) {
                        m_checkBoxList.selectAll();
                    } else {
                        m_checkBoxList.unselectAll();
                    }
                    repaint();
                }

            });
        }
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);
        
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        JScrollPane scrollPane = new JScrollPane(m_checkBoxList);
        add(scrollPane, c);
        
        if (showSelectAll) {
            c.gridy++;
            c.weighty = 0;
            add(m_selectAllCheckBox, c);
        }
  
    }
    
    public JCheckBoxList getCheckBoxList() {
        return m_checkBoxList;
    }
}
