package fr.proline.studio.rsmexplorer.gui.dialog.spectralcount;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class SpectralCountDialog extends DefaultDialog {

    private static final int STEP_PANEL_DEFINE_NAME = 0;
    private static final int STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES = 1;
    private static final int STEP_PANEL_SELECT_WEIGHT_REF_IDENT_SUMMARIES = 2;
    private int m_step = STEP_PANEL_DEFINE_NAME;

    private IdentificationTree m_tree = null;
    private ArrayList<DataSetNode> m_identRSMs = null;
    private ArrayList<DataSetNode> m_weightRefIdentRSMs = null;

    public SpectralCountDialog(Window parent, IdentificationTree childTree) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_tree = childTree;

        setTitle("Spectral Count Wizard");

        setResizable(true);

        setDocumentationSuffix("id.1egqt2p");

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));

        SpectralCountNamePanel namePanel = SpectralCountNamePanel.getSpectralCountNamePanel();
        this.setHelpHeader("<html><b>Step 1:</b> Define spectral count name and description.</html>", null);
        namePanel.reinit();
        setInternalComponent(namePanel);

    }

    public String getSpectralCountName() {
        return SpectralCountNamePanel.getSpectralCountNamePanel().getSpectralCountName();
    }

    public String getSpectralCountDescription() {
        return SpectralCountNamePanel.getSpectralCountNamePanel().getSpectralCountDescription();
    }
    
//    public Boolean useSpectralCountV2_0() {
//        return SpectralCountNamePanel.getSpectralCountNamePanel().useVersion2_0();
//    }

    public ArrayList<DataSetNode> getSelectedRSMDSNodeList() {
        return m_identRSMs;
    }

    public ArrayList<DataSetNode> getSelectedWeightRSMDSNodeList() {
        return m_weightRefIdentRSMs;
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

            TreeSelectionPanel treeSelectionPanel = TreeSelectionPanel.getTreeSelectionPanel(m_tree);
            this.setHelpHeader("<html><b>Step 2:</b> Select Identification Summaries.</html>", 
                    "Select the Identification Summaries for which Spectral Count will be calculated. The common list of protein sets and peptide specificity will be calculated at parent level, from which spectral count has been run.");
            replaceInternalComponent(treeSelectionPanel);

            revalidate();
            repaint();

            m_step = STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES;

            return false;
        } else if (m_step == STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES) { // m_step == STEP_PANEL_SELECT_IDENTIFICATION_SUMMARIES)

            // check values
            TreePath[] paths = TreeSelectionPanel.getTreeSelectionPanel().getSelectionPaths();

            if (paths == null || paths.length == 0) {
                setStatus(true, "You must at least select one Identification Summary");
                highlight(TreeSelectionPanel.getTreeSelectionPanel().getTree());
                return false;
            }

            m_identRSMs = TreeSelectionPanel.getTreeSelectionPanel().getSelectedRSMDSNodeList();

            // change to ok button
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

            //Reinit tree selection for Ref RSM weight computation    
            m_tree.revertSelectionSetDisabled(true);
            m_tree.setSelection(new ArrayList<ResultSummary>());


            TreeSelectionPanel treeSelectionPanel = TreeSelectionPanel.getTreeSelectionPanel(m_tree);
            setHelpHeader( "<html><b>Step 3:</b> Select Weight Computation Identification Summaries.</html>", "Select Datasets (and associated identification summaries) in the hierarchy where shared PSM weight will be defined. The calculated weight will then be applied to Identification Summaries previously selected, the nearest parent will be used as reference for PSM weight.");
            replaceInternalComponent(treeSelectionPanel);

            revalidate();
            repaint();

            m_step = STEP_PANEL_SELECT_WEIGHT_REF_IDENT_SUMMARIES;

            return false;
        } else { // m_step == STEP_PANEL_SELECT_WEIGHT_REF_IDENT_SUMMARIES)

            // check values
            TreePath[] paths = TreeSelectionPanel.getTreeSelectionPanel().getSelectionPaths();

            if (paths == null || paths.length == 0) {
                setStatus(true, "You must at least select one Identification Summary for weight computation");
                highlight(TreeSelectionPanel.getTreeSelectionPanel().getTree());
                return false;
            }

            m_weightRefIdentRSMs = TreeSelectionPanel.getTreeSelectionPanel().getSelectedRSMDSNodeList();
            m_tree.setSelection(new ArrayList<ResultSummary>());
            m_tree.revertSelectionSetDisabled(false);
            return true;
        }

    }

    @Override
    protected boolean cancelCalled() {
        //Reset previous tree selection/disabled data if needed
        m_tree.setSelection(new ArrayList<ResultSummary>());
        m_tree.revertSelectionSetDisabled(false);

        return true;
    }

}
