package fr.proline.studio.rsmexplorer.gui.dialog;


import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */
public class AddFolderDialog  extends DefaultDialog {

    private static AddFolderDialog m_singletonDialog = null;

    private JTextField m_folderNameTextField = null;
    
    public static AddFolderDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AddFolderDialog(parent);
        }

        m_singletonDialog.reinit(); 

        return m_singletonDialog;
    }

    private AddFolderDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Add Folder");

        // JPM.TODO setDocumentationSuffix("id.2p2csry");


        setInternalComponent(createFolderPanel());
    }

    private void reinit() {
        m_folderNameTextField.setText("");
    }
    
    private JPanel createFolderPanel() {
        JPanel folderPanel = new JPanel(new java.awt.GridBagLayout());
        folderPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        JLabel folderLabel = new JLabel("Folder Name:");
        m_folderNameTextField = new JTextField(30);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        folderPanel.add(folderLabel, c);
        
        c.weightx = 1.0;
        c.gridx++;
        folderPanel.add(m_folderNameTextField, c);
        
        return folderPanel;
    }

    @Override
    protected boolean okCalled() {

        String name = getFolderName();

        if (name.isEmpty()) {
            setStatus(true, "You must fill the folde name.");
            highlight(m_folderNameTextField);
            return false;
        }

        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }


    public String getFolderName() {
        return m_folderNameTextField.getText().trim();
    }


}
