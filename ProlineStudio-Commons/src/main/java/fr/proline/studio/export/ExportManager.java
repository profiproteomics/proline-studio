package fr.proline.studio.export;


import fr.proline.studio.gui.DefaultDialog;
import java.awt.Component;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ExportManager {

    private JTable m_table;

    public ExportManager(JTable table) {
        m_table = table;
    }

    public ExportWorker getTask(ExporterInterface exporter, String name, String filePath) {
        return new ExportWorker(exporter, name, filePath);
    }

    private String componentToText(Component c) {
        if (c instanceof JLabel) {
            return ((JLabel) c).getText();
        } else if (c instanceof AbstractButton) {
            return ((AbstractButton) c).getText();
        }
        return "";
    }

    public class ExportWorker extends DefaultDialog.ProgressTask {

        private ExporterInterface m_exporter;
        private String m_name;
        private String m_filePath;

        public ExportWorker(ExporterInterface exporter, String name, String filePath) {
            m_exporter = exporter;
            m_name = name;
            m_filePath = filePath;
        }

        @Override
        protected Object doInBackground() throws Exception {
            m_exporter.start(m_filePath);
            m_exporter.startSheet(m_name);

            // headers
            int nbCol = m_table.getColumnCount();
            m_exporter.startRow();
            for (int j = 0; j < nbCol; j++) {
                String colName = m_table.getColumnName(j);
                m_exporter.addCell(colName);
            }


            // all rows
            int nbRow = m_table.getRowCount();

            int lastPercentage = 0;
            int percentage;
            for (int row = 0; row < nbRow; row++) {
                m_exporter.startRow();
                for (int col = 0; col < nbCol; col++) {
                    TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                    Component c = m_table.prepareRenderer(renderer, row, col);
                    String text = componentToText(c);
                    m_exporter.addCell(text);
                }
                percentage = (int) Math.round((((double)(row + 1)) / nbRow) * 100);
                if (percentage>lastPercentage) {
                    setProgress(percentage);
                    lastPercentage = percentage;
                }
            }

            setProgress(getMaxValue());
            
            // end fild
            m_exporter.end();

            return null;
        }

        @Override
        public int getMinValue() {
            return 0;
        }

        @Override
        public int getMaxValue() {
            return 100;
        }
    }
}
