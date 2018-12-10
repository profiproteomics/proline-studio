package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSiteTablePanel;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PtmMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JToolBar;

/**
 *
 * @author Karine XUE
 */
public class PanelPeptidesPTMSiteGraphic extends PeptidesPTMSiteTablePanel {

    protected DataMgrPtm _dataMgr;

    protected Object _projetId;
    protected PanelPtmDraw _paintArea;
    protected PtmMarkCtrl _ctrlMark;
    protected ProteinSequenceCtrl _ctrlSequence;
    protected PeptideAreaCtrl _ctrlPeptideArea;

    public PanelPeptidesPTMSiteGraphic() {
        super(false);
        _dataMgr = new DataMgrPtm();
        _ctrlMark = new PtmMarkCtrl();
        _ctrlSequence = new ProteinSequenceCtrl();
        _ctrlPeptideArea = new PeptideAreaCtrl();
        initComponents();

    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        _paintArea = new PanelPtmDraw(_ctrlMark, _ctrlSequence, _ctrlPeptideArea);
        _paintArea.setSize(WIDTH - 30, HEIGHT);
        JToolBar toolbar = initToolbar();
        this.add(toolbar, BorderLayout.WEST);
        this.add(_paintArea, BorderLayout.CENTER);

    }

    /**
     * Export image button in ToolBar
     *
     * @return
     */
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", this);
        toolbar.add(exportImageButton);

        return toolbar;
    }

    @Override
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {
        _dataMgr.setData(peptidesPTMSite);
        this._ctrlSequence.setData(_dataMgr.getProteinSequence());
        this._ctrlMark.setData(_dataMgr.getAllPtmSite2Mark());
        this._ctrlPeptideArea.setData(_dataMgr.getPtmSitePeptideList());
        int ajustedLocation = _dataMgr.getBeginBestFit();
        this._paintArea.setIsDataLoaded(true);
        this._paintArea.setRowCount(this._dataMgr.getRowCount());
        this._paintArea.setSequenceLength(_dataMgr.getProteinSequence().length());
        this._paintArea.setAjustedLocation(ajustedLocation);
        this.repaint();
    }

     /**
     * test use
     */
    public void setData(ArrayList<PtmSitePeptide> list) {
        _dataMgr.setData(list);
        this._ctrlSequence.setData(_dataMgr.getProteinSequence());
        this._ctrlMark.setData(_dataMgr.getAllPtmSite2Mark());
        this._ctrlPeptideArea.setData(_dataMgr.getPtmSitePeptideList());
        int ajustedLocation = _dataMgr.getBeginBestFit();
        this._paintArea.setIsDataLoaded(true);
        this._paintArea.setAjustedLocation(ajustedLocation);
        this.repaint();
    }

    /**
     * @todo @return
     */
    public DPeptideInstance getSelectedPeptideInstance() {

        // Retrieve Selected Row
        int selectedRow = getSelectedTableModelRow();
//
//        //return m_peptideData.getSelectedPeptideInstance(selectedRow);
//        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
//        // Retrieve ProteinPTMSite selected
//        PeptidesOfPTMSiteTableModel tableModel = (PeptidesOfPTMSiteTableModel) compoundTableModel.getBaseModel();
//
//        return tableModel.getSelectedPeptideInstance(selectedRow);
        return null;
    }

}
