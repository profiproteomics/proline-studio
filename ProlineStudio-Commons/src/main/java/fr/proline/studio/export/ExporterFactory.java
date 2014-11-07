package fr.proline.studio.export;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ExporterFactory {
    
    public static final int EXPORT_TABLE = 1;
    public static final int EXPORT_IMAGE = 2;
    public static final int EXPORT_FROM_SERVER = 3;
    public static final int EXPORT_IMAGE2 = 4;
    public static final int EXPORT_XIC = 5;

    private static ArrayList<ExporterInfo> m_listTable = null;
    private static ArrayList<ExporterInfo> m_listImage = null;
    private static ArrayList<ExporterInfo> m_listServer= null;
    private static ArrayList<ExporterInfo> m_listImage2 = null;
    private static ArrayList<ExporterInfo> m_listXic = null;
    
    public enum ExporterType {
        EXCEL_XML,
        EXCEL_2003,
        CSV,
        PNG,
        SVG
    };
    
    public static  ArrayList<ExporterInfo> getList(int exportType) {
        
        if (exportType == EXPORT_TABLE) {

            if (m_listTable != null) {
                return m_listTable;
            }
            m_listTable = new ArrayList<>(3);


            m_listTable.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));
            m_listTable.add(new ExporterInfo(ExporterType.EXCEL_2003, "Excel 2003 (.xls)", "xls"));
            m_listTable.add(new ExporterInfo(ExporterType.CSV, "CSV (.csv)", "csv"));
           

            return m_listTable;
        } else if (exportType == EXPORT_IMAGE) {
            if (m_listImage != null) {
               return m_listImage;
           }
           m_listImage = new ArrayList<>(1);


           m_listImage.add(new ExporterInfo(ExporterType.PNG, "PNG (.png)", "png"));
          
           return m_listImage;
           
        } else if (exportType == EXPORT_IMAGE2) {
            if (m_listImage2 != null) {
               return m_listImage2;
           }
           m_listImage2 = new ArrayList<>(2);


           m_listImage2.add(new ExporterInfo(ExporterType.PNG, "PNG (.png)", "png"));
          // m_listImage2.add(new ExporterInfo(ExporterType.PNG, "3000x2000 pixels PNG (.png)", "png"));
           m_listImage2.add(new ExporterInfo(ExporterType.PNG, "SVG (.svg)", "svg"));
          // m_listImage2.add(new ExporterInfo(ExporterType.PNG, "SVG jfreesvg (.svg)", "svg"));
          // m_listImage.add(new ExporterInfo(ExporterType.PNG, "WMF (.WMF)", "wmf"));

           return m_listImage2;
        }else if (exportType == EXPORT_XIC) {
            if (m_listXic != null) {
                return m_listXic;
            }
            m_listXic = new ArrayList<>(1);

            m_listXic.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));
            m_listXic.add(new ExporterInfo(ExporterType.EXCEL_2003, "Excel 2003 (.xls)", "xls"));


            return m_listXic;
        } else { // EXPORT_FROM_SERVER
            
            if (m_listServer != null) {
                return m_listServer;
            }
            m_listServer = new ArrayList<>(1);


            m_listServer.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));


            return m_listServer;
        }
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
