/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 *
 * @author CB205360
 */
public class ResidueMethodParamsPanel extends AbstractParamsPanel {

    private final QuantitationMethod m_quantMethod;
    private final List<PtmSpecificity> m_ptms;
    private Map<QuantitationLabel, List<JComboBox<PtmSpecificity>>> m_comboBoxesByTags; 
            
    public ResidueMethodParamsPanel(QuantitationMethod method, List<PtmSpecificity> ptms) {
        m_quantMethod = method;
        m_ptms = ptms;
        
        setLayout(new BorderLayout());
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Residue Labeling parameters"));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        
        m_comboBoxesByTags = new HashMap<>();  
        
        for (QuantitationLabel label : m_quantMethod.getLabels()) {
            JLabel jLabel = new JLabel(label.getName()+":");
            final JComboBox<PtmSpecificity> combo1 = new JComboBox<>(m_ptms.toArray(new PtmSpecificity[0]));
            final JComboBox<PtmSpecificity> combo2 = new JComboBox<>(m_ptms.toArray(new PtmSpecificity[0]));
            List<JComboBox<PtmSpecificity>> list = new ArrayList<JComboBox<PtmSpecificity>>();
            list.add(combo1);
            list.add(combo2);
            m_comboBoxesByTags.put(label, list);
            combo1.setRenderer(new PtmSpecificityRenderer());
            combo2.setRenderer(new PtmSpecificityRenderer());
            panel.add(jLabel,c);
            c.gridx++;
            
            JButton button = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
            button.setMargin(new java.awt.Insets(2, 2, 2, 2));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                  boolean visibility = !combo1.isVisible();
                  combo1.setVisible(visibility);
                  if (visibility) {
                      ((JButton)e.getSource()).setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                  } else {
                      ((JButton)e.getSource()).setIcon(IconManager.getIcon(IconManager.IconType.PLUS));
                  }
                }
            });
            panel.add(button, c);
            c.gridx++;
            panel.add(combo1,c);
            c.gridx++;
            button = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
            button.setMargin(new java.awt.Insets(2, 2, 2, 2));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                  boolean visibility = !combo2.isVisible();
                  combo2.setVisible(visibility);
                  if (visibility) {
                      ((JButton)e.getSource()).setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                  } else {
                      ((JButton)e.getSource()).setIcon(IconManager.getIcon(IconManager.IconType.PLUS));
                  }

                }
            });
            panel.add(button,c);
            c.gridx++;
            panel.add(combo2,c);
            combo2.setVisible(false);
            c.gridx++;
            c.weightx = 500; // stupid value but needed because remaining space is shared between the 2 last components
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(Box.createHorizontalGlue(),c);
            
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
        }
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 4;
        panel.add(Box.createGlue(), c);
        return panel;
    }
    
    
    @Override
    public Map<String, Object> getQuantParams() {
        Map<String,Object> params = new HashMap<>();
        List<Object> tags = new ArrayList<Object>();
        for (Map.Entry<QuantitationLabel, List<JComboBox<PtmSpecificity>>> e : m_comboBoxesByTags.entrySet()) {
            Map<String,Object> labelParams = new HashMap<>();
            List<Long> ptmsIds = new ArrayList<Long>();
            for (JComboBox<PtmSpecificity> combo : e.getValue()) {
                if (combo.isVisible() && (combo.getSelectedItem() != null)) {
                    ptmsIds.add( ((PtmSpecificity)combo.getSelectedItem()).getId() );
                }
            }
            labelParams.put("tag_id", e.getKey().getId());
            labelParams.put("ptm_specificity_ids", ptmsIds);
            tags.add(labelParams);
        }
        params.put("tags", tags);
        return params;
    }

    @Override
    public void setQuantParams(Map<String, Object> quantParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

class PtmSpecificityRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        StringBuilder builder = new StringBuilder(((PtmSpecificity)value).getPtm().getShortName());
        builder.append('(').append(((PtmSpecificity)value).getResidue()).append(')');
        return super.getListCellRendererComponent(list, builder.toString(), index, isSelected, cellHasFocus); 
    }
    
            
            
}