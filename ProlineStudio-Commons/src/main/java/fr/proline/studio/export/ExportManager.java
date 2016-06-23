package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;

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

    public ExportWorker getTask(ExporterInterface exporter, String name, String filePath) {
        return new ExportWorker(exporter, name, filePath);
    }

    /*
     private String componentToText(Component c) {
     if (c instanceof ExportTextInterface) {
     return ((ExportTextInterface)c).getExportText();
     } else if (c instanceof JLabel) {
     return ((JLabel) c).getText();
     } else if (c instanceof AbstractButton) {
     return ((AbstractButton) c).getText();
     }
     return "";
     }
     */
    private HSSFRichTextString componentToText(Component c) {
        HSSFRichTextString richString;
        String text = "";
        ArrayList<ExportSubStringFont> fontsToApply = null;

        if (c instanceof ExportTextInterface) {
            fontsToApply = ((ExportTextInterface) c).getSubStringFonts();
            text = ((ExportTextInterface) c).getExportText();
        } else if (c instanceof JLabel) {
            text = ((JLabel) c).getText();
        } else if (c instanceof AbstractButton) {
            text = ((AbstractButton) c).getText();
        }

        richString = new HSSFRichTextString(text);

        if (fontsToApply != null && fontsToApply.size() > 0) {
            this.applyFonts(richString, fontsToApply);
        }

        return richString;
    }

    private void applyFonts(HSSFRichTextString text, ArrayList<ExportSubStringFont> fonts) {

        if (fonts == null || fonts.size() < 1) {
            return;
        }

        for (int i = 0; i < fonts.size(); i++) {
            text.applyFont(fonts.get(i).getStartIndex(), fonts.get(i).getStopIndex(), fonts.get(i).getFont());
        }

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
            boolean columnTextInterface = (m_table instanceof ExportModelInterface);
            int nbCol = m_table.getColumnCount();
            if (columnTextInterface) {
                m_exporter.startRow();
                for (int j = 0; j < nbCol; j++) {
                    String colName = ((ExportModelInterface) m_table).getExportColumnName(j);
                    m_exporter.addCell(new HSSFRichTextString(colName));
                }
            } else {
                m_exporter.startRow();
                for (int j = 0; j < nbCol; j++) {
                    String colName = m_table.getColumnName(j);
                    m_exporter.addCell(new HSSFRichTextString(colName));
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
                        HSSFRichTextString text = new HSSFRichTextString(((ExportModelInterface) m_table).getExportRowCell(row, col));
                        if (text == null) {
                            TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                            Component c = m_table.prepareRenderer(renderer, row, col);
                            text = componentToText(c);
                        }
                        m_exporter.addCell(text);
                    }
                } else {
                    m_exporter.startRow();
                    for (int col = 0; col < nbCol; col++) {
                        TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                        Component c = m_table.prepareRenderer(renderer, row, col);
                        HSSFRichTextString text = componentToText(c);
                        m_exporter.addCell(text);
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
