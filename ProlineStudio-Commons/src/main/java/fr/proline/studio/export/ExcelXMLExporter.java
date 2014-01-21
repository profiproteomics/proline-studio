package fr.proline.studio.export;


import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

/**
 *
 * @author JM235353
 */
public class ExcelXMLExporter implements ExporterInterface {
    
    private SXSSFWorkbook m_wb;
    private  Sheet m_sheet;
    private int m_curRow = 0;
    private int m_curCell = 0; 
    private Row m_row;
    
    private String m_filePath = null;
    
    @Override
    public void start(String filePath) {
        
        if (!filePath.endsWith(".xlsx")) {
            filePath = filePath+".xlsx";
        }
        
        m_wb = new SXSSFWorkbook(512);
        m_filePath = filePath;
    }
    
    @Override
    public void startSheet(String pageName) {
        m_sheet = m_wb.createSheet(pageName);
        m_curRow = 0;
    }
    
    @Override
    public void startRow() {
        m_row = m_sheet.createRow(m_curRow);
        
        m_curRow++;
        m_curCell=0;
    }
    
    @Override
    public void addCell(String t) {
        Cell cell = m_row.createCell(m_curCell);
        
        if (NumberUtils.isNumber(t)) {
            double d = Double.parseDouble(t);
            cell.setCellValue(d);
        } else {
            cell.setCellValue(t);
        }
        m_curCell++;
    }
    
    @Override
    public void end() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(m_filePath);
        m_wb.write(fileOut);
        fileOut.close();
        
        m_wb.dispose();
    }
    
}
