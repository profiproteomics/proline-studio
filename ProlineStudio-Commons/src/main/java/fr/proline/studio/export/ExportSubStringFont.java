/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.export;

import org.apache.poi.hssf.usermodel.HSSFFont;

/**
 *
 * @author AK249877
 */
public class ExportSubStringFont {

    private int startIndex, stopIndex;
    private HSSFFont font;

    public ExportSubStringFont() {
        ;
    }

    public ExportSubStringFont(int startIndex, int stopIndex, HSSFFont font) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.font = font;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
    }

    public int getStopIndex() {
        return this.stopIndex;
    }

    public void setFont(HSSFFont font) {
        this.font = font;
    }

    public HSSFFont getFont() {
        return this.font;
    }
}
