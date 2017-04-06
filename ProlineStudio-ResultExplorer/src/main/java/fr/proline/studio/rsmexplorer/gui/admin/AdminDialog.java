package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 *
 * @author JM235353
 */
public class AdminDialog extends DefaultDialog  {

    private static AdminDialog m_singletonDialog = null;


    public static AdminDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AdminDialog(parent);
        }
        return m_singletonDialog;
    }

    public AdminDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Admin Dialog");

        setSize(new Dimension(640, 480));
        setMinimumSize(new Dimension(400, 360));
        setResizable(true);

        //setDocumentationSuffix("h.eb8nfjv41vkz"); //JPM.TODO

        setButtonVisible(BUTTON_CANCEL, false);
        setButtonName(BUTTON_OK, "Close");
        //setStatusVisible(true);

        initInternalPanel();

    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JTabbedPane tabbedPane = new JTabbedPane(); 
        

        JPanel userAccountsPanel = new UserAccountsPanel(this);
        JPanel peaklistSoftwarePanel = new PeaklistSoftwarePanel(this);
        JPanel instrumentConfigPanel = new InstrumentConfigPanel();

        tabbedPane.add("User Accounts", userAccountsPanel);
        tabbedPane.add("Peaklist Softwares", peaklistSoftwarePanel);
        // JPM.TODO : not done for the moment tabbedPane.add("Instrument Config", instrumentConfigPanel);
        

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(tabbedPane, c);

        setInternalComponent(internalPanel);

    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }


    @Override
    protected boolean okCalled() {
        return true;
    }



}
