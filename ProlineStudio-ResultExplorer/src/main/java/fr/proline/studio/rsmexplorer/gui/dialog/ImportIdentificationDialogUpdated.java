package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * Dialog used to start the import of identifications by selecting multiple
 * files, a parser and its parameters. The fields are filled with last used
 * parameters. If they not exist, these fields are filled with default values.
 *
 * @author jm235353
 */
public class ImportIdentificationDialogUpdated extends DefaultDialog implements DefaultDialogInterface {

    private static ImportIdentificationDialogUpdated m_singletonDialog = null;
    private ImportIdentificationPanel m_identificationPanel;

    public static ImportIdentificationDialogUpdated getDialog(Window parent/*, long projectId*/) {

        if (m_singletonDialog == null) {
            m_singletonDialog = new ImportIdentificationDialogUpdated(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }

    private ImportIdentificationDialogUpdated(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_identificationPanel = new ImportIdentificationPanel(parent);

        setTitle("Import Search Results");

        try {
            setHelpURL(new File(".").getCanonicalPath() + File.separatorChar + "Documentation" + File.separatorChar + "Proline_UserGuide_1.4RC1.docx.html#id.147n2zr");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        setInternalComponent(m_identificationPanel);
        m_identificationPanel.setDialog(this);

    }


    private void reinitialize() {
        m_identificationPanel.reinitializePanel();
    }

    @Override
    protected boolean okCalled() {
        return m_identificationPanel.okTriggered();
    }

    @Override
    protected boolean cancelCalled() {
        return m_identificationPanel.cancelTriggered();
    }

    @Override
    protected boolean saveCalled() {
        return m_identificationPanel.saveTriggered();
    }

    @Override
    protected boolean loadCalled() {
        return m_identificationPanel.loadTriggered();
    }

    @Override
    public void setDialogStatus(boolean b, String s) {
        setStatus(b, s);
    }

    @Override
    public void highlightPanelComponent(Component c) {
        highlight(c);
    }

    @Override
    public void provokeRepack() {
        this.repack();
    }

    @Override
    public void provokeBusy(boolean b) {
        ;
    }

}
