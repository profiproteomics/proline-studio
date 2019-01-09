package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PtmMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
//public class PanelPeptidesPTMSiteGraphic extends PeptidesPTMSiteTablePanel {
public class PanelPeptidesPTMSiteGraphic extends JPanel implements DataBoxPanelInterface, SplittedPanelContainer.UserActions {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    protected DataMgrPtm _dataMgr;

    protected Object _projetId;
    protected PanelPtmDraw _paintArea;
    protected PtmMarkCtrl _ctrlMark;
    protected ProteinSequenceCtrl _ctrlSequence;
    protected PeptideAreaCtrl _ctrlPeptideArea;

    protected AbstractDataBox m_dataBox;
    protected PTMSite m_currentPTMSite = null;
    protected DPeptideInstance m_currentPepInst = null;

    public PanelPeptidesPTMSiteGraphic() {
        //super(false);
        super();
        _dataMgr = new DataMgrPtm();
        _ctrlMark = new PtmMarkCtrl();
        _ctrlSequence = new ProteinSequenceCtrl();
        _ctrlPeptideArea = new PeptideAreaCtrl();
        _paintArea = new PanelPtmDraw(this, _ctrlMark, _ctrlSequence, _ctrlPeptideArea);
        initComponents();

    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());

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

    // @Override
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {
        //logger.debug(this.getClass().getName() + " setData ->");
        if ((peptidesPTMSite == m_currentPTMSite) && (pepInst == m_currentPepInst)) {
            return;
        }
        _dataMgr.setData(peptidesPTMSite);
        if (peptidesPTMSite == null) {
            this._paintArea.clean();
        } else {
            this.m_currentPTMSite = peptidesPTMSite;
            this.m_currentPepInst = pepInst;
            this._ctrlSequence.setData(_dataMgr.getProteinSequence());
            this._ctrlMark.setData(_dataMgr.getAllPtmMarks());
            this._ctrlPeptideArea.setData(_dataMgr.getPTMSitePeptideInstances());
            int ajustedLocation = _dataMgr.getBeginBestFit();
            this._ctrlPeptideArea.setSelectedIndex(0);
            this._paintArea.setIsDataLoaded(true);
            this._paintArea.setRowCount(this._dataMgr.getRowCount());
            this._paintArea.setSequenceLength(_dataMgr.getProteinSequence().length());
            this._paintArea.setAjustedLocation(ajustedLocation);
            valueChanged(0);//first selected is 0            
        }
        this.repaint();
    }

    public PTMSite getSelectedPTMSite() {
       int selectedRowIndex = this._paintArea.getSelectedPeptideIndex();
       return this._dataMgr.getSelectedPTMSite(selectedRowIndex);
    }
    
    /**
     * used to set next Data Box
     */
    public DPeptideInstance getSelectedPeptideInstance() {
        int selectedRowIndex = this._paintArea.getSelectedPeptideIndex();
        //logger.debug(this.getClass().getName() + "getSelectedPeptideInstance " + " selectRowIndex: " + selectedRowIndex);
        return this._dataMgr.getSelectedPeptideInstance(selectedRowIndex);
    }
    /**
     * when selected petptied change, change next databox and table selected row
     *
     * @param i
     */
    protected void valueChanged(int i) {
        //logger.debug(this.getClass().getName() + "valueChanged selectRow="+ i);
        _dataMgr.setSelectedPeptideInstance(i);
        m_dataBox.propagateDataChanged(PTMSite.class);
        this.repaint();
    }

    /**
     * useful between table-graphic ptm site panel
     * @param i 
     */
    public void setSelectedRow(int i) {
        this._ctrlPeptideArea.setSelectedIndex(i);
        this._paintArea.setSelectedIndex(i);
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    @Override
    public void addSingleValue(Object v) {
        //Nothing to Do
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
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
