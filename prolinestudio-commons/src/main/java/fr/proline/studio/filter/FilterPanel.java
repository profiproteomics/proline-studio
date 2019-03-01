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
 *
 * @author JM235353
 */
public class FilterPanel extends JPanel {

    private Filter[] m_filters;

    private JPanel m_filterSelectedPanel;
    private JComboBox m_filterComboBox;
    private JButton m_addFilterButton;

    private DefaultDialog m_dialog;

    private int m_nbFiltersSelected = 0;

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

                f.setIndex(m_nbFiltersSelected);

                initPrefilterSelectedPanel();

                m_dialog.repack();
            }
        });

    }

    public void setFilters(Filter[] filters) {
        m_filters = filters;

        initPrefilterSelectedPanel();
         m_dialog.repack();
    }
    

    public void initPrefilterSelectedPanel() {
        m_filterSelectedPanel.removeAll();
        m_filterComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        int nbParameters = m_filters.length;

        m_nbFiltersSelected = 0;
        for (int i = 0; i < nbParameters; i++) {

            final Filter currentFilter = m_filters[i];

            c.gridy = currentFilter.getIndex();

            if (currentFilter.isDefined()) {

                m_nbFiltersSelected++;
                
                c.gridx = 0;
                if (currentFilter.getIndex() != 0) {
                    m_filterSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    m_filterSelectedPanel.add(new JLabel("   "), c);
                }

                currentFilter.createComponents(m_filterSelectedPanel, c);

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
                        currentFilter.setDefined(false);

                        for (int j = 0; j < nbParameters; j++) {
                            if (m_filters[j].getIndex() > currentFilter.getIndex()) {
                                m_filters[j].setIndex(m_filters[j].getIndex() - 1);
                            }
                        }

                        currentFilter.setIndex(-1);

                        initPrefilterSelectedPanel();

                        m_dialog.repack();
                    }
                });
                m_filterSelectedPanel.add(removeButton, c);
            } else {
                m_filterComboBox.addItem(currentFilter);
            }

        }
    }

}
