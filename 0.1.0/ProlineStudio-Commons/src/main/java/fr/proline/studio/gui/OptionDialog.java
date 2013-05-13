package fr.proline.studio.gui;

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
public class OptionDialog extends DefaultDialog {

    private JTextField m_optionTextfield = null;
    
    private String m_message = null;
    private String m_textForTextField = null;
   
    private boolean m_allowEmptyText = false;

    public OptionDialog(Window parent, String title, String message, String textForTextField) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_message = message;
        m_textForTextField = textForTextField;
        
        setTitle(title);

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
        internalPanel.add(getTextfieldPanel(), c);


        setInternalComponent(internalPanel);
    }
    
    private JPanel getTextfieldPanel() {
        JPanel renamePanel = new JPanel();
        renamePanel.setLayout(new GridBagLayout());
        renamePanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        if (m_message != null) {
            c.gridwidth = 2;
            String messageArray[] = m_message.split("\n");
            for (int i=0;i<messageArray.length;i++) {
                JLabel messageLabel = new JLabel(messageArray[i]);
                renamePanel.add(messageLabel, c);
                c.gridy++;
            }
            c.gridwidth = 1;
        }
        
        
        
        JLabel textfieldLabel = new JLabel(m_textForTextField+" :");
        m_optionTextfield = new JTextField(30);

        c.gridx = 0;
        renamePanel.add(textfieldLabel, c);


        c.gridx++;
        c.weightx = 1.0;
        renamePanel.add(m_optionTextfield, c);

        return renamePanel;
    }
    
    
    @Override
    protected boolean okCalled() {
        
        String text = m_optionTextfield.getText();
       
        if ((!m_allowEmptyText) && (text.isEmpty())) {
            setStatus(true, "You must fill the field "+m_textForTextField+".");
            highlight(m_optionTextfield);
            return false;
        }

        return true;
    }
    
    public void setAllowEmptyText(boolean allowEmptyText) {
        m_allowEmptyText = allowEmptyText;
    }
    
    public void setText(String name) {
        m_optionTextfield.setText(name);
        m_optionTextfield.requestFocus();
        m_optionTextfield.setSelectionStart(0);
        m_optionTextfield.setSelectionEnd(name.length());
    }
    
    public String getText() {
        return m_optionTextfield.getText();
    }

}