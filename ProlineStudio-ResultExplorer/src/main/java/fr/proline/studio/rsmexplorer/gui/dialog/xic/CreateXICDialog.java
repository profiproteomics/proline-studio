package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.JScrollPane;


/**
 *
 * @author JM235353
 */
public class CreateXICDialog extends DefaultDialog {

    private static final int STEP_PANEL_DEFINE_GROUPS = 0;
    private static final int STEP_PANEL_MODIFY_GROUPS = 1;
    private static final int STEP_PANEL_DEFINE_SAMPLE_ANALYSIS = 2;
    private static final int STEP_PANEL_DEFINE_QUANT_PARAMS = 3;
    private int m_step = STEP_PANEL_DEFINE_GROUPS;
    private static CreateXICDialog m_singletonDialog = null;
    private RSMNode finalXICDesignNode = null;

    public static CreateXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new CreateXICDialog(parent);
        }


        return m_singletonDialog;
    }

    private CreateXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("XIC Quantitation Wizard");

        setHelpURL(null); //JPM.TODO

        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);

        init();

    }
    
    private void init(){
        
       setButtonName(DefaultDialog.BUTTON_OK, "Next");
       setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
 
       setInternalComponent(DefineBioGroupsPanel.getDefineBioGroupsPanel());
       //TODO: reinitialize wizard panels...
        
    }

    public final void reinit(){
        init();
         m_step = STEP_PANEL_DEFINE_GROUPS; 
    }
    
    public RSMNode getDesignRSMNode(){
        return finalXICDesignNode;
    }
    
    public Map<String, Object> getQuantiParameters(){
        return DefineQuantParamsPanel.getDefineQuantPanel().getQuantParams();
    }
    
    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_DEFINE_GROUPS) {
            
            // check values
            DefineBioGroupsPanel defineBioGroupsPanel = DefineBioGroupsPanel.getDefineBioGroupsPanel();
            String quantitationName = defineBioGroupsPanel.getQuantitationName();
            if (quantitationName.length() == 0) {

                setStatus(true, "You must fill the Quantitation Name");
                highlight(defineBioGroupsPanel.getQuantitationNameTextField());
                return false;

            }
            
            String groupPrefix = defineBioGroupsPanel.getGroupPrefix();
            if (groupPrefix.length() == 0) {

                setStatus(true, "You must fill the Group Prefix");
                highlight(defineBioGroupsPanel.getGroupPrefixTextField());
                return false;

            }
            
            String samplePrefix = defineBioGroupsPanel.getSamplePrefix();
            if (samplePrefix.length() == 0) {

                setStatus(true, "You must fill the Sample Prefix");
                highlight(defineBioGroupsPanel.getSamplePrefixTextField());
                return false;

            }
            

            
            int nbGroups = defineBioGroupsPanel.getGroupNumber();

            int nbSamples = defineBioGroupsPanel.getSampleNumber();


            JScrollPane scrollPane = new JScrollPane();
            ModifyBioGroupsPanel definePanel = ModifyBioGroupsPanel.getDefinePanel(nbGroups, groupPrefix, nbSamples, samplePrefix);
            scrollPane.setViewportView(definePanel);

            replaceInternaleComponent(scrollPane);

            setResizable(true);
            setSize(new Dimension(600, 500));

            revalidate();
            repaint();

            m_step = STEP_PANEL_MODIFY_GROUPS;

            return false;
        } else if (m_step == STEP_PANEL_MODIFY_GROUPS) {

            // check values
                 
            
            JScrollPane scrollPane = new JScrollPane();
            String quantitationName = DefineBioGroupsPanel.getDefineBioGroupsPanel().getQuantitationName();
            finalXICDesignNode = ModifyBioGroupsPanel.getDefinePanel().generateTreeNodes(quantitationName);
            SetSampleAnalysisPanel defineSampleAnalysisPanel = SetSampleAnalysisPanel.getDialog(finalXICDesignNode);
            scrollPane.setViewportView(defineSampleAnalysisPanel);

            replaceInternaleComponent(scrollPane);


            revalidate();
            repaint();
            
            m_step = STEP_PANEL_DEFINE_SAMPLE_ANALYSIS;
            
            return false;
        } else if (m_step == STEP_PANEL_DEFINE_SAMPLE_ANALYSIS)  {  // STEP_PANEL_DEFINE_SAMPLE_ANALYSIS
             // check values
            if (finalXICDesignNode == null) {

                setStatus(true, "You must correctly define your experimental design");
                highlight(SetSampleAnalysisPanel.getDialog());
                return false;

            }

             Enumeration xicGrps = finalXICDesignNode.children();
            //Iterate over Groups
            while (xicGrps.hasMoreElements()) {
                RSMNode grpNode = (RSMNode) xicGrps.nextElement();
                //Iterate over Samples
                Enumeration grpSpls = grpNode.children();
                while (grpSpls.hasMoreElements()) {
                    RSMNode splNode = (RSMNode) grpSpls.nextElement();
                    if (splNode.getChildCount() < 1) {
                        setStatus(true, "You must specify at least one identifications summary for each sample.");
                        highlight(SetSampleAnalysisPanel.getDialog());
                        return false;
                    }
                }
            }

            // change to ok button
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));
 
            //Update panel
            
            JScrollPane scrollPane = new JScrollPane();            
            DefineQuantParamsPanel quantPanel =  DefineQuantParamsPanel.getDefineQuantPanel();
            scrollPane.setViewportView(quantPanel);
            scrollPane.createVerticalScrollBar();
            
            replaceInternaleComponent(quantPanel);

            revalidate();
            repaint();
            
            m_step = STEP_PANEL_DEFINE_QUANT_PARAMS;
            
            return false;
        } else { //STEP_PANEL_DEFINE_QUANT_PARAMS
            // check values TODO    
            return true;
        }

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    
}
