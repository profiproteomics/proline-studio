package fr.proline.studio.markerbar;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog to set the name of an annotation
 * @author jm235353
 */
public class AnnotationMarkerDialog extends DefaultDialog {
 
    private static AnnotationMarkerDialog singletonDialog = null;
    
    private JTextField descriptionTextfield = null;
    
    public static AnnotationMarkerDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new AnnotationMarkerDialog(parent);
        }

        singletonDialog.reinit(); 
        
        return singletonDialog;
    }


    
    private AnnotationMarkerDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        
        setTitle("Add Annotation");

        setButtonVisible(BUTTON_DEFAULT, false);

        initInternalPanel();


    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(getRenamePanel(), c);


        setInternalComponent(internalPanel);
    }
    

    
    private JPanel getRenamePanel() {
        JPanel renamePanel = new JPanel();
        renamePanel.setLayout(new GridBagLayout());
        renamePanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel renameLabel = new JLabel("Description :");
        descriptionTextfield = new JTextField(30);

        c.gridx = 0;
        c.gridy = 0;
        renamePanel.add(renameLabel, c);


        c.gridx++;
        c.weightx = 1.0;
        renamePanel.add(descriptionTextfield, c);

        return renamePanel;
    }
    
    private void reinit() {
        descriptionTextfield.setText("");
    }
    
    @Override
    protected boolean okCalled() {
        
        String name = descriptionTextfield.getText();
       
        if (name.isEmpty()) {
            setStatus(true, "You must fill the description.");
            highlight(descriptionTextfield);
            return false;
        }

        return true;
    }
    
    
    public void setDescriptionField(String name) {
        descriptionTextfield.setText(name);
    }
    
    public String getDescriptionField() {
        return descriptionTextfield.getText();
    }

}
