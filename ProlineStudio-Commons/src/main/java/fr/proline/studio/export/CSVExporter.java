package fr.proline.studio.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Export data to CSV
 * @author JM235353
 */
public class CSVExporter implements ExporterInterface {

    private StringBuilder m_sb = new StringBuilder();
    private static final char separatorCSV = ',';

    private int m_curRow = 0;
    private int m_curCell = 0;
    
    private  FileWriter m_fw;
    
    boolean m_decorated = false;
    
    @Override
    public void start(String filePath) throws java.io.IOException {
        
        if (!filePath.endsWith(".csv") && !filePath.endsWith(".txt")) {
            filePath = filePath+".csv";
        }
        
        m_fw = new FileWriter(filePath);
    }

    @Override
    public void startSheet(String pageName) {
        // nothing to do
    }

    @Override
    public void startRow() throws IOException {
        if (m_curRow>0) {
            m_fw.write("\n");
        }
        m_curRow++;
        m_curCell = 0;
    }

    @Override
    public void addCell(String t, ArrayList<ExportSubStringFont> fonts) throws IOException {
        
        if (m_curCell>0) {
            m_fw.write(separatorCSV);
        }
        m_curCell++;
        m_fw.write(StringEscapeUtils.escapeCsv(t));
    }
    
    @Override
    public void end() throws IOException {
        if (m_curRow>0) {
            m_fw.write("\n");
        }
        m_fw.close();
    }

    @Override
    public void setDecorated(boolean decorated) {
        this.m_decorated = decorated;
    }

    @Override
    public boolean getDecorated() {
        return this.m_decorated;
    }
    

    
}
