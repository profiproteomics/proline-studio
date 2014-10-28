/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import java.util.Map;

/**
 * Dialog to compute the quantitation profile
 * @author MB243701
 */
public class QuantProfileXICDialog extends DefaultDialog {
    
    private static QuantProfileXICDialog m_singletonDialog = null;
    
    private QuantProfileXICPanel m_quantProfilePanel;
    
    public static QuantProfileXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new QuantProfileXICDialog(parent);
        }

        return m_singletonDialog;
    }

    private QuantProfileXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("XIC Compute Profile Quantitation");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:xic");

        setResizable(true);
        
        init();
        
    }
    
    private void init() {
        m_quantProfilePanel = new QuantProfileXICPanel();
        
        this.setInternalComponent(m_quantProfilePanel);
    }
    
    public Map<String,Object> getQuantParams(){
        return m_quantProfilePanel.getQuantParams();
    }
}
