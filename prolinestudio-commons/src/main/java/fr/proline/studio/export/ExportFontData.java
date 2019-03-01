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
