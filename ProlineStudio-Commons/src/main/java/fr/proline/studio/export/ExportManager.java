package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Class used to do a local export of a table
 *
 * @author JM235353
 */
public class ExportManager {

    private final JTable m_table;

    public ExportManager(JTable table) {
        m_table = table;
    }

    public ExportWorker getTask(ExporterInterface exporter, String name, String filePath, boolean decorated) {
        return new ExportWorker(exporter, name, filePath, decorated);
    }

    private String componentToText(Component c) {
        String text = "";

        if (c instanceof ExportTextInterface) {
            text = ((ExportTextInterface) c).getExportText();
        } else if (c instanceof JLabel) {
            text = ((JLabel) c).getText();
        } else if (c instanceof AbstractButton) {
            text = ((AbstractButton) c).getText();
        }

        return text;
    }
    
    private ArrayList<ExportSubStringFont> componentToSubStringFonts(Component c){
        if(c instanceof ExportTextInterface){
            return ((ExportTextInterface) c).getSubStringFonts();
        }else{
            return new ArrayList<ExportSubStringFont>();
        }
    }
    

    public class ExportWorker extends DefaultDialog.ProgressTask {

        private ExporterInterface m_exporter;
        private String m_name;
        private String m_filePath;
        private boolean m_decorated;

        public ExportWorker(ExporterInterface exporter, String name, String filePath, boolean decorated) {
            m_exporter = exporter;
            m_name = name;
            m_filePath = filePath;
            m_decorated = decorated;
        }

        @Override
        protected Object doInBackground() throws Exception {
            m_exporter.start(m_filePath);
            m_exporter.startSheet(m_name);
            
            m_exporter.setDecorated(m_decorated);

            // headers
            boolean columnTextInterface = (m_table instanceof ExportModelInterface);
            int nbCol = m_table.getColumnCount();
            if (columnTextInterface) {
                m_exporter.startRow();
                for (int j = 0; j < nbCol; j++) {
                    String colName = ((ExportModelInterface) m_table).getExportColumnName(j);
                    m_exporter.addCell(colName, new ArrayList<ExportSubStringFont>());
                }
            } else {
                m_exporter.startRow();
                for (int j = 0; j < nbCol; j++) {
                    String colName = m_table.getColumnName(j);
                    m_exporter.addCell(colName, new ArrayList<ExportSubStringFont>());
                }
            }

            // all rows
            int nbRow = m_table.getRowCount();

            int lastPercentage = 0;
            int percentage;
            for (int row = 0; row < nbRow; row++) {
                boolean rowTextInterface = (m_table instanceof ExportModelInterface);
                if (rowTextInterface) {
                    m_exporter.startRow();
                    for (int col = 0; col < nbCol; col++) {
                        String text = ((ExportModelInterface) m_table).getExportRowCell(row, col);
                        if (text == null) {
                            TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                            Component c = m_table.prepareRenderer(renderer, row, col);
                            text = componentToText(c);
                        }
                        TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                        Component c = m_table.prepareRenderer(renderer, row, col);
                        m_exporter.addCell(text,componentToSubStringFonts(c));
                    }
                } else {
                    m_exporter.startRow();
                    for (int col = 0; col < nbCol; col++) {
                        TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                        Component c = m_table.prepareRenderer(renderer, row, col);
                        String text = componentToText(c);
                        
                        m_exporter.addCell(text, componentToSubStringFonts(c));
                    }
                }
                percentage = (int) Math.round((((double) (row + 1)) / nbRow) * 100);
                if (percentage > lastPercentage) {
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
