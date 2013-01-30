package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import javax.swing.*;

/**
 * Dialog to create a new Project. The owner will be the current user
 * @author jm235353
 */
public class AddProjectDialog extends DefaultDialog {
    
    private static AddProjectDialog singletonDialog = null;

    private JTextField nameTextField;
    private JTextArea descriptionTextArea;
    
    public static AddProjectDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new AddProjectDialog(parent);
        }

        return singletonDialog;
    }

    private AddProjectDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Add Project");

        setButtonVisible(BUTTON_DEFAULT, false);
        
        setResizable(true);

        initInternalPanel();
    }
    
        private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        JPanel projectParametersPanel = createProjectParametersPanel();



        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(projectParametersPanel, c);


        setInternalComponent(internalPanel);
    }
    
    
    private JPanel createProjectParametersPanel() {
        
        JPanel projectParametersPanel = new JPanel(new GridBagLayout());
        projectParametersPanel.setBorder(BorderFactory.createTitledBorder(" Project Parameters "));
        
        JLabel projectNameLabel = new JLabel("Name :");
        nameTextField = new JTextField(30);
        
        JLabel projectDescriptionLabel = new JLabel("Description :");
        descriptionTextArea = new JTextArea();
        JScrollPane desciptionScrollPane = new JScrollPane(descriptionTextArea) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        projectParametersPanel.add(projectNameLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        projectParametersPanel.add(nameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        projectParametersPanel.add(projectDescriptionLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        projectParametersPanel.add(desciptionScrollPane, c);
        
        return projectParametersPanel;
    }
    
        @Override
    protected boolean okCalled() {


        // check parameters
        if (!checkParameters()) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }
    
    private boolean checkParameters() {
        String name = nameTextField.getText();
        if (name.isEmpty()) {
            setStatus(true, "You must fill the Project Name.");
            highlight(nameTextField);
            return false;
        }
        if (name.length()>250) {
            setStatus(true, "Project Name must not exceed 250 characters.");
            highlight(nameTextField);
            return false;
        }
        
        String description = descriptionTextArea.getText();
        if (description.isEmpty()) {
            setStatus(true, "You must fill the Project Description.");
            highlight(descriptionTextArea);
            return false;
        }
        
        if (description.length()>1000) {
            setStatus(true, "Description must not exceed 1000 characters.");
            highlight(nameTextField);
            return false;
        }
        
        return true;
    }
 
    public String getProjectName() {
        return nameTextField.getText();
    }
    
    public String getProjectDescription() {
        return descriptionTextArea.getText();
    }
    
    
}
