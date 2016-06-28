/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.export;

import java.awt.Font;

/**
 *
 * @author AK249877
 */
public class ExportSubStringFont {

    private int startIndex, stopIndex;
    private short color;
    private int textWeight;

    public ExportSubStringFont() {
        ;
    }

    public ExportSubStringFont(int startIndex, int stopIndex, short color) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.color = color;
        this.textWeight = Font.PLAIN;
    }

    public ExportSubStringFont(int startIndex, int stopIndex, short color, int textWeight) {
        this(startIndex, stopIndex, color);
        this.textWeight = textWeight;
    }

    public void setTextWeight(int textWeight) {
        this.textWeight = textWeight;
    }

    public int getTextWeight() {
        return this.textWeight;
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

    public void setColor(short color) {
        this.color = color;
    }

    public short getColor() {
        return this.color;
    }

}
