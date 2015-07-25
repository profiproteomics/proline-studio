/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionRequest;
import fr.proline.mzscope.ui.model.ExtractionResultsTableModel;
import fr.proline.mzscope.model.ExtractionResult;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class ExtractionResultsPanel extends JPanel {

   final private static Logger logger = LoggerFactory.getLogger(ExtractionResultsPanel.class);

   private List<ExtractionResult> extractions;
   private ExtractionResultsTableModel extractionResultsTableModel;
   private ExtractionResultsTable extractionResultsTable;
   private SwingWorker extractionWorker;
   
   public ExtractionResultsPanel() {
      initComponents();

   }

   private void initComponents() {
      setLayout(new BorderLayout());
      add(getToolBar(), BorderLayout.NORTH);
      add(getExtractionResultsTable(), BorderLayout.CENTER);
   }

   private JToolBar getToolBar() {
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      JButton importCSVBtn = new JButton("CSV");
      importCSVBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            importCSVExtractions();
         }

      });
      toolbar.add(importCSVBtn);
      JButton iRTBtn = new JButton("iRT");
      iRTBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            setExtractions(buildIRTRequest());
         }

      });
      toolbar.add(iRTBtn);
      toolbar.addSeparator();
      JButton extractBtn = new JButton("Run");
      extractBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            startExtractions();
         }
      });
      toolbar.add(extractBtn);
      return toolbar;
   }

   private void importCSVExtractions() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   public List<ExtractionResult> getExtractions() {
      return extractions;
   }

   public void setExtractions(List<Ms1ExtractionRequest> extractionRequests) {
      List<ExtractionResult> results = new ArrayList<>();
      for (Ms1ExtractionRequest request : extractionRequests) {
         ExtractionResult extractionResult = new ExtractionResult(request);
         results.add(extractionResult);
      }
      extractionResultsTableModel.setExtractions(results);
      extractions = results;
   }

   private void startExtractions() {
      final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();
      if ( (extractionWorker == null) || extractionWorker.isDone() ) {
      extractionWorker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;
            @Override
            protected Integer doInBackground() throws Exception {
                for (ExtractionResult extraction : extractions) {
                for (IRawFile rawFile : rawfiles) {
                    long start = System.currentTimeMillis();
                    Chromatogram c = rawFile.getXIC(extraction.getRequest());
                    extraction.addChromatogram(c);
                    count++;
                    publish(c);
                    logger.info("extraction done in "+(System.currentTimeMillis() - start));
                }
                extraction.setStatus(ExtractionResult.Status.DONE);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
               extractionResultsTableModel.fireTableDataChanged();
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} MS1 extraction done", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extracting chromatograms");
                }
            }
        };

        extractionWorker.execute();
      }
   }

   private JComponent getExtractionResultsTable() {

      extractionResultsTableModel = new ExtractionResultsTableModel();
      JScrollPane jScrollPane = new JScrollPane();
      extractionResultsTable = new ExtractionResultsTable();
      extractionResultsTable.setModel(extractionResultsTableModel);
      extractionResultsTable.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent evt) {
            logger.debug("Extraction Result table mouse clicked");
         }
      });
      
      //extractionResultsTable.getRowSorter().addRowSorterListener(this);

      jScrollPane.setViewportView(extractionResultsTable);
      extractionResultsTable.setFillsViewportHeight(true);
      extractionResultsTable.setViewport(jScrollPane.getViewport());

      MarkerContainerPanel m_markerContainerPanel = new MarkerContainerPanel(jScrollPane, extractionResultsTable);
      return m_markerContainerPanel;

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
   
   private static List<Ms1ExtractionRequest> buildIRTRequest() {
      List<Ms1ExtractionRequest> list = new ArrayList<>();
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(487.257).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(547.297).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(622.853).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(636.869).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(644.822).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(669.838).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(683.827).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(683.853).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(699.338).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(726.835).build());
      list.add(Ms1ExtractionRequest.builder().setMzTolPPM(10.0f).setMz(776.929).build());
      return list;
   }
}
