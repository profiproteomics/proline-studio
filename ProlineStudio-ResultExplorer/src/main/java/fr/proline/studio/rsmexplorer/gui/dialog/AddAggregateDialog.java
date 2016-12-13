package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 * Dialog to add an aggregate
 * @author JM235353
 */
public class AddAggregateDialog extends DefaultDialog {
    
    
    private static AddAggregateDialog m_singletonDialog = null;
   
    private AddAggregatePanel m_aggregatePanel = null;
   
    public static AddAggregateDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AddAggregateDialog(parent);
        }

        m_singletonDialog.m_aggregatePanel.reinitialize();
        
        return m_singletonDialog;
    }
    

    
    private AddAggregateDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Add Dataset");

        try {
            setHelpURL(new File(".").getCanonicalPath()+File.separatorChar+"Documentation"+File.separatorChar+"Proline_UserGuide_1.4RC1.docx.html#id.2p2csry");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        setButtonVisible(BUTTON_DEFAULT, true);
        
        m_aggregatePanel = new AddAggregatePanel();

        setInternalComponent(m_aggregatePanel);
    }
    
    @Override
    protected boolean okCalled() {
        
        String name = m_aggregatePanel.getAggregateName();
       
        if (name.isEmpty()) {
            setStatus(true, "You must fill the dataset name.");
            highlight(m_aggregatePanel.getNameTextfield());
            return false;
        }

        return true;
    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
       
    @Override
    protected boolean defaultCalled() {
        m_aggregatePanel.initDefaults();

        return false;
    }
    
    public String getAggregateName() {
        return m_aggregatePanel.getAggregateName();
    }
    
    public int getNbAggregates() {
       return m_aggregatePanel.getNbAggregates();
    }
    
    public Aggregation.ChildNature getAggregateType() {
        return m_aggregatePanel.getAggregateType();
    }
    
}
