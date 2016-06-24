package fr.proline.studio.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

/**
 * Export data to Excell 2003 (xls, not xlsx)
 *
 * @author JM235353
 */
public class Excel2003Exporter implements ExporterInterface {

    private Workbook m_wb;
    private Sheet m_sheet;
    private int m_curRow = 0;
    private int m_curCell = 0;
    private Row m_row;

    private String m_pageName;
    private int m_pageCur = 1;

    private String m_filePath = null;

    @Override
    public void start(String filePath) {

        if (!filePath.endsWith(".xls")) {
            filePath = filePath + ".xls";
        }

        m_wb = new HSSFWorkbook();
        m_filePath = filePath;
    }

    @Override
    public void startSheet(String pageName) {
        m_pageName = pageName;
        m_sheet = m_wb.createSheet(pageName + "-1");
        m_curRow = 0;
    }

    @Override
    public void startRow() {

        if (m_curRow == 65536) {
            m_curRow = 0;
            m_pageCur++;
            m_sheet = m_wb.createSheet(m_pageName + "-" + m_pageCur);

        }

        m_row = m_sheet.createRow(m_curRow);

        m_curRow++;
        m_curCell = 0;
    }

    @Override
    public void addCell(HSSFRichTextString t, ArrayList<ExportSubStringFont> fonts) {

        for (int i = 0; i < fonts.size(); i++) {

            int startIndex = fonts.get(i).getStartIndex();
            int stopIndex = fonts.get(i).getStopIndex();
            short color = fonts.get(i).getColor();

            HSSFFont currentFont = (HSSFFont) m_wb.createFont();
            currentFont.setColor(color);

            if (startIndex > 0 && stopIndex < t.getString().length()) {
                t.applyFont(startIndex, stopIndex, currentFont);
            }

        }

        String text = t.getString();
        Cell cell = m_row.createCell(m_curCell);
        if (NumberUtils.isNumber(text)) {
            double d = Double.parseDouble(text);
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
    }

}
