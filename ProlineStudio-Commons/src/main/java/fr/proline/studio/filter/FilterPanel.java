package fr.proline.studio.filter;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Dynamic filter panel
 * @author JM235353
 */
public class FilterPanel extends JPanel {
    
    private Filter[] m_filters;
    
    private JPanel m_filterSelectedPanel;
    private JComboBox m_filterComboBox;
    private JButton m_addFilterButton;
    
    private DefaultDialog m_dialog;
    
    public FilterPanel(DefaultDialog d) {
        
        m_dialog = d;
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(" Filter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_filterSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        add(m_filterSelectedPanel, c);


        m_filterComboBox = new JComboBox();
        m_filterComboBox.setRenderer(new FilterComboboxRenderer()); 
        m_addFilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addFilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        add(m_filterComboBox, c);

        c.gridx++;
        c.weightx = 0;
        add(m_addFilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        add(Box.createHorizontalBox(), c);


        m_addFilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Filter f = (Filter) m_filterComboBox.getSelectedItem();
                if (f == null) {
                    return;
                }
                f.setDefined(true);
                m_filterComboBox.removeItem(f);

                initPrefilterSelectedPanel();
                
                m_dialog.repack();
            }
        });

    }
    

    
    public void setFilers(Filter[] filters) {
        m_filters = filters;

        initPrefilterSelectedPanel();
    }
    
    
    public void initPrefilterSelectedPanel() {
        m_filterSelectedPanel.removeAll();
        m_filterComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        boolean putAndInFront = false;
        int nbParameters = m_filters.length;
        for (int i = 0; i < nbParameters; i++) {
            final Filter f = m_filters[i];

            if ((f != null) && (f.isDefined())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_filterSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_filterSelectedPanel.add(new JLabel("   "), c);
                }

                f.createComponents(m_filterSelectedPanel, c);

                c.weightx = 0;
                c.gridwidth = 1;
                c.weighty = 0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        f.setDefined(false);
                        initPrefilterSelectedPanel();
                        
                        m_dialog.repack();
                    }
                });
                m_filterSelectedPanel.add(removeButton, c);

                c.gridy++;
            } else {
                m_filterComboBox.addItem(f);
            }

        }
    }
    
}
