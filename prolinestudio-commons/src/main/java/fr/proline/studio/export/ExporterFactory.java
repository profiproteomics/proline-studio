package fr.proline.studio.export;

import java.util.ArrayList;

/**
 * Link between data type and export type
 * @author JM235353
 */
public class ExporterFactory {
    
    public static final int EXPORT_TABLE = 1;
    public static final int EXPORT_IMAGE = 2;
    public static final int EXPORT_FROM_SERVER = 3;
    public static final int EXPORT_XIC = 4;
    public static final int EXPORT_MGF = 5;
    //public static final int EXPORT_TSV = 6; //VDS : NOT USED !!!! 
    public static final int EXPORT_SPECTRA = 6;
    public static final int EXPORT_MZIDENTML = 7;

    private static ArrayList<ExporterInfo> m_listTable = null;
    private static ArrayList<ExporterInfo> m_listImage = null;
    private static ArrayList<ExporterInfo> m_listServer = null;
    private static ArrayList<ExporterInfo> m_listXic = null;
    private static ArrayList<ExporterInfo> m_listMGF = null;
    private static ArrayList<ExporterInfo> m_listSpectra = null;
    private static ArrayList<ExporterInfo> m_listMzIdent = null;
    
    public static final ExporterInfo EXPORTER_INFO_PNG = new ExporterInfo(ExporterType.PNG, "PNG (.png)", "png");
    public static final ExporterInfo EXPORTER_INFO_SVG = new ExporterInfo(ExporterType.SVG, "SVG (.svg)", "svg");
    
    public enum ExporterType {
        EXCEL_XML,
        EXCEL_2003,
        CSV,
        PNG,
        SVG, 
        MGF, 
        TSV,
        MZID
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
            
       } else if (exportType == EXPORT_SPECTRA) {
             if (m_listSpectra != null) {
                return m_listSpectra;
            }
            m_listSpectra = new ArrayList<>(1);           
            m_listSpectra.add(new ExporterInfo(ExporterType.TSV, "TSV (.tsv)", "tsv"));

            return m_listSpectra;    
            
        } else if (exportType == EXPORT_IMAGE) {
            if (m_listImage != null) {
               return m_listImage;
           }
           m_listImage = new ArrayList<>(2);


           m_listImage.add(EXPORTER_INFO_PNG);
           m_listImage.add(EXPORTER_INFO_SVG);
          
           return m_listImage;
           
        }  else if (exportType == EXPORT_XIC) {
            if (m_listXic != null) {
                return m_listXic;
            }
            m_listXic = new ArrayList<>(1);

            m_listXic.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));
            m_listXic.add(new ExporterInfo(ExporterType.EXCEL_2003, "Excel 2003 (.xls)", "xls"));


            return m_listXic;
        } else if(exportType == EXPORT_FROM_SERVER){ // EXPORT_FROM_SERVER
            
            if (m_listServer != null) {
                return m_listServer;
            }
            m_listServer = new ArrayList<>(1);


            m_listServer.add(new ExporterInfo(ExporterType.EXCEL_XML, "Excel (.xlsx)", "xlsx"));
           // m_listServer.add(new ExporterInfo(ExporterType.CSV, "CSV (.txt)", "csv"));
            


            return m_listServer;
        }else if (exportType == EXPORT_MGF){
            if (m_listMGF != null) {
                return m_listMGF;
            }
            m_listMGF = new ArrayList<>(2);

            m_listMGF.add(new ExporterInfo(ExporterType.MGF, "MGF (.mgf)", "mgf"));
            m_listMGF.add(new ExporterInfo(ExporterType.TSV, "TSV (.tsv)", "tsv"));

            return m_listMGF; 
        } else if (exportType == EXPORT_MZIDENTML) {
            if(m_listMzIdent != null)
                return m_listMzIdent;
            
            m_listMzIdent = new ArrayList<>(1);
            m_listMzIdent.add(new ExporterInfo(ExporterType.MZID, "MzIdentML (.mzid)","mzid"));
            return m_listMzIdent; 
        }
        return new ArrayList(); // should not happen
    }
    
    public static class ExporterInfo {
        
        private final ExporterType m_type;
        private final String m_name;
        private final String m_fileExtension;
        
        public ExporterInfo(ExporterType type, String name, String fileExtension) {
            m_type = type;
            m_name = name;
            m_fileExtension = fileExtension;
        

        }

        public ExporterType geType() {
            return m_type;
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
