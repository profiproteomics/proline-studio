/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Font;
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
    
    private boolean m_decorated = false;

    @Override
    public void start(String filePath) {

        if (!filePath.endsWith(getFileExtension())) {
            filePath = filePath + getFileExtension();
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
    public void addCell(String t, ArrayList<ExportFontData> fonts) {

        Cell cell = m_row.createCell(m_curCell);

        if ((getDecorated()) && (fonts != null)) {
            
            HSSFRichTextString rich = new HSSFRichTextString(t);
            
            for (int i = 0; i < fonts.size(); i++) {

                int startIndex = fonts.get(i).getStartIndex();
                int stopIndex = fonts.get(i).getStopIndex();
                short color = fonts.get(i).getColor();

                HSSFFont currentFont = (HSSFFont) m_wb.createFont();

                if (currentFont != null) {
                    currentFont.setColor(color);

                    if (fonts.get(i).getTextWeight() == Font.BOLD) {
                        currentFont.setBold(true);
                    } else if (fonts.get(i).getTextWeight() == Font.ITALIC) {
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
        return ".xls";
    }

}
