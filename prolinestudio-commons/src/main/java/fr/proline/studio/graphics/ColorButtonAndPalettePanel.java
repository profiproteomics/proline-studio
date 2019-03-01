package fr.proline.studio.graphics;

import fr.proline.studio.graphics.colorpicker.ColorDataInterface;
import fr.proline.studio.graphics.colorpicker.ColorPalettePanel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

/**
 *
 * @author jm235353
 */
public class ColorButtonAndPalettePanel extends JPanel implements ColorDataInterface {
 
    final int PALETTE_DELTA = 3;
    final int PALETTE_SQUARE_SIZE = 12;
    final int PALETTE_WIDTH = 9;
    
    private ColorButton m_colorButton = null;
    
    public ColorButtonAndPalettePanel(ColorButton colorButton) {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 2);
        
        m_colorButton = colorButton;
        m_colorButton.addListener(this);
        
        c.gridx = 0;
        c.gridy = 0;
        add(m_colorButton, c);
        
        
        ColorPalettePanel palettePanel = new ColorPalettePanel(this, CyclicColorPalette.getPalette(), PALETTE_SQUARE_SIZE, PALETTE_DELTA, PALETTE_WIDTH);
        c.gridx++;
        add(palettePanel, c);
        

    }
    public ColorButtonAndPalettePanel() {
        this(new ColorButton());
        m_colorButton.initActionListener();
        m_colorButton.addListener(this);
    }
    
    public void setColor(Color c) {
        m_colorButton.setColor(c);
    }
    
    public Color getColor() {
        return m_colorButton.getColor();
    }

    @Override
    public void propagateColorChanged(int r, int g, int b) {
        m_colorButton.propagateColorChanged(r, g, b);
        repaint();
    }

    @Override
    public int getRed() {
        return m_colorButton.getRed();
    }

    @Override
    public int getGreen() {
        return m_colorButton.getGreen();
    }

    @Override
    public int getBlue() {
        return m_colorButton.getBlue();
    }

    @Override
    public void addListener(ColorDataInterface colorDataInterface) {
        // not used
    }
    
}
