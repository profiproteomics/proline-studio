package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSiteTablePanel;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PanelPeptidesPTMSiteAll extends JPanel implements DataBoxPanelInterface {
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private AbstractDataBox _dataBox;
    
    private PTMSite _currentPeptidesPTMSite = null;
    private DPeptideInstance _currentPepInst = null;
    
    private JTabbedPane _panelTabbed;
    private PeptidesPTMSiteTablePanel _panelTable;
    private PanelPeptidesPTMSiteGraphic _panelGraphic;
    


    public PanelPeptidesPTMSiteAll() {
        // m_peptideData = new PeptidesOfPTMSiteTableModel();
        initComponents();
    }
 
    private void initComponents() {
        this.setLayout(new BorderLayout());
     // @param viewAll if true display all Peptides Matches of all Peptide
     // Instance for this PTMSite. If false, display only best PeptideMatch of
     // all Peptide Instance for this PTMSite.
        _panelTable = new PeptidesPTMSiteTablePanel(false);
        _panelGraphic = new PanelPeptidesPTMSiteGraphic();
        _panelTabbed = new JTabbedPane();
        _panelTabbed.addTab("Graphic View", null, _panelGraphic,
                "graphic presentation");
        _panelTabbed.addTab("Table View", null, _panelTable,
                "table presentation");
        _panelTabbed.setSelectedComponent(_panelGraphic);
        this.add(_panelTabbed);
    }
    
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {
       //logger.debug("PanelPeptidesPTMSiteAll setData ->");
        if ((peptidesPTMSite == _currentPeptidesPTMSite) && (pepInst == _currentPepInst)) {
            return;
        }
        _currentPeptidesPTMSite = peptidesPTMSite;
        _currentPepInst = pepInst;
        
        _panelTable.setData(peptidesPTMSite, pepInst);
        _panelGraphic.setData(peptidesPTMSite, pepInst);
    }

    /**
     * getSelected for the next data Box
     *
     * @todo synchronise between graphic + table Panel
     * @return
     */
    public PTMSite getSelectedPTMSite() {
        return ((PeptidesPTMSiteTablePanel) _panelTabbed.getSelectedComponent()).getSelectedPTMSite();
    }
    
    public DPeptideMatch getSelectedPeptideMatchSite() {
        return ((PeptidesPTMSiteTablePanel) _panelTabbed.getSelectedComponent()).getSelectedPeptideMatchSite();
        
    }
    
    public DPeptideInstance getSelectedPeptideInstance() {
        return ((PeptidesPTMSiteTablePanel)this._panelTabbed.getSelectedComponent()).getSelectedPeptideInstance();
    }
    
    //@todo 
    private int getSelectedTableModelRow() {
        //@todo return ((PeptidesPTMSiteTablePanel) this._panelTabbed.getSelectedComponent()).getSelectedTableModelRow();
        return this._panelTable.getSelectedTableModelRow();
    }
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        _dataBox = dataBox;
        
        _panelTable.setName(this.getName());
        _panelTable.setDataBox(this._dataBox);
        
        _panelGraphic.setName(this.getName());
        _panelGraphic.setDataBox(this._dataBox);
    }
    
    @Override
    public AbstractDataBox getDataBox() {
        return _dataBox;
    }
    
    /**
     * in AbstractDataBox 
     * protected void setDataBoxPanelInterface(DataBoxPanelInterface panelInterface) {
     *    m_panel = panelInterface;
     *    if (m_panel != null) {
     *      getDataBoxPanelInterface().addSingleValue(m_projectId);
     *  }
     * }
     * so the v is projetId
     * @param v 
     */
    @Override
    public void addSingleValue(Object v) {
        _panelGraphic.addSingleValue(v);
        _panelTable.addSingleValue(v);
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return _dataBox.getRemoveAction(splittedPanel);
    }
    
    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return _dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return _dataBox.getSaveAction(splittedPanel);
    }
    
    @Override
    public void setLoading(int id) {
        //Nothing to Do
    }
    
    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to Do
    }
    
    @Override
    public void setLoaded(int id) {
        //Nothing to Do
    }
    
}
