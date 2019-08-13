/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 19 déc. 2018
 */
package fr.proline.studio.pattern;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.ptm.PTMPeptidesGraphicView;
import java.util.List;

/**
 * This class is very similaire as DataBoxPTMSitePeptides
 *
 * @author Karine XUE
 */
public class DataBoxPTMPeptidesGraphic extends AbstractDataBoxPTMPeptides {

    /**
     * Create a DataBoxPTMPeptidesGraphic : graphical view of PTMPeptideInstances. 
    */
    public DataBoxPTMPeptidesGraphic() {
        super(DataboxType.DataBoxPTMPeptidesGraphic, DataboxStyle.STYLE_RSM);
        m_displayAllPepMatches = false;
        m_isXICResult = false;
        
        // Name of this databox
        m_typeName = "Graphical Peptides PTMs info";
        m_description = "PTMs information of Peptides of a [Group] Modification Site using graphical display.";
        
        // Register Possible in parameters          
        super.registerParameters();        
    }
            
    @Override
    public void createPanel() {
        PTMPeptidesGraphicView p = new PTMPeptidesGraphicView();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    
    @Override
    public void updateData() {
        
        PTMPeptidesGraphicView graphicView = (PTMPeptidesGraphicView) getDataBoxPanelInterface();
      
        if (m_ptmPepInstances == null || m_ptmPepInstances.isEmpty()) {
            graphicView.setData(null);
            return;
        }

     
        final List<PTMSite> notLoadedPtmSite = getNotLoadedPTMSite();
        
        if (notLoadedPtmSite.isEmpty()) {
            resetPrevPTMTaskId();
            graphicView.setData(m_ptmPepInstances); 
            propagateDataChanged(PTMPeptideInstance.class);                
            
        } else
            loadPtmSite(notLoadedPtmSite);
 
    }
    
    @Override
    protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance){
        ((PTMPeptidesGraphicView) getDataBoxPanelInterface()).setSelectedPTMPeptide(pepInstance);
     }

    @Override
    protected PTMPeptideInstance getSelectedPTMPeptide(){
         return ((PTMPeptidesGraphicView) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
     }

    @Override
    protected void loadXicAndPropagate() {
        //Should not occur : m_isXICResult = false;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
