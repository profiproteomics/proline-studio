package fr.proline.studio.export;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ExporterFactory {
    
    private static ArrayList<ExporterInfo> m_list = null;
    
    public enum ExporterType {
        EXCEL_XML,
        EXCEL_2003,
        CSV
    };
    
    public static  ArrayList<ExporterInfo> getList() {
        if (m_list != null) {
            return m_list;
        }
        m_list = new ArrayList<>(3);

        
        m_list.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));
        m_list.add(new ExporterInfo(ExporterType.EXCEL_2003, "Excel 2003 (.xls)", "xls"));
        m_list.add(new ExporterInfo(ExporterType.CSV, "CSV (.csv)", "csv"));
    
        return m_list;
    }
    
    public static class ExporterInfo {
        
        private ExporterType m_type;
        private String m_name;
        private String m_fileExtension;
        
        public ExporterInfo(ExporterType type, String name, String fileExtension) {
            m_type = type;
            m_name = name;
            m_fileExtension = fileExtension;
        

        }
        
        public String getName() {
            return m_name;
        }

        public String getFileExtension() {
            return m_fileExtension;
        }
        
        public ExporterInterface getExporter() {
            switch (m_type) {
                case EXCEL_XML:
                    return new ExcelXMLExporter();
                case EXCEL_2003:
                    return new Excel2003Exporter();
                case CSV:
                    return new CSVExporter();
            }
            return null; // should never happen
        }
        
        @Override
        public String toString() {
            return m_name;
        }
    }
    
}
