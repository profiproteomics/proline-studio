package fr.proline.studio.table;

import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExpansionTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author CB205360
 */
public class CompoundTableDemo extends JFrame {

    public CompoundTableDemo() {
        super("Compound Table demo");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getTablePane(), BorderLayout.CENTER);
        setSize(450, 350);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    protected JComponent getTablePane() {
        JScrollPane scroll = new javax.swing.JScrollPane();
        DecoratedTable table = new DecoratedTable() {

            @Override
            public TablePopupMenu initPopupMenu() {
                return new TablePopupMenu();
            }

            @Override
            public void prepostPopupMenu() {
            }

        };

        String filepath = "C:/Users/CB205360/Documents/Classeur1.csv";
        ImportedDataTableModel model1 = new ImportedDataTableModel();
        ImportedDataTableModel.loadFile(model1, filepath, ',', true, false);
        ImportedDataTableModel model2 = new ImportedDataTableModel();
        ImportedDataTableModel.loadFile(model2, filepath, ',', true, false);
        ExpansionTableModel model = new ExpansionTableModel(model1, model2);
        
        table.setModel(model);
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
                    CompoundTableDemo plot = new CompoundTableDemo();
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
