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
public class PtmType {

    private String _ptm;

    public PtmType(String ptm) {
        _ptm = ptm.trim();
    }

    public Color getColor() {

        switch (_ptm) {
            case "Acetyl":
                return ViewSetting.DEFAULT_BASE_PALETTE[0];
            case "Phospho":
                return ViewSetting.DEFAULT_BASE_PALETTE[1];
            case "Oxidation":
                return ViewSetting.DEFAULT_BASE_PALETTE[2];
            default:
            {
                int length = ViewSetting.DEFAULT_BASE_PALETTE.length - 3;
                int i = (Math.abs(_ptm.hashCode()) % length)+3;
                return ViewSetting.DEFAULT_BASE_PALETTE[i];
            }
        }

    }

    public Character getPtmTypeChar() {
        return Character.toUpperCase(_ptm.charAt(0));
    }
    
    public String getToolTip(){
        return this._ptm;
    }
    
    public String toString(){
        return this._ptm;
    }
}
