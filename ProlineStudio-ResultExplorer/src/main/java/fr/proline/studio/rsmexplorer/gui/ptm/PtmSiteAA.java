/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 26 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import java.awt.Color;

/**
 *
 * @author Karine XUE
 */
public class PtmSiteAA {

    Character _aminoAcid;

    /**
     * location of modification inside a peptide
     */
    int _locPep;
    /**
     * location of modification at the protein level
     */
    int _locProtein;

    PtmType _ptmType;
    float _probability;
    String _readableString;
    boolean _isNTermAt1;

    /**
     * example of s: Acetyl (Protein N-term); Phospho (S7); Phospho (T9);
     * Phospho (S10); Phospho (S14); Phospho (Y30)
     *
     * @param s
     * @param peptideFirstAAInProtein first AA of peptide position in protein
     * @param isNTermAt1
     */
    public PtmSiteAA(String s, int peptideFirstAAInProtein, boolean isNTermAt1) {
        _isNTermAt1 = isNTermAt1;
        int beginIndex = s.indexOf("(");
        int endIndex = s.indexOf(")");
        Character aa;
        int location;//location in peptide

        if (s.contains("N-term")) {
            aa = ' ';
            location = 0;
            if (peptideFirstAAInProtein == 1) {
                location = 1;
            }

        } else if (s.contains("C-term")) {
            aa = ' ';
            location = Integer.MAX_VALUE;
        } else {
            aa = s.charAt(beginIndex + 1);
            location = Integer.parseInt(s.substring(beginIndex + 2, endIndex));
        }

        int locationInProtein;
        if (location == Integer.MAX_VALUE) {
            locationInProtein = 0;
        } else if (isNTermAt1) {
            locationInProtein = location;
        } else {
            locationInProtein = location + peptideFirstAAInProtein;
        }
        String type = s.substring(0, beginIndex);
        //return new PtmSiteAA(aa, location, locationInProtein, type, probability);

        this._readableString = s;
        this._aminoAcid = aa;
        this._locPep = location;
        this._locProtein = locationInProtein;
        this._ptmType = new PtmType(type);
        this._probability = 1;
    }

    public boolean isNTermAt1() {
        return _isNTermAt1;
    }

    public Character getAminoAcid() {
        return _aminoAcid;
    }

    public void setLocProtein(int locationInProtein) {
        this._locProtein = locationInProtein;
    }

    public Color getColorWithProbability() {
        float[] hsbvals = new float[3];//Hue Saturation Brightness
        Color c = _ptmType.getColor();
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbvals);
        Color colorWithProbability = Color.getHSBColor(hsbvals[0], this._probability, hsbvals[0]);

        return colorWithProbability;
    }

    public Color getColor() {
       return _ptmType.getColor();
    }

    public int getModifyLocPep() {
        return _locPep;
    }

    public int getModifyLocProtein() {
        return _locProtein;
    }

    public Character getPtmTypeChar() {
        return _ptmType.getPtmTypeChar();
    }

    public float getProbability() {
        return _probability;
    }

    public void setProbability(float _probability) {
        this._probability = _probability;
    }

    public String getPtmSite() {
        return _readableString;
    }

    @Override
    public String toString() {
        return "PtmSiteAA{" + "m_aminoAcid=" + _aminoAcid + ", m_locPep=" + _locPep + ", m_locProtein=" + _locProtein + ", m_ptmType=" + _ptmType + ", _probability=" + _probability + '}';
    }

}
