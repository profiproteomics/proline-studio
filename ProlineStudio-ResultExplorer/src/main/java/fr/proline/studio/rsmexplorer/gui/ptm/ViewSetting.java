/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 29 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Karine XUE
 */
public class ViewSetting {

    /**
     * the width to draw an Amino acid
     */
    public static int WIDTH_AA;
    public static int HEIGHT_AA;

    public static int BORDER_GAP = 5;
    public static int HEIGHT_MARK;
    public static int HEIGHT_SEQUENCE;
    public static Font FONT_NUMBER = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
    public static Font FONT_PTM = new Font(Font.MONOSPACED, Font.BOLD, 11);
    public static Font FONT_SEQUENCE = new Font(Font.MONOSPACED, Font.BOLD, 16);
    public static Color PEPTIDE_COLOR = CyclicColorPalette.getDarkerColor(CyclicColorPalette.getColor(5), -0.7);
    public static Color SELECTED_PEPTIDE_COLOR = CyclicColorPalette.GRAY_DARK;
    public static Color SEQUENCE_COLOR = Color.BLUE;
    private static final double FONT_ROTATE = -Math.PI / 6;
    public static Font FONT_NUMBER_DIAGONAL;

    public static final Stroke DASHED = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f, 2.0f}, 0.0f);
    public static final BasicStroke STROKE_PEP = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    public static Map<Long, Color> PTM_COLORS = new HashMap<>();
    
    static {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,false,true);
        WIDTH_AA = (int)Math.round(FONT_SEQUENCE.getStringBounds("M", frc).getWidth());
        // getHeight or getAscent give the height of the line, not the height of the single character. Since the font is monospaced, we assume that letters are square.
        HEIGHT_AA = (int)Math.round(WIDTH_AA);

        HEIGHT_MARK = (int)Math.round(HEIGHT_AA * 2.5 + BORDER_GAP * 2);
        HEIGHT_SEQUENCE = HEIGHT_AA + BORDER_GAP * 2;

        AffineTransform rotateText = new AffineTransform();
        rotateText.rotate(FONT_ROTATE);
        FONT_NUMBER_DIAGONAL = FONT_NUMBER.deriveFont(rotateText);
    }

    
    // PTM Colors: in this implementation always returns the same color for a specificity, colors 
    // are assigned based on the order they are requested. A better implementation will be to 
    // load used PTM specificityId of a project, sort them by Id and assigned a color based on the rank 
    // of a specificity Id. 
    
    public static final Color getColor(PTMSite site) {
        return getColor(site.getPTMSpecificity().getIdPtmSpecificity());
    }

    public static final Color getColor(Long specificityId) {
        Color c = PTM_COLORS.get(specificityId);
        if (c == null) {
            c = CyclicColorPalette.getColor(PTM_COLORS.size());
            PTM_COLORS.put(specificityId, c);
        }
        return c;
    }
    
    public static final Color getColor(PTMMark mark) {
        return getColor(mark.getPtmSpecificityId());
    } 

}
