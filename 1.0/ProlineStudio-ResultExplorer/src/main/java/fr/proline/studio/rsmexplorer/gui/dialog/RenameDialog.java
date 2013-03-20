package fr.proline.studio.rsmexplorer.gui.dialog;

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
 * Dialog to Rename a Project or a DataSet
 * @author jm235353
 */
public class RenameDialog extends DefaultDialog {
 
    private static RenameDialog singletonDialog = null;
    
    private JTextField renameTextfield = null;
    
    public static RenameDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new RenameDialog(parent);
        }

        return singletonDialog;
    }

    private RenameDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        
        setTitle("Rename");

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

        JLabel renameLabel = new JLabel("New Name :");
        renameTextfield = new JTextField(30);

        c.gridx = 0;
        c.gridy = 0;
        renamePanel.add(renameLabel, c);


        c.gridx++;
        c.weightx = 1.0;
        renamePanel.add(renameTextfield, c);

        return renamePanel;
    }
    
    
    @Override
    protected boolean okCalled() {
        
        String name = renameTextfield.getText();
       
        if (name.isEmpty()) {
            setStatus(true, "You must fill the new name.");
            highlight(renameTextfield);
            return false;
        }

        return true;
    }
    
    
    public void setNameField(String name) {
        renameTextfield.setText(name);
    }
    
    public String getNameField() {
        return renameTextfield.getText();
    }

}
