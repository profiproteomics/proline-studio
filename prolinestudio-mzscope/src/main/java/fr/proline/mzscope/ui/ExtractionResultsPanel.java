/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.processing.IAnnotator;
import fr.proline.mzscope.processing.PeakelAnnotator;
import fr.proline.mzscope.ui.model.ExtractionResultsTableModel;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExpansionTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 * 
 */
public class ExtractionResultsPanel extends JPanel {

    final private static Logger logger = LoggerFactory.getLogger(ExtractionResultsPanel.class);
    private final static String LAST_DIR = "mzscope.last.csv.extraction.directory";

    private List<ExtractionResult> m_extractions;
    private ExtractionResultsTableModel m_extractionResultsTableModel;
    private CompoundTableModel m_globalTableModel;
    private ImportedDataTableModel m_importedTableModel;
    private ExtractionResultsTable m_extractionResultsTable;
    private SwingWorker m_extractionWorker;
    
    private IExtractionResultsViewer m_extractionResultsViewer;
    private JFileChooser m_fchooser;
    private MarkerContainerPanel m_markerContainerPanel;
    private ExportButton m_exportButton;
    private FilterButton m_filterButton;
    
    
    public final static int TOOLBAR_ALIGN_VERTICAL = 0;
    public final static int TOOLBAR_ALIGN_HORIZONTAL = 1;
    private int m_toolbarAlign = TOOLBAR_ALIGN_HORIZONTAL;

    public ExtractionResultsPanel(IExtractionResultsViewer extractionResults, int align) {
        this.m_extractionResultsViewer = extractionResults;
        m_toolbarAlign = align;
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(getExtractionResultsTable(), BorderLayout.CENTER);
        if (m_toolbarAlign == TOOLBAR_ALIGN_HORIZONTAL){
            add(getToolBar(), BorderLayout.NORTH);
        }else if (m_toolbarAlign == TOOLBAR_ALIGN_VERTICAL){
            add(getToolBar(), BorderLayout.WEST);
        }

        m_fchooser = new JFileChooser();
        m_fchooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv");
        m_fchooser.setFileFilter(filter);
    }

