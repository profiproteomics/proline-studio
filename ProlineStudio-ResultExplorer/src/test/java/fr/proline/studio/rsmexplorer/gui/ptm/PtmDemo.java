///*
// * @cea 
// * http://www.profiproteomics.fr
// * create date: 30 nov. 2018
// */
//package fr.proline.studio.rsmexplorer.gui.ptm;
//
//import fr.proline.studio.rsmexplorer.gui.ptm.mark.PtmMarkCtrl;
//import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
//import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
//
//import java.awt.*;
//import java.util.ArrayList;
//import javax.swing.JFrame;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//import javax.swing.UnsupportedLookAndFeelException;
//import javax.swing.WindowConstants;
//import org.openide.util.Exceptions;
//
///**
// *
// * @author Karine XUE
// */
//public class PtmDemo extends JFrame {
//
//    private static final int AJUSTE_GAP = 3;
//    private DataMgrPtm _dataMgr;
//    private Object _projetId;
//    private PanelPtmDraw _paintArea;
//    private PtmMarkCtrl _ctrlMark;
//    private ProteinSequenceCtrl _ctrlSequence;
//    private PeptideAreaCtrl _ctrlPeptideArea;
//    private int _beginBestFit;
//
//    public PtmDemo() {
//        super();
//
//        Font font = ViewSetting.FONT_SEQUENCE;
//
//        _dataMgr = new DataMgrPtm();
//        _ctrlMark = new PtmMarkCtrl();
//        _ctrlSequence = new ProteinSequenceCtrl();
//        _ctrlPeptideArea = new PeptideAreaCtrl();
//        _paintArea = new PanelPtmDraw(null, _ctrlMark, _ctrlSequence, _ctrlPeptideArea);
//        initComponents();
//    }
//
//    /**
//     * ToolBar at left, PainArea at Center
//     */
//    private void initComponents() {
//        setLayout(new BorderLayout());
//
//        this.add(_paintArea, BorderLayout.CENTER);
//        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//    }
//
//    public void setData() {
//        System.out.println("set data");
//        this._beginBestFit = Integer.MAX_VALUE;
//        _dataMgr.setData(createTestPtmSitePeptideList());
//        //_dataMgr.setData(null);
//        if (_dataMgr.getPtmSitePeptideList() == null) {
//            this._paintArea.clean();
//        } else {
//            this._ctrlSequence.setData(_dataMgr.getProteinSequence());
//            this._ctrlMark.setData(_dataMgr.getAllPtmSite2Mark());
//            this._ctrlPeptideArea.setData(_dataMgr.getPtmSitePeptideList());
//            int ajustedLocation = _dataMgr.getBeginBestFit();
//            ajustedLocation = this._beginBestFit;
//            System.out.println("set data sequence->");
//            this._paintArea.setIsDataLoaded(true);
//            this._paintArea.setRowCount(this._dataMgr.getRowCount());
//            this._paintArea.setSequenceLength(_dataMgr.getProteinSequence().length());
//            this._paintArea.setAjustedLocation(ajustedLocation);
//            //valueChanged();
//        }
//        this.repaint();
//
//    }
//
//    /**
//     * test use
//     *
//     * @return
//     */
//    private ArrayList<PtmSitePeptide> createTestPtmSitePeptideList() {
//        ArrayList<PtmSitePeptide> ptmPepList = new ArrayList();
//        long pepId, pepMatchId;
//        Character aa;
//        String sequence;
//        String ptm;
//        int locationInPep, locationInProtein;
//        float ptmProbability;
//        
//        ptmPepList.add(createPtmSitePeptide((long) 726166, (long) 1410188, "SLAVCEESSARSGGESHQDQESIHLQLSSF", "Carbamidomethyl (C5); Phospho (S22)", 22, 88, 0.2f));
//        ptmPepList.add(createPtmSitePeptide((long) 739245, (long) 1410186, "SLAVCEESSARSGGESHQDQESIHLQLSSF", "Carbamidomethyl (C5); Phospho (S29)", 29, 95, 0.2f));
//        ptmPepList.add(createPtmSitePeptide((long) 742368, (long) 1352587, "MGMSTXLYKHLNGQCCCLSHFSIAVK", "Acetyl (Protein N-term); Oxidation (M1); Oxidation (M3); Carbamidomethyl (C15); Carbamidomethyl (C16); Carbamidomethyl (C17); Phospho (S19); Phospho (S22)", 0, 0, 1.0f));
//        ptmPepList.add(createPtmSitePeptide((long) 708966, (long) 1370831, "AVSRPGPHSMRYFETAVSR", "Phospho (S3); Oxidation (M10); Phospho (S18)", 3, 23, 0.33333334f));
//        ptmPepList.add(createPtmSitePeptide((long) 731713, (long) 1431186, "AVSRPGPHSMRYFETAVSR", "Phospho (S3); Oxidation (M10); Phospho (T15)", 3, 23, 0.52358395f));
//        ptmPepList.add(createPtmSitePeptide((long) 728619, (long) 1370833, "AVSRPGPHSMRYFETAVSR", "Phospho (S3); Phospho (S9); Oxidation (M10)", 3, 23, 0.33333334f));
//
//        ptmPepList.add(createPtmSitePeptide((long) 768837, (long) 1336169, "PTPLREGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (S10); Phospho (Y14); Phospho (T16); Oxidation (M17); Phospho (S22); Carbamidomethyl (C30)", 17, 332, 0.16666667f));
//        ptmPepList.add(createPtmSitePeptide((long) 750087, (long) 1336170, "PTPLREGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (S10); Phospho (Y13); Phospho (T16); Oxidation (M17); Phospho (S22); Carbamidomethyl (C30)", 17, 332, 0.16666667f));
//        ptmPepList.add(createPtmSitePeptide((long) 720436, (long) 1336173, "PTPLREGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (Y8); Phospho (S10); Phospho (T16); Oxidation (M17); Phospho (S22); Carbamidomethyl (C30)", 17, 332, 0.16666667f));
//        ptmPepList.add(createPtmSitePeptide((long) 753597, (long) 1336842, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (T11); Oxidation (M12); Oxidation (M14); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 781107, (long) 1336843, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (Y9); Oxidation (M12); Oxidation (M14); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 702303, (long) 1336844, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (Y8); Oxidation (M12); Oxidation (M14); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 713671, (long) 1336845, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (S5); Oxidation (M12); Oxidation (M14); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 780148, (long) 1336846, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Oxidation (M4); Phospho (T11); Oxidation (M12); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 754868, (long) 1336847, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Oxidation (M4); Phospho (Y9); Oxidation (M12); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 739864, (long) 1336848, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Oxidation (M4); Phospho (Y8); Oxidation (M12); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 697752, (long) 1336849, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Oxidation (M4); Phospho (S5); Oxidation (M12); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 706817, (long) 1336850, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (Y3); Oxidation (M12); Oxidation (M14); Carbamidomethyl (C25)", 12, 332, 0.1f));
//        ptmPepList.add(createPtmSitePeptide((long) 764022, (long) 1336851, "EGYMSWHYYVTMPMPASTFAIAVGCWTEMK", "Phospho (Y3); Oxidation (M4); Oxidation (M12); Carbamidomethyl (C25)", 12, 332, 0.1f));
//
//        ptmPepList.add(createPtmSitePeptide((long) 726166, (long) 1410188, "SLAVCEESSARSGGESHQDQESIHLQLSSF", "Carbamidomethyl (C5); Phospho (S22)", 22, 1288, 0.2f));
//        ptmPepList.add(createPtmSitePeptide((long) 739245, (long) 1410186, "SLAVCEESSARSGGESHQDQESIHLQLSSF", "Carbamidomethyl (C5); Phospho (S29)", 29, 1295, 0.2f));
//
//        
////        ptmPepList.add(createPtmSitePeptide ((long)132209, (long)520636, "SDNDDIEVESDADKR", "Acetyl (Protein N-term); Phospho (S1)",  0, 1, 1.0f));
////        ptmPepList.add(createPtmSitePeptide ((long)181343, (long)581313, "SDNDDIEVESDADKR", "Acetyl (Protein N-term); Phospho (S1); Phospho (S10)", 0, 1, 1.0f));
//
//        return ptmPepList;
//    }
//
//    /**
//     * used by test demo
//     *
//     * @param pepId
//     * @param pepMatchId
//     * @param aa, specify amino aide
//     * @param sequence
//     * @param ptm
//     * @param locationInPep, the specify aa location in pep
//     * @param locationProtein, the specify aa location in protein
//     * @param ptmProbability, the specify aa ptm probability
//     */
//    public PtmSitePeptide createPtmSitePeptide(long pepId, long pepMatchId, String sequence, String ptm, int locationInPep, int locationProtein, float ptmProbability) {
//        boolean isNTermAt1 = false;
//        if (locationProtein == 1 && locationInPep == 0) {
//            isNTermAt1 = true;
//        }
//        ArrayList<PtmSiteAA> ptmList = new ArrayList<PtmSiteAA>();
//        int distance = locationProtein - locationInPep;
//
//        String[] ptmSet = ptm.split(";");
//
//        for (String element : ptmSet) {
//            PtmSiteAA pSite = new PtmSiteAA(element.trim(), distance, isNTermAt1);
//            pSite.setProbability(ptmProbability);
//
//            ptmList.add(pSite);
//        }
//        PtmSitePeptide result = new PtmSitePeptide(pepId, pepMatchId, sequence, ptmList, distance, true);
//
//        if (isNTermAt1 && distance == 1) {
//            this._beginBestFit = 0;//modify adjustLocation is N-Termini is at 1
//        }
//        if (this._beginBestFit > result.getBeginInProtein()) {
//            this._beginBestFit = result.getBeginInProtein();
//        }
//        return result;
//    }
//
//    public static void main(String[] args) {
//
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    PtmDemo test = new PtmDemo();
//                    test.setSize(1200, 250);
//
//                    test.setData();;
//                    test.setVisible(true);
//                }
//            });
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//    }
//}
