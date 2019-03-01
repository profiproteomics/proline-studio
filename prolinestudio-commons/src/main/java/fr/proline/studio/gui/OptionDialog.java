package fr.proline.studio.gui;

import java.awt.*;
import javax.swing.*;

/**
 * Dialog for a question yes/no or to get a text input
 * 
 * @author jm235353
 */
public class OptionDialog extends DefaultDialog {

    public enum OptionDialogType {
        NO_TEXT,
        TEXTFIELD,
        TEXTAREA
    }
    
    private OptionDialogType m_dialogType;

    private JTextField m_optionTextfield = null;
    private JTextArea m_optionTextArea = null;
    
    private String m_message = null;
    private String m_labelForText = null;
   
    private boolean m_allowEmptyText = false;

    public OptionDialog(Window parent, String title, String message) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_dialogType = OptionDialogType.NO_TEXT;

        setStatusVisible(false);
        
        m_message = message;

        setTitle(title);

        setButtonVisible(BUTTON_HELP, false);


        initInternalPanel();


    }
    
    public OptionDialog(Window parent, String title, String message, String labelForText, OptionDialogType dialogType) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_dialogType = dialogType;
        
        m_message = message;
        m_labelForText = labelForText;
        
        setTitle(title);

        setButtonVisible(BUTTON_HELP, false);


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
        
        
        if (m_dialogType == OptionDialogType.TEXTFIELD) {
            JLabel textfieldLabel = new JLabel(m_labelForText + " :");
            m_optionTextfield = new JTextField(30);

            c.gridx = 0;
            renamePanel.add(textfieldLabel, c);


            c.gridx++;
            c.weightx = 1.0;
            renamePanel.add(m_optionTextfield, c);
        } else if (m_dialogType == OptionDialogType.TEXTAREA) {
            JLabel textfieldLabel = new JLabel(m_labelForText + " :");
            m_optionTextArea = new JTextArea();
            JScrollPane desciptionScrollPane = new JScrollPane(m_optionTextArea) {

                private Dimension preferredSize = new Dimension(360, 200);

                @Override
                public Dimension getPreferredSize() {
                    return preferredSize;
                }
            };

            c.gridx = 0;
            renamePanel.add(textfieldLabel, c);


            c.gridx++;
            c.weightx = 1.0;
            renamePanel.add(desciptionScrollPane, c);
        }

        return renamePanel;
    }
    
    
    @Override
    protected boolean okCalled() {
        
        if (m_optionTextfield != null) {

            String text = m_optionTextfield.getText();

            if ((!m_allowEmptyText) && (text.isEmpty())) {
                setStatus(true, "You must fill the field " + m_labelForText + ".");
                highlight(m_optionTextfield);
                return false;
            }
        } else if (m_optionTextArea != null) {
            
            String text = m_optionTextArea.getText();

            if ((!m_allowEmptyText) && (text.isEmpty())) {
                setStatus(true, "You must fill the field " + m_labelForText + ".");
                highlight(m_optionTextArea);
                return false;
            }
        }
    
        return true;
    }
    
    public void setAllowEmptyText(boolean allowEmptyText) {
        m_allowEmptyText = allowEmptyText;
    }
    
    public void setText(String name) {
        if (m_optionTextfield != null) {
            m_optionTextfield.setText(name);
            m_optionTextfield.requestFocus();
            m_optionTextfield.setSelectionStart(0);
            m_optionTextfield.setSelectionEnd(name.length());
        } else if (m_optionTextArea != null) {
            m_optionTextArea.setText(name);
            m_optionTextArea.requestFocus();
            m_optionTextArea.setSelectionStart(0);
            m_optionTextArea.setSelectionEnd(name.length());
        }
    }
    
    public String getText() {
        if (m_optionTextfield != null) {
            return m_optionTextfield.getText();
        } else if (m_optionTextArea != null) {
            return m_optionTextArea.getText();
        }
        return ""; // should not happen
    }

}