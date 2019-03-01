package fr.proline.studio.demo;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotScatter;
import fr.proline.studio.sampledata.Sample;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author CB205360
 */
public class ScatterPlotDemo extends JFrame { 

   private PlotPanel plotPanel;
   
   public ScatterPlotDemo() {
      super("ScatterPlot demo");
      JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      split.setDividerLocation(150);
      plotPanel = new PlotPanel();
      Sample sample = new Sample(3000);
      final BasePlotPanel basePlot = plotPanel.getBasePlotPanel();
      PlotScatter scatter = new PlotScatter(basePlot, sample, null, 0, 3);
      basePlot.setPlot(scatter);
      basePlot.setDrawCursor(true);
      //basePlot.setMargins(new Insets(5, 0, 5, 0));
      basePlot.getXAxis().setRange(-4, 4);
      getContentPane().setLayout(new BorderLayout());
      split.setLeftComponent(plotPanel);
      split.setRightComponent(getTablePane(sample));
      getContentPane().add(split, BorderLayout.CENTER);
      pack();
      setSize(450,350);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//      addWindowListener(new WindowAdapter(){
//                public void windowClosing(WindowEvent e){
//                    System.out.println("Mean time per frame= "+basePlot.fps.getMeanTimePerFrame()+" ms");
//                }
//            });
   }

   protected JComponent getTablePane(Sample sample) {
       JScrollPane scroll = new javax.swing.JScrollPane();
        DecoratedTable table = new DecoratedTable() {

          @Override
          public TablePopupMenu initPopupMenu() {
             return new TablePopupMenu();
          }

          @Override
          public void prepostPopupMenu() {  }

       };
        table.setModel(sample.getTableModel());
        scroll.setViewportView(table);
        
        return scroll;
   }
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
               ScatterPlotDemo plot = new ScatterPlotDemo();
               plot.setVisible(true);
            }
         });
      } catch (ClassNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (InstantiationException ex) {
         Exceptions.printStackTrace(ex);
      } catch (IllegalAccessException ex) {
         Exceptions.printStackTrace(ex);
      } catch (UnsupportedLookAndFeelException ex) {
         Exceptions.printStackTrace(ex);
      }
   }
   
}
