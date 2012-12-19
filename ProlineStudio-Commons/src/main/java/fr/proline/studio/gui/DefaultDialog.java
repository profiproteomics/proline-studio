package fr.proline.studio.gui;

import java.awt.*;
import javax.swing.*;
import org.openide.util.ImageUtilities;

/**
 * All Dialogs of this application must extend this class
 * which offers a common behaviour
 * @author JM235353
 */
public class DefaultDialog extends javax.swing.JDialog {

    public static final int BUTTON_OK = 0;
    public static final int BUTTON_CANCEL = 1;
    public static final int BUTTON_DEFAULT = 2;
    
    private JPanel internalPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JButton defaultButton;

    
    private boolean firstDisplay = true;
    
    protected int buttonClicked = -1;
    
    private static final ImageIcon okIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/tick.png", false);
    private static final ImageIcon cancelIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/tick-red.png", false);
    private static final ImageIcon defaultIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/arrow-circle.png", false);
    
    /**
     * Creates new form AbstractDialog
     */
    public DefaultDialog() {
        this(null, Dialog.ModalityType.MODELESS);
    }
    public DefaultDialog(Window parent) {
        this(parent, Dialog.ModalityType.APPLICATION_MODAL);
    }
    public DefaultDialog(Window parent, Dialog.ModalityType modalityType) {
        super(parent, modalityType);
        initComponents(); 
    }

    @Override
    public void setVisible(boolean v) {
        if (firstDisplay) {
            pack();
            firstDisplay = false;
        }
        super.setVisible(v);
    }

    public int getButtonClicked() {
        return buttonClicked;
    }
    
    
    protected void setInternalComponent(Component component) {

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        internalPanel.add(component, c);
 
    }

    protected void setButtonVisible(int buttonId, boolean visible) {
        switch (buttonId) {
            case BUTTON_OK:
                okButton.setVisible(visible);
                break;
            case BUTTON_CANCEL:
                cancelButton.setVisible(visible);
                break;
            case BUTTON_DEFAULT:
                defaultButton.setVisible(visible);
                break;
                
        }
    }
    
    protected void setButtonName(int buttonId, String name) {
        switch (buttonId) {
            case BUTTON_OK:
                okButton.setText(name);
                break;
            case BUTTON_CANCEL:
                cancelButton.setText(name);
                break;
            case BUTTON_DEFAULT:
                defaultButton.setText(name);
                break;
        }
    }
    
    protected void setButtonIcon(int buttonId, Icon icon) {
        switch (buttonId) {
            case BUTTON_OK:
                okButton.setIcon(icon);
                break;
            case BUTTON_CANCEL:
                cancelButton.setIcon(icon);
                break;
            case BUTTON_DEFAULT:
                defaultButton.setIcon(icon);
                break;
        }
    }
    
    protected boolean cancelCalled() {
        return true;
    }
    
    protected boolean okCalled() {
        return true;
    }
    
    protected boolean defaultCalled() {
        return false;
    }
    

    
    
    private void initComponents() {

        setResizable(false);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        add(internalPanel, c);
        
        JPanel buttonPanel = createButtonPanel();
        c.gridy++;
        c.weighty = 0;
        add(buttonPanel, c);
        
    }
  
    private JPanel createButtonPanel() {
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        
        okButton = new JButton(okIcon);
        cancelButton = new JButton(cancelIcon);
        defaultButton = new JButton(defaultIcon);
        
        buttonPanel.add(defaultButton, c);
        
        c.gridx++;
        c.weightx = 1;
        buttonPanel.add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        buttonPanel.add(okButton, c);
        
        c.gridx++;
        buttonPanel.add(cancelButton, c);
        
        okButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.cancelButton.text"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        defaultButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.defaultButton.text"));
        defaultButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed();
            }
        });


        return buttonPanel;
    }

    private void cancelButtonActionPerformed() {                                             
        if (cancelCalled()) {
            buttonClicked = BUTTON_CANCEL;
            setVisible(false);
        }
    }                                            

    private void okButtonActionPerformed() {                                         
        if (okCalled()) {
            buttonClicked = BUTTON_OK;
            setVisible(false);
        }
    }                                        

    private void defaultButtonActionPerformed() {                                         
        if (defaultCalled()) {
            buttonClicked = BUTTON_DEFAULT;
            setVisible(false);
        }
    } 


}
