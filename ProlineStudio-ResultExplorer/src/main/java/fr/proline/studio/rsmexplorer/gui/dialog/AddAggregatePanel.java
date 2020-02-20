package fr.proline.studio.rsmexplorer.gui.dialog;


import fr.proline.core.orm.uds.Aggregation;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Panel used in Add Aggregate Dialog
 * @author JM235353
 */
public class AddAggregatePanel extends javax.swing.JPanel {

    private static Aggregation.ChildNature[] m_aggregateTypes = { Aggregation.ChildNature.BIOLOGICAL_GROUP, Aggregation.ChildNature.BIOLOGICAL_SAMPLE, Aggregation.ChildNature.SAMPLE_ANALYSIS, Aggregation.ChildNature.OTHER };
    
    /**
     * Creates new form AddAggregatePanel
     */
    public AddAggregatePanel() {
        initComponents();
        
        // if the user click on the spinner, it is enabled
        // and the multipleAggregateCheckBox is automatically checked
        initAutoEnableForSpinner();
      
        initDefaults();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        parameterPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextfield = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeCombobox = new JComboBox(m_aggregateTypes);
        typeCombobox.setRenderer(new DataSetComboboxRenderer());
        multipleAggregateCheckBox = new javax.swing.JCheckBox();
        nbAggregateSpinner = new javax.swing.JSpinner();

        parameterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AddAggregatePanel.class, "AddAggregatePanel.parameterPanel.border.title"))); // NOI18N

        nameLabel.setText(org.openide.util.NbBundle.getMessage(AddAggregatePanel.class, "AddAggregatePanel.nameLabel.text")); // NOI18N

        nameTextfield.setText(org.openide.util.NbBundle.getMessage(AddAggregatePanel.class, "AddAggregatePanel.nameTextfield.text")); // NOI18N

        typeLabel.setText(org.openide.util.NbBundle.getMessage(AddAggregatePanel.class, "AddAggregatePanel.typeLabel.text")); // NOI18N

        multipleAggregateCheckBox.setText(org.openide.util.NbBundle.getMessage(AddAggregatePanel.class, "AddAggregatePanel.multipleAggregateCheckBox.text")); // NOI18N
        multipleAggregateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multipleAggregateCheckBoxActionPerformed(evt);
            }
        });

        nbAggregateSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, 100, 1));

        javax.swing.GroupLayout parameterPanelLayout = new javax.swing.GroupLayout(parameterPanel);
        parameterPanel.setLayout(parameterPanelLayout);
        parameterPanelLayout.setHorizontalGroup(
            parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(parameterPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(parameterPanelLayout.createSequentialGroup()
                                .addGap(0, 124, Short.MAX_VALUE)
                                .addComponent(multipleAggregateCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(nbAggregateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(parameterPanelLayout.createSequentialGroup()
                                .addComponent(typeLabel)
                                .addGap(8, 8, 8)
                                .addComponent(typeCombobox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(parameterPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nameTextfield)))
                .addContainerGap())
        );
        parameterPanelLayout.setVerticalGroup(
            parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(parameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multipleAggregateCheckBox)
                    .addComponent(nbAggregateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parameterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parameterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void multipleAggregateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multipleAggregateCheckBoxActionPerformed
        boolean isSelected = multipleAggregateCheckBox.isSelected();
        nbAggregateSpinner.setEnabled(isSelected);
    }//GEN-LAST:event_multipleAggregateCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox multipleAggregateCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextfield;
    private javax.swing.JSpinner nbAggregateSpinner;
    private javax.swing.JPanel parameterPanel;
    private javax.swing.JComboBox typeCombobox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables

    protected String getAggregateName() {
        return nameTextfield.getText();
    }
    protected JTextField getNameTextfield() {
        return nameTextfield;
    }
    
    protected int getNbAggregates() {
        if (multipleAggregateCheckBox.isSelected()) {
            return ((Integer)nbAggregateSpinner.getValue()).intValue();
        }
        return 1;
    }
    
    protected Aggregation.ChildNature getAggregateType() {
        return ((Aggregation.ChildNature)typeCombobox.getSelectedItem());
    }

    protected final void initDefaults() {

        nameTextfield.setText("");
        
        typeCombobox.setSelectedItem(Aggregation.ChildNature.BIOLOGICAL_SAMPLE);
        
        multipleAggregateCheckBox.setSelected(false);
        nbAggregateSpinner.setValue(new Integer(2));
        nbAggregateSpinner.setEnabled(false);
             
    }

    public void reinitialize() {
        nameTextfield.setText("");
    }
    
    public class DataSetComboboxRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
            Aggregation.ChildNature selectedType = ((Aggregation.ChildNature) value);
            
            switch (selectedType) {
                case BIOLOGICAL_GROUP:
                    l.setText("Biological Group");
                    //l.setIcon(IconManager.getIcon(IconManager.IconType.VIAL));
                    break;
                case BIOLOGICAL_SAMPLE:
                    l.setText("Biological Sample");
                    //l.setIcon(IconManager.getIcon(IconManager.IconType.GEL));
                    break;
                case SAMPLE_ANALYSIS:
                    l.setText("Sample Analysis");
                    //l.setIcon(IconManager.getIcon(IconManager.IconType.GEL));
                    break;
                case OTHER:
                    l.setText("Other");
                    //l.setIcon(null);
                    break;
            } //JPM.TODO : icons

            return l;
        } 
    }
    
    private void initAutoEnableForSpinner() {
        spinnerMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!multipleAggregateCheckBox.isSelected()) {
                    nbAggregateSpinner.setEnabled(true);
                    multipleAggregateCheckBox.setSelected(true);
                }
            }
        };
        
        initAutoEnableForSpinnerImpl(nbAggregateSpinner);
        
        
    }
    private void initAutoEnableForSpinnerImpl(Container c) {
        int nbSubComponent = c.getComponentCount();
        for (int i=0;i<nbSubComponent;i++) {
            Component componentCur = c.getComponent(i);
            componentCur.addMouseListener(spinnerMouseAdapter);
            if (componentCur instanceof Container) {
                initAutoEnableForSpinnerImpl((Container)componentCur);
            }
        }
    }
    private MouseAdapter spinnerMouseAdapter = null;
}