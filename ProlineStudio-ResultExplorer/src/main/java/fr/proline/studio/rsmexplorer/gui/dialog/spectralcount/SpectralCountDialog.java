package fr.proline.studio.rsmexplorer.gui.dialog.spectralcount;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;


/**
 *
 * @author JM235353
 */
public class SpectralCountDialog extends DefaultDialog {

    private static final int STEP_PANEL_DEFINE_NAME = 0;
    private static final int STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES = 1;
    private int m_step = STEP_PANEL_DEFINE_NAME;

    private IdentificationTree m_tree = null;


    public SpectralCountDialog(Window parent, IdentificationTree childTree) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_tree = childTree;
        
        setTitle("Spectral Count Wizard");

        setResizable(true);

        
        setHelpURL(null); //JPM.TODO

        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));

        SpectralCountNamePanel namePanel = SpectralCountNamePanel.getSpectralCountNamePanel();
        namePanel.reinit();
        setInternalComponent(namePanel);


    }

    public String getSpectralCountName() {
        return SpectralCountNamePanel.getSpectralCountNamePanel().getSpectralCountName();
    }
    
    public String getSpectralCountDescription() {
        return SpectralCountNamePanel.getSpectralCountNamePanel().getSpectralCountDescription();
    }
    
    public ArrayList<DataSetNode> getSelectedRSMDSNodeList() {
        return TreeSelectionPanel.getTreeSelectionPanel().getSelectedRSMDSNodeList();
    }
    
    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_DEFINE_NAME) {
            
            // check values
            SpectralCountNamePanel spectralCountNamePanel = SpectralCountNamePanel.getSpectralCountNamePanel();
            String name = spectralCountNamePanel.getSpectralCountName();
            if (name.length() == 0) {

                setStatus(true, "You must fill the Spectral Count Name");
                highlight(spectralCountNamePanel.getNameTextField());
                return false;

            }
            
            String description = spectralCountNamePanel.getSpectralCountDescription();
            /**if (description.length() == 0) {

                setStatus(true, "You must fill the Spectral Count Description");
                highlight(spectralCountNamePanel.getDescriptionTextArea());
                return false;

            }*/ // no check on description

            // change to ok button
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

            JScrollPane scrollPane = new JScrollPane();
            TreeSelectionPanel treeSelectionPanel = TreeSelectionPanel.getTreeSelectionPanel(m_tree);
            scrollPane.setViewportView(treeSelectionPanel);

            replaceInternaleComponent(scrollPane);


            revalidate();
            repaint();

            m_step = STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES;

            
            
            return false;
        } else { // m_step == STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES)

            // check values
            TreePath[] paths = TreeSelectionPanel.getTreeSelectionPanel().getSelectionPaths();

            if (paths == null || paths.length == 0) {
                setStatus(true, "You must at least select one Identification Summary");
                highlight(TreeSelectionPanel.getTreeSelectionPanel().getTree());
                return false;
            }



            return true;
        }

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    
}
