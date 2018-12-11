/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 29 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Karine XUE
 */
public class ViewSetting {

    /**
     * the width to draw an Amino acid
     */
    //public static int WIDTH_AA = 13;
    public static int HEIGHT_AA = 14;
    public static int HEIGHT_PTM = 14;
    public static int BORDER_GAP = 5;
    public static int HEIGHT_MARK;
    public static int HEIGHT_SEQUENCE;
    public static Font FONT_NUMBER = new Font(Font.SERIF, Font.BOLD, 10);
    public static Font FONT_PTM = new Font(Font.MONOSPACED, Font.BOLD, 18);
    public static Font FONT_SEQUENCE = new Font(Font.MONOSPACED, Font.BOLD, 24);
    public static Color PEPTIDE_COLOR = new Color(240, 255, 255);
    public static Color SELECTED_PEPTIDE_COLOR = Color.black;
    public static Color SEQUENCE_COLOR = Color.BLACK;
    private static final double FONT_ROTATE = -Math.PI / 6;
    public static Font FONT_NUMBER_DIAGONAL;

    static {
        HEIGHT_MARK = HEIGHT_AA * 2 + BORDER_GAP * 2;
        HEIGHT_SEQUENCE = HEIGHT_AA + BORDER_GAP * 4;
        
        AffineTransform rotateText = new AffineTransform();
        rotateText.rotate(FONT_ROTATE);
        FONT_NUMBER_DIAGONAL = FONT_NUMBER.deriveFont(rotateText);
    }

    public static final Color[] DEFAULT_BASE_PALETTE = {
        new Color(225, 43, 10), // red
        new Color(42, 23, 234), // blue
        new Color(10, 255, 43), // green

        new Color(252, 180, 46), // orange
        new Color(0, 147, 221), // cyan
        new Color(221, 18, 123), // magenta
        new Color(231, 197, 31), //yellow
        new Color(231, 113, 58), //orange
        new Color(169, 35, 59), //red bordeaux
        new Color(106, 44, 95), //purple bordeaux
        new Color(104, 71, 160), //purple blue
        new Color(98, 126, 206), //blue
        new Color(82, 120, 123), //green blue 
        new Color(63, 121, 58), //green
        new Color(109, 153, 5) //green grace
    };

}
