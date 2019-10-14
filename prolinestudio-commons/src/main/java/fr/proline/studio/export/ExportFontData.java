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

import java.awt.Font;

/**
 *
 * @author AK249877
 */
public class ExportFontData {

    private int m_startIndex, m_stopIndex;
    private short m_color;
    private int m_textWeight;

    public ExportFontData() {
    }

    public ExportFontData(int startIndex, int stopIndex, short color) {
        m_startIndex = startIndex;
        m_stopIndex = stopIndex;
        m_color = color;
        m_textWeight = Font.PLAIN;
    }

    public ExportFontData(int startIndex, int stopIndex, short color, int textWeight) {
        this(startIndex, stopIndex, color);
        m_textWeight = textWeight;
    }

    public void setTextWeight(int textWeight) {
        m_textWeight = textWeight;
    }

    public int getTextWeight() {
        return m_textWeight;
    }

    public void setStartIndex(int startIndex) {
        m_startIndex = startIndex;
    }

    public int getStartIndex() {
        return m_startIndex;
    }

    public void setStopIndex(int stopIndex) {
        m_stopIndex = stopIndex;
    }

    public int getStopIndex() {
        return m_stopIndex;
    }

    public void setColor(short color) {
        m_color = color;
    }

    public short getColor() {
        return m_color;
    }

}
