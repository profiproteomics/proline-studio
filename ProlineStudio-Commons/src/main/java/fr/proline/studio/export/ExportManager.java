package fr.proline.studio.export;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Class used to do a local export of a table
 *
 * @author JM235353
 */
public class ExportManager {

    private final JTable m_table;

    private ExportManager m_exportManager = null;
    private Exception m_exportException = null;
    
    
    public ExportManager(JTable table) {
        m_table = table;
        m_exportManager = this;
    }

    public ExportWorker getTask(ExporterInterface exporter, String name, String filePath, boolean decorated) {
        return new ExportWorker(exporter, name, filePath, decorated);
    }
    
    public void setException(Exception e) {
        m_exportException = e;
    }

    public Exception getException() {
        return m_exportException;
    }

    private String componentToText(Component c) {
        String text = "";

        /*if (c instanceof ExportTextInterface) {
            text = ((ExportTextInterface) c).getExportText();
        } else*/ if (c instanceof JLabel) {
            text = ((JLabel) c).getText();
        } else if (c instanceof AbstractButton) {
            text = ((AbstractButton) c).getText();
        }

        return text;
    }
    
    private ArrayList<ExportSubStringFont> componentToSubStringFonts(Component c){
        /*if(c instanceof ExportTextInterface){
            return ((ExportTextInterface) c).getSubStringFonts();
        }else{*/
            return null;
        //}
    }
    

    public class ExportWorker extends DefaultDialog.ProgressTask {

        private final ExporterInterface m_exporter;
        private final String m_name;
        private final String m_filePath;
        private final boolean m_decorated;

        public ExportWorker(ExporterInterface exporter, String name, String filePath, boolean decorated) {
            m_exporter = exporter;
            m_name = name;
            m_filePath = filePath;
            m_decorated = decorated;
        }

        @Override
        protected Object doInBackground() throws Exception {
            
            try {
                m_exporter.start(m_filePath);
                m_exporter.startSheet(m_name);

                m_exporter.setDecorated(m_decorated);

                // --- JPM.TODO : following lines should be called in AWT
                
                // get cols from the table
                int nbCol = m_table.getColumnCount();
                int[] colsInModel = new int[nbCol];
                for (int i=0;i<nbCol;i++) {
                    int colInModel = m_table.convertColumnIndexToModel(i);
                    colsInModel[i] = colInModel;
                }
                
                // get rows from the table
                int nbRow = m_table.getRowCount();
                int[] rowsInModel = new int[nbRow];
                for (int i = 0; i < nbRow; i++) {
                    int rowInModel = m_table.convertRowIndexToModel(i);
                    rowsInModel[i] = rowInModel;
                }
                
                // --- JPM.TODO.END 
                
                // retrieve model
                TableModel model = m_table.getModel();
                ExportModelInterface exportInterface = (model instanceof ExportModelInterface) ? (ExportModelInterface) model : null;
                
                // headers
                if (exportInterface != null) {
                    m_exporter.startRow();
                    for (int j = 0; j < nbCol; j++) {
                        String colName = exportInterface.getExportColumnName(colsInModel[j]);
                        m_exporter.addCell(colName, null);
                    }
                } else {
                    m_exporter.startRow();
                    for (int j = 0; j < nbCol; j++) {
                        String colName = model.getColumnName(colsInModel[j]);
                        m_exporter.addCell(colName, null);
                    }
                }

                // all rows
                

                int lastPercentage = 0;
                int percentage;
                for (int j = 0; j < nbRow; j++) {
                    int row = rowsInModel[j];
                    if (exportInterface != null) {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = colsInModel[i];
                            String text = exportInterface.getExportRowCell(row, col);
                             ArrayList<ExportSubStringFont> stringFonts = null;
                            if (text == null) {
                                Object o = ((CompareDataInterface) model).getDataValueAt(row, col);
                                if (o != null) {
                                    text = o.toString();
                                }
                            } else {
                                stringFonts = exportInterface.getSubStringFonts(row, col);
                            }
                            m_exporter.addCell(text, stringFonts);
                        }
                    } else if (model instanceof CompareDataInterface) {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = colsInModel[i];
                            String text = null;
                            Object o = ((CompareDataInterface) model).getDataValueAt(row, col);
                            if (o != null) {
                                text = o.toString();
                            }
                            
                            m_exporter.addCell(text, null);
                        }
                    } else {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = colsInModel[i];
                            String text = null;
                            Object o = model.getValueAt(row, col);
                            if (o != null) {
                                text = o.toString();
                            }
                            
                            m_exporter.addCell(text, null);
                        }
                    }
                    
                    /*if (rowTextInterface) {
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
                            m_exporter.addCell(text, componentToSubStringFonts(c));
                        }
                    } else {
                        m_exporter.startRow();
                        for (int col = 0; col < nbCol; col++) {
                            TableCellRenderer renderer = m_table.getCellRenderer(row, col);
                            Component c = m_table.prepareRenderer(renderer, row, col);
                            String text = componentToText(c);

                            m_exporter.addCell(text, componentToSubStringFonts(c));
                        }
                    }*/
                    percentage = (int) Math.round((((double) (j + 1)) / nbRow) * 100);
                    if (percentage > lastPercentage) {
                        setProgress(percentage);
                        lastPercentage = percentage;
                    }
                }

            } catch (Exception e) {
                m_exportManager.setException(e);
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