    private JToolBar getToolBar() {
        JToolBar toolbar = new JToolBar(m_toolbarAlign == TOOLBAR_ALIGN_HORIZONTAL? JToolBar.HORIZONTAL : JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        JButton clearBtn = new JButton();
        clearBtn.setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
        clearBtn.setToolTipText("Clear all values");
        clearBtn.addActionListener((ActionEvent e) -> {
            clearAllValues();
        });
        toolbar.add(clearBtn);
        
        JButton importCSVBtn = new JButton();
        importCSVBtn.setIcon(IconManager.getIcon(IconManager.IconType.TABLE_IMPORT));
        importCSVBtn.setToolTipText("Import m/z values from a csv file...");
        importCSVBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                importCSVExtractions();
            }

        });
        toolbar.add(importCSVBtn);
        
        JButton iRTBtn = new JButton("iRT");
        iRTBtn.setToolTipText("indexed Retention Time Standard");
        iRTBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_importedTableModel = null;
                setExtractions(buildIRTRequest(), null);
            }

        });
        toolbar.add(iRTBtn);
        toolbar.addSeparator();
        JButton extractBtn = new JButton();
        extractBtn.setIcon(IconManager.getIcon(IconManager.IconType.EXECUTE));
        extractBtn.setToolTipText("Start Extractions on all files");
        extractBtn.addActionListener(e -> startExtractions());
        toolbar.add(extractBtn);
        
        toolbar.addSeparator();
        m_exportButton = new ExportButton(((CompoundTableModel) m_extractionResultsTable.getModel()), "Extraction Values", m_extractionResultsTable);
        toolbar.add(m_exportButton);
        m_filterButton = new FilterButton(((CompoundTableModel) m_extractionResultsTable.getModel())) {
            @Override
            protected void filteringDone() { 
                logger.info("Filtering done ! ");
            }
        };
        // TODO : button removed cause Expansion model filtering is not yet working ?  
        toolbar.add(m_filterButton);
        return toolbar;
    }
    
    /**
     * clear all extraction values
     */
    private void clearAllValues(){
        m_extractionResultsTableModel = new ExtractionResultsTableModel();
        m_globalTableModel.setBaseModel(m_extractionResultsTableModel);
        m_globalTableModel.fireTableStructureChanged();
        m_importedTableModel = null;
    }

    private void importCSVExtractions() {
      Preferences prefs = Preferences.userNodeForPackage(this.getClass());
      String directory = prefs.get(LAST_DIR, m_fchooser.getCurrentDirectory().getAbsolutePath());
      m_fchooser.setCurrentDirectory(new File(directory));
      int result = m_fchooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
            File csvFile = m_fchooser.getSelectedFile();
            String fileName = csvFile.getName();
            if (!fileName.endsWith(".csv")){
                JOptionPane.showMessageDialog(this, "The file must be a csv file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            prefs.put(LAST_DIR, csvFile.getParentFile().getAbsolutePath());
            m_importedTableModel = new ImportedDataTableModel();
            ImportedDataTableModel.loadFile(m_importedTableModel, csvFile.getAbsolutePath(), ';', true, false);
            int mzColumnIdx = findColumn(m_importedTableModel, new String[]{"moz", "m/z", "mz"});
            int rtColumnIdx = findColumn(m_importedTableModel, new String[]{"rt", "retention_time", "retention time", "elution_time", "elution time", "time"});
            int zColumnIdx = findColumn(m_importedTableModel, new String[]{"charge", "z"});

            if (mzColumnIdx != -1) {
                List<Double> mzValues = new ArrayList<>();
                List<Double> rtValues = new ArrayList<>();
                List<Integer> zValues = new ArrayList<>();
                for (int k = 0; k < m_importedTableModel.getRowCount(); k++) {
                    mzValues.add((Double) m_importedTableModel.getValueAt(k, mzColumnIdx));
                    rtValues.add((rtColumnIdx != -1) ? (Double) m_importedTableModel.getValueAt(k, rtColumnIdx) : -1.0);
                    zValues.add((zColumnIdx != -1) ? ((Long) m_importedTableModel.getValueAt(k, zColumnIdx)).intValue() : 0);
                }

                float moztol = MzScopePreferences.getInstance().getMzPPMTolerance();
                List<MsnExtractionRequest> list = new ArrayList<>();
                for (int k = 0; k < mzValues.size(); k++) {
                    list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(mzValues.get(k)).setElutionTime(rtValues.get(k).floatValue()).build());
                }
                setExtractions(list, zValues);
            } else {
                JOptionPane.showMessageDialog(this, "No column named \"mz\",\"moz\" or \"m/z\" detected in the imported file.\n Verify the column headers (the column separator must be \";\")", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int findColumn(AbstractTableModel tableModel, String[] alternativeNames) {
        int columnIdx = -1;
        for(String name : alternativeNames) {
            columnIdx = tableModel.findColumn(name);
            if (columnIdx != -1) {
                break;
            }
        }
        return columnIdx;
    }

    private void setExtractions(List<MsnExtractionRequest> extractionRequests, List<Integer> expectedCharge) {
        List<ExtractionResult> results = new ArrayList<>();
        for (int k = 0; k < extractionRequests.size(); k++) {
            MsnExtractionRequest request = extractionRequests.get(k);
            ExtractionResult extractionResult = new ExtractionResult(request, (expectedCharge !=null) ? expectedCharge.get(k) : 0);
            results.add(extractionResult);
        }
        m_extractionResultsTableModel.setExtractions(results);
        if (m_importedTableModel != null) {
            m_globalTableModel.setBaseModel(new ExpansionTableModel(m_importedTableModel, m_extractionResultsTableModel));
            m_globalTableModel.fireTableStructureChanged();
        }
        m_markerContainerPanel.setMaxLineNumber(extractionRequests.size());
        m_extractions = results;
    }

    private void startExtractions() {
        logger.info("startExtractions...");
        if (m_extractions == null){
            logger.info("no extractions!");
            return;
        }
        final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();

        //final ChromatogramAnnotator annotator = new ChromatogramAnnotator();
        final IAnnotator annotator = new PeakelAnnotator();

        m_extractionResultsTableModel.setRawFiles(rawfiles);
        
        if ((m_extractionWorker == null) || m_extractionWorker.isDone()) {
            for (ExtractionResult extraction : m_extractions) {
                extraction.setStatus(ExtractionResult.Status.REQUESTED);
            }
            m_extractionResultsTableModel.fireTableStructureChanged();
            m_extractionWorker = new SwingWorker<Integer, List<Object>>() {
                @Override
                protected Integer doInBackground() {
                    int count = 0;
                    for (ExtractionResult extraction : m_extractions) {
                        for (IRawFile rawFile : rawfiles) {
                            long start = System.currentTimeMillis();
                            IChromatogram c = rawFile.getXIC(extraction.getRequest());
                            count++;
                            List<Object> o = new ArrayList();
                            AnnotatedChromatogram ac = (extraction.getElutionTime() > 0) ?  annotator.annotate(rawFile, c, extraction.getRequest(), extraction.getExpectedCharge()) :  new AnnotatedChromatogram(c, null);
                            o.add(ac);
                            o.add(rawFile);
                            o.add(extraction);
                            publish(o);
                            logger.info("extraction done in " + (System.currentTimeMillis() - start));
                        }
                        extraction.setStatus(ExtractionResult.Status.DONE);
                    }
                    return count;
                }

                @Override
                protected void process(List<List<Object>> chunks) {
                    for (List<Object> o : chunks) {
                        AnnotatedChromatogram ac = (AnnotatedChromatogram)o.get(0);
                        IRawFile rf = (IRawFile)o.get(1);
                        ExtractionResult extraction = (ExtractionResult)o.get(2);
                        extraction.addChromatogram(rf, ac);
                    }
                    m_extractionResultsTableModel.fireTableDataChanged();
                }

                @Override
                protected void done() {
                    try {
                        logger.info("{} MS1 extraction done", get());
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error while extracting chromatograms", e);
                    }
                }
            };

            m_extractionWorker.execute();
        }
    }

    private JComponent getExtractionResultsTable() {

        m_extractionResultsTableModel = new ExtractionResultsTableModel();
        JScrollPane jScrollPane = new JScrollPane();
        m_extractionResultsTable = new ExtractionResultsTable();
        m_globalTableModel = (m_importedTableModel == null) ? new CompoundTableModel(m_extractionResultsTableModel, true) : new CompoundTableModel(new ExpansionTableModel(m_importedTableModel, m_extractionResultsTableModel), true);
        m_extractionResultsTable.setModel(m_globalTableModel);
        m_extractionResultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                int selRow = m_extractionResultsTable.getSelectedRow();
                if (selRow != -1){
                    ExtractionResult extraction = m_extractionResultsTableModel.getExtractionResultAt(getModelRowId(selRow));
                    //logger.debug("mouse clicked on Extraction Result "+extraction);
                    if (extraction != null && extraction.getStatus() == ExtractionResult.Status.DONE){
                        Map<IRawFile, IChromatogram> mapChr = extraction.getChromatogramsMap();
                        final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();
                        int nbFiles = rawfiles.size();
                        if (nbFiles == 1){
                            // view singleRawFile
                            if (mapChr.containsKey(rawfiles.get(0))){
                                AnnotatedChromatogram chr = (AnnotatedChromatogram )mapChr.get(rawfiles.get(0));
                                if (chr != null) {
                                    if (chr.getAnnotation() != null) {
                                        // because displayFeature will re-extract the XIC of the feature moz ... find a better solution
                                        m_extractionResultsViewer.displayFeatureInCurrentRawFile(chr.getAnnotation());
                                    } else {
                                        m_extractionResultsViewer.displayChromatogramAsSingleView(rawfiles.get(0), chr);
                                    }
                                }
                            }
                        }else{
                            // view all file
                            m_extractionResultsViewer.displayChromatogramAsMultiView(mapChr);
                        }
                    }
                }
                }
            }
        });

      //extractionResultsTable.getRowSorter().addRowSorterListener(this);
        jScrollPane.setViewportView(m_extractionResultsTable);
        m_extractionResultsTable.setFillsViewportHeight(true);
        m_extractionResultsTable.setViewport(jScrollPane.getViewport());

        m_markerContainerPanel = new MarkerContainerPanel(jScrollPane, m_extractionResultsTable);
        return m_markerContainerPanel;

    }

    private int getModelRowId(int rowId){
        if (m_globalTableModel.getRowCount() != 0) {
            // convert according to the sorting
            rowId = m_extractionResultsTable.convertRowIndexToModel(rowId);
            rowId = m_globalTableModel.convertCompoundRowToBaseModelRow(rowId);
        }
        return rowId;
    }

    class ExtractionResultsTable extends DecoratedMarkerTable {

        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();
            return popupMenu;
        }

        @Override
        public void prepostPopupMenu() {

        }

        @Override
        public void addTableModelListener(TableModelListener l) {

        }

    }

    private static List<MsnExtractionRequest> buildIRTRequest() {
        float moztol = MzScopePreferences.getInstance().getMzPPMTolerance();
        List<MsnExtractionRequest> list = new ArrayList<>();
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(487.257).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(547.297).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(622.853).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(636.869).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(644.822).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(669.838).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(683.827).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(683.853).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(699.338).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(726.835).build());
        list.add(MsnExtractionRequest.builder().setMzTolPPM(moztol).setMz(776.929).build());
        return list;
    }
}
