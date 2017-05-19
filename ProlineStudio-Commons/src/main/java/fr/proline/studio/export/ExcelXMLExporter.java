package fr.proline.studio.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Font;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Export data to xlsx
 *
 * @author JM235353
 */
public class ExcelXMLExporter implements ExporterInterface {

    private XSSFWorkbook m_wb;
    private Sheet m_sheet;
    private int m_curRow = 0;
    private int m_curCell = 0;
    private Row m_row;

    private String m_filePath = null;

    private boolean m_decorated = false;

    @Override
    public void start(String filePath) {

        if (!filePath.endsWith(getFileExtension())) {
            filePath = filePath + getFileExtension();
        }

        m_wb = new XSSFWorkbook();
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
        m_curCell = 0;
    }

    @Override
    public void addCell(String t, ArrayList<ExportFontData> fonts) {

        Cell cell = m_row.createCell(m_curCell);

        if ((getDecorated()) && (fonts != null)) {
            
            XSSFRichTextString rich = new XSSFRichTextString(t);
            
            for (int i = 0; i < fonts.size(); i++) {

                int startIndex = fonts.get(i).getStartIndex();
                int stopIndex = fonts.get(i).getStopIndex();
                short color = fonts.get(i).getColor();

                XSSFFont currentFont = (XSSFFont) m_wb.createFont();

                if (currentFont != null) {
                    currentFont.setColor(color);

                    if (fonts.get(i).getTextWeight() == Font.BOLD) {
                        currentFont.setBold(true);
                    } else if (fonts.get(i).getTextWeight() == java.awt.Font.ITALIC) {
                        currentFont.setItalic(true);
                    }

                    if (startIndex >= 0 && stopIndex <= t.length()) {
                        rich.applyFont(startIndex, stopIndex, currentFont);
                    }
                }

            }

            cell.setCellValue(rich);
        } else if (NumberUtils.isNumber(t)) {
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
        fileOut.flush();
        fileOut.close();
    }

    @Override
    public void setDecorated(boolean decorated) {
        m_decorated = decorated;
    }

    @Override
    public boolean getDecorated() {
        return m_decorated;
    }

    @Override
    public String getFileExtension() {
        return ".xlsx";
    }

}
