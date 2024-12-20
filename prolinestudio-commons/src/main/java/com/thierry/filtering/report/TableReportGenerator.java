/* 
 * Copyright (C) 2019
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
package com.thierry.filtering.report;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

public class TableReportGenerator {

    private JTable m_table;

    public TableReportGenerator(JTable table) {
        m_table = table;
    }

    private Component getRendererComponent(int row, int column) {
        TableCellRenderer renderer = m_table.getCellRenderer(row, column);
        return m_table.prepareRenderer(renderer, row, column);
    }

    public String generateFullReport(ReportBuilder builder) {
        ListSelectionModel previousModel = m_table.getSelectionModel();
        try {
            m_table.setSelectionModel(previousModel.getClass().getDeclaredConstructor().newInstance());
            builder.open();
            builder.startTable();

            int columnCount = m_table.getColumnCount();
            int rowCount = m_table.getRowCount();

            builder.startTableHeader();
            for (int i = 0; i < columnCount; i++) {
                builder.addColumnHeader(m_table.getColumnName(i));
            }
            builder.endTableHeader();

            for (int i = 0; i < rowCount; i++) {
                builder.startLine();
                for (int j = 0; j < columnCount; j++) {
                    addHTMLFormattedCell(builder, i, j);
                }
                builder.endLine();
            }
            builder.endTable();
            builder.close();
            return builder.toString();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        } finally {
            if (m_table != null) {
                m_table.setSelectionModel(previousModel);
            }
        }
    }

    public String getSelection(ReportBuilder builder) {
        ListSelectionModel previousModel = m_table.getSelectionModel();
        int[] rows = m_table.getSelectedRows();
        try {
            m_table.setSelectionModel(previousModel.getClass().getDeclaredConstructor().newInstance());
            int columnCount = m_table.getColumnCount();
            builder.open();
            builder.startTable();

            builder.startTableHeader();
            for (int i = 0; i < columnCount; i++) {
                builder.addColumnHeader(m_table.getColumnName(i));
            }
            builder.endTableHeader();
            
            for (int i = 0; i < rows.length; i++) {
                builder.startLine();
                for (int j = 0; j < columnCount; j++) {
                    addHTMLFormattedCell(builder,rows[i],j);
                }
                builder.endLine();
            }
            builder.endTable();
            builder.close();
            return builder.toString();
        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        } finally {
            if (m_table != null) {
                m_table.setSelectionModel(previousModel);
            }
        }
    }

    private void addHTMLFormattedCell(ReportBuilder builder, int row, int column) {
        Component comp = getRendererComponent(row, column);
        Color bgColor = comp.getBackground();
        Color fgColor = comp.getForeground();
        builder.addCell(bgColor, fgColor, extractTextFromComponent(comp));
    }

    private String extractTextFromComponent(Component comp) {
        String text = "";
        if (comp instanceof JLabel) {
            text = ((JLabel) comp).getText();
        } else if (comp instanceof AbstractButton) {
            text = ((AbstractButton) comp).getText();
        }
        text = text.replaceFirst("<html><body>", "");
        text = text.replaceFirst("</body></html>", "");
        return text;
    }
}
